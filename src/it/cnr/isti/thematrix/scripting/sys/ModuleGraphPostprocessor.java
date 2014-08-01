/*
 * Copyright (c) 2010-2014 "HPCLab at ISTI-CNR"
 *
 * This file is part of TheMatrix.
 *
 * TheMatrix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.isti.thematrix.scripting.sys;

import it.cnr.isti.thematrix.configuration.LogST;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * (Experimental) class to hold methods that query and modify the ModuleTable of
 * the interpreter (defined in TheMatrixSys). Targets are: 1) post=processing
 * the module graph before script execution 2) debugging post-processing 3)
 * providing standard methods to modify the graph for all those modules that
 * need to hack the graph in their constructor or at setup() time.
 * 
 * Issue: we really wish not to to break the way TheMatrixSys handles
 * interpreter recursion. In the long term having a single class where all graph
 * changes are performed improves interpreter recursion and graph rewriting
 * reliability.
 * 
 * We need to deal with two phases of post processing: the one in the outer
 * script (which happens after the interpreter eval()) and the one in the inner
 * scripts (which happens INSIDE the interpreter eval() and currenlty INSIDE the
 * ScriptInputModule code). This is caused by
 * 
 * ScriptInput spontaneously rewrites itself to a different module (either a
 * FileInput or a nested FileOutput) during the interpreter graph building
 * phase. Solution sketch: define a method for self-rewriting modules (at
 * constructor time or at setup() time: this is different; possibly it may be
 * unified) that can report here the change they do (or we could hook up the
 * functions provided in TheMatrixSys for this purpose). SEE ScriptInputModule
 * comments.
 * 
 * Issue: some of the methods may better be protected, assuming only classes in
 * the same package access them.
 * 
 * Assumptions: the class always operates on the current TheMatrixSys, which
 * internally manages the current interpreter data structure as a queue of
 * nested singletons. This may not be enough/true, needs checking.
 * 
 * @author massimo
 * 
 */
public class ModuleGraphPostprocessor {

	/**
	 * file where the DOT graph of modules will be saved
	 */
	private static FileWriter graphFileDesc;

	/**
	 * Set to a filename (in local directory) if we want to save the output from
	 * printGraphConsumers() at each post processing step
	 */
	private static String nameConsumerGraph = null;

	/**
	 * Clean up node names for DOT printing. Purges a module name from ASCII
	 * dots and other symbols embedded in it, also checks for a few substrings.
	 * Given syntax and file conventions, it is likely but there is
	 * unfortunately no guarantee that graph node names will end up being
	 * unique.
	 * 
	 * @param s
	 *            name to clean up
	 * @return
	 */
	private static String cleanUpName(String s) {
		s = s.replaceAll("-", "_");
		s = s.replaceAll("\\.csv", "_DOT_csv");
		s = s.replaceAll("\\.txt", "_DOT_txt");
		s = s.replaceAll("\\.", "_DOT_");
		return s;
	}

	/**
	 * Write a graph line to the logs and possibly to the proper Graph file.
	 * 
	 * @param sb
	 */
	private static void emitGraphLine(StringBuffer sb) {
		LogST.logP(0, sb.toString());
		if (graphFileDesc != null) {
			try {
				sb.append("\n");
				graphFileDesc.write(sb.toString());
			} catch (IOException e) {
				LogST.logP(0, "ERROR : emitGraphLine() could not write");
				LogST.logException(e);
				graphFileDesc = null; // forget it
			}
		}
		sb.setLength(0);
	}

	/**
	 * Open the file for saving the module graph in DOT format. If it cannot
	 * open the file, this method enables DOT dump into the LOGs.
	 * 
	 * @param name
	 *            a name that is suitable for a DOT file; used verbatim
	 */
	private static void openGraphFile(String name) {

		// LogST.logP(1, "openGraphFile("+name+")");
		try {
			graphFileDesc = new FileWriter(name);
		} catch (IOException e) {
			LogST.logP(0,
					"ERROR : openGraphFile() could not open file for writing "
							+ name);
			LogST.logException(e);
			graphFileDesc = null;
			TheMatrixSys.dumpConsumerGraph = true; // if we cannot save at all,
													// dump the graph(s) in the
													// logs
		}
	}

	/**
	 * Close the file after saving the module graph in DOT format, if needed.
	 */
	private static void closeGraphFile() {
		if (graphFileDesc != null)
			try {
				graphFileDesc.close();
			} catch (IOException e) {
				LogST.logP(0, "ERROR : closeGraphFile() got exception");
				LogST.logException(e);
				graphFileDesc = null;
			} finally {
				graphFileDesc = null;
			}
	}

	/*******************************************************************/

	/**
	 * Utility function to print the whole module graph, nodes and edges. Output
	 * suitable as a DOT language file for Graphviz output. It scans the whole
	 * inputTable looking at the consumers arrays (not all modules have an
	 * accessible input module or a single input). Can save to a file, will
	 * enable logging if the file cannot be open.
	 * 
	 * @param iterpreterInstance
	 *            pointer to the current TheMatrixSys interpreter used to access
	 *            the actual graph
	 * @param graphName
	 *            the graph name inside the dot file, use valid characters for a
	 *            DOT identifier
	 * @param phase
	 *            the phase identifier of this save; it is inserted in the file
	 *            name, so use a valid character
	 */
	public static void printConsumerGraph( String graphName, char phase) {

		TheMatrixSys interpreterInstance = TheMatrixSys.getInstance();
		
		/**
		 * graph defaults:
		 * 
		 * sep="+25.0,+100.0"; overlap=scalexy; node [shape=record]; edge
		 * [headport="n"];
		 * 
		 **/

		LogST.logP(0,
				"*****************************************************************************");

		StringBuffer line = new StringBuffer(1024);

		if (nameConsumerGraph != null)
			openGraphFile(nameConsumerGraph + "_" + phase + ".dot");

		// DOT preamble
		line.append("digraph ConsumerGraph_" + graphName + "_" + phase + " {\n"
				+ "sep=\"+25.0,+100.0\";\n" + "overlap=scalexy;\n"
				+ " node [shape=record];\n" + "edge [headport=\"n\"];");

		/*
		 * LogST.logP(0,"digraph ConsumerGraph_"+graphName+"_"+phase+" {\n"+
		 * "sep=\"+25.0,+100.0\";\n"
		 * +"overlap=scalexy;\n"+" node [shape=record];\n"+
		 * "edge [headport=\"n\"];"); // this is our DOT preamble
		 */
		emitGraphLine(line); // will also do the Logging and reset the buffer

		Collection<MatrixModule> allModules = interpreterInstance.moduleTable
				.values();

		// emit the list of nodes
		for (MatrixModule m : allModules) {
			int pos = m.getClass().toString().lastIndexOf('.');
			line.append(cleanUpName(m.name) + " [label =\"{{\\N}|{"
					+ m.getClass().toString().substring(pos + 1) + "}}\"];");
			emitGraphLine(line);
		}

		// now the edge lists, both regular and logical edges
		for (MatrixModule m : allModules) {
			List<MatrixModule> l = m.getConsumers();
			String mName = cleanUpName(m.name);
			for (MatrixModule m2 : l) {
				line.append(mName + " -> " + cleanUpName(m2.name) + ";");
				emitGraphLine(line);
				// LogST.logP(0,mName + " -> "+ cleanUpName(m2.name) +";");
			}

			// links which are logical dependencies (e.g. temp file I/O)
			l = m.getLogicalConsumers();
			if (!(l == null)) { // careful : this arrayList defaults to null
				for (MatrixModule m2 : l) {
					// LogST.logP(0,mName + " -> "+ cleanUpName(m2.name)
					// +" [ style = \"dotted\" ];");
					line.append(mName + " -> " + cleanUpName(m2.name)
							+ " [ style = \"dotted\" ];");
					emitGraphLine(line);
				}
			}
		}
		// LogST.logP(0,"}");
		line.append("}");
		emitGraphLine(line);
		if (nameConsumerGraph != null)
			closeGraphFile();

		LogST.logP(0,
				"*****************************************************************************");

	}

	/**
	 * Utility function to print the whole module graph as a list of edges.
	 * Quick&dirty approach, output suitable as input to a DOT language reader
	 * like graphviz. It scans the whole inputTable looking at the consumers
	 * arrays (not all modules have an accessible input module or a single
	 * input).
	 * 
	 * Note that with linked script graphs (ScriptInputModule) the printout may
	 * possibly reach modules of nested / containing scripts; this can happen
	 * only if the modules were linked to the current graph some way (e.g. by
	 * postprocessing).
	 * 
	 * TODO allow to put the name of the program in the graph title?
	 * 
	 * TODO allow to save output to a file instead?
	 * 
	 * TODO rewrite the function to make a depth-first visit
	 * 
	 * @param inst
	 *            the instance of the interpreter where the graph is stored
	 * @param graphName
	 *            the name of the graph to be used for saving and as DOT name
	 * @param phase
	 *            an arbitrary char labeling a specific rewriting phase in case
	 *            we need to save multiple graphs
	 * 
	 */
/****
 *  FIXME -- old method, disabled for compilation
 * 
 * 	public static void printConsumerGraph(TheMatrixSys inst, String graphName,
			char phase) {

		LogST.logP(0, "digraph ConsumerGraph_" + graphName + "_" + phase + " {"); // this
																					// is
																					// DOT
		Collection<MatrixModule> allModules = inst.moduleTable.values();

		for (MatrixModule m : allModules) {
			int pos = m.getClass().toString().lastIndexOf('.');
			LogST.logP(0, m.name + " [xlabel =\""
					+ m.getClass().toString().substring(pos + 1) + "\"];");
		}

		for (MatrixModule m : allModules) {
			List<MatrixModule> l = m.getConsumers();
			for (MatrixModule m2 : l) {
				LogST.logP(0, m.name + " -> " + m2.name + ";"); // this is DOT
			}
		}
		LogST.logP(0, "}"); // this is DOT

	}
**/

	/**
	 * probably useless
	 * 
	 * @param inst
	 * @return
	 */
	private static ModuleTable getCurrentModuleTable(TheMatrixSys inst) {
		if (inst != null)
			return inst.moduleTable;

		// LOG ERROR

		return null;
	}

	/**
	 * here we should have a method to register graph postprocessing changes if
	 * they are not evident from the consumers() arrays. Example: scriptInput
	 * dymamical graph rewriting.
	 * 
	 * virtual sort modules? (after postprocessing)
	 * 
	 * buffer modules?
	 */

	/**
	 * hashmap of modules to be give a special care when printing the graph
	 * 
	 * names of scriptinputs which are turned into fileoutputs from inners
	 * scripts
	 * 
	 * names of scriptinputs which are turned into fileinputs
	 * 
	 * names of fileinputs and (to be soon) fileoutputs that are generated by
	 * sort modules
	 * 
	 * names of buffers modules?
	 */

	/*********** removed from Sort, refactor **********************/
	/**
	 * Migrate all consumers from one old module to a new one when adding a new
	 * module to the table. Does not add any buffering, perform postprocessing
	 * later of if needed. This method shall be moved to a more general class
	 * and refactored (maybe e.g. allow renaming of the new module)
	 * 
	 * FIXME untested code! FIXME move to the proper class ().
	 * 
	 * @param mOld
	 * @param mNew
	 */
	private void migrateAllConsumers(MatrixModule mOld, MatrixModule mNew) {
		List<MatrixModule> consumerList = mOld.getConsumers();
		LogST.logP(0,
				"MatrixSort.migrateAllConsumers() : list in " + this.toString()
						+ " has size " + consumerList.size() + " "
						+ consumerList.toString());

		for (MatrixModule m : consumerList)
			m.changeInput(mNew);

	}

	/**
	 * Migrate exactly ONE consumer from one old module to a new one when adding
	 * a new module to the table. This method shall be moved to a more general
	 * class and refactored (maybe e.g. allow renaming of the new module)
	 * 
	 * FIXME untested code! FIXME move to the proper class ().
	 * 
	 * @param mOld
	 * @param mNew
	 */
	private void migrateOnlyConsumer(MatrixModule mOld, MatrixModule mNew) {
		List<MatrixModule> consumerList = mOld.getConsumers();
		if (consumerList.size() != 1) {
			LogST.logP(
					0,
					"MatrixSort.migrateOnlyConsumer() : list in "
							+ this.toString() + " has size "
							+ consumerList.size() + " "
							+ consumerList.toString());
			// FIXME shall throw some error or exception
		}
		consumerList.get(0).changeInput(mNew);
	}

}
