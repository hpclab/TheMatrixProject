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
package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;
import it.cnr.isti.thematrix.mapping.utils.TempFileManager;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import it.cnr.isti.thematrix.scripting.utils.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.erasmusmc.utilities.NewFileSort;

/* WRONG plan -- setup creates the sorting thread in waiting, registers it in a global list. First read access wakes the
 * thread, which then goes on by itself; at end of batch, it goes to sleep and wakes the next sorting thread from the
 * list (1 the original method still being suspended) (2 since we are in the running phase, all thread have been
 * properly initialized already). As the reading job is done, the sorting thread removes itself from the list and starts
 * the actual sorting, then unlocks the caller. No: it needs to be done via thread. Not implemented now.
 */

/**
 * This class implements the SortModule functionality using the Jerboa on-disk
 * FileSorter, with some caveats about concurrency. Class initialization reads
 * the data from the input and stages it to disk in order to sort it. Sorting
 * requires all the data and some time, and data pull requests for MatrixSort
 * cannot return until the data is there. <br>
 * All calls to MatrixSort that needs actual data (
 * <code>hasmore() next() attributes() get()</code> ) are protected by the
 * semaphore <code>phase</code> (mutual exclusion of those methods) and will
 * call the sort() function if data is not yet there. Only once the sorting is
 * done (see <code>sort()</code>) the specific primitive is processed, the
 * semaphore is released and other data-accessing method are enabled. Class
 * relies on buffering: there is no need for semaphore fairness as we only
 * expect a single thread to be able to call us at a time (otherwise there would
 * be a buffer in between).<br>
 * 
 * FIXME This class creates a FileInputModule on the fly, thus possibly creating
 * an issue with postprocessing and linking other modules; I'm trying to creat
 * the InputModule a bit earlier.
 * 
 * TODO in case of a shortcut with a MatrixFileInput, use the tempfile to do a
 * raw copy of the input, which is then passed to the file sorter; this is to
 * avoid touching the original input file, which may be a shared or a permanent
 * one.
 * 
 * @author edoardovacchi massimo
 * 
 */
public class MatrixSort extends MatrixModule 
{
	private static final long serialVersionUID = -5414034925146978819L;
		
	private final List<String> sortFieldNames;
	private MatrixModule inputTable; // this is our input module

	// 
	/**
	 * temporary file full absolute pathname; it sits in the iad directory
	 */
	private String tempFileName;

	/**
	 * temporary file name stripped off of path and csv suffix 
	 */
	private String tempBaseName;

	/**
	 * temporary file where the sorting happens
	 */
	private File tempFile;

	// these are initialized in setup()
	/**
	 * reader module that gets back the sorted data from disc
	 */
	private MatrixModule readSortedModule;

	/**
	 * FIXME write module YET UNUSED ; should rewrite the data stream to disc
	 */
	private MatrixModule writeUnsortedModule;

	private String schemaName;

	/**
	 * The writer for the intermediate file.
	 */
	private CSVFile tempCSV = null;

	/**
	 * prevent any data read method before the sorting phase is complete
	 */
	private Semaphore phase = new Semaphore(1); // a semaphore with one permit

	/**
	 * true as soon as we enter the sort routine
	 */
	private boolean sorted = false;

	/**
	 * still unused: disable creation of the input temporary file (we need to
	 * get the file from somewhere!)
	 */
	private boolean skipInputFileCreation = false;

	private String readyFileName;

	public MatrixSort(String name, String inputTable, String schema,
			List<String> fieldNames) {
		super(name);

		this.inputTable = TheMatrixSys.getModule(inputTable);
		this.inputTable.schemaMatches(schema);
		this.schemaName = schema; // TODO save for setup(); do we really need to
									// save it?
		this.inputTable.addConsumer(this);
		this.sortFieldNames = fieldNames;

		// try { // initially we acquire the lock, no reading out until the file
		// is sorted
		// this.phase.acquire();
		// } catch (InterruptedException e) {
		// throw new Error("MatrixSort.MatrixSort exception " + e.toString());
		// }
	}

	/**
	 * setup the sort module.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setup() {
		/**
		 * should initialize everything needed by sort.
		 * 
		 * FIXME maybe initializations shall be delayed as much as possible?
		 * 
		 * TODO the code creating the temporary file should be skipped if we
		 * already have a temp file at hand; since we cannot do that (we are
		 * inside setup, we don't know yet) most of the changes will have to be
		 * undone in that case.
		 */
		this.setSchema(inputTable.getSchema());

		// we will use fieldNames.toArray() as the column list; Jerboa
		// Comparator wants either a single column index or
		// a String[] of names.
		if (sortFieldNames.isEmpty())
			throw new Error("MatrixSort.setup() no fields to sort with!");

		/*
		 * check disabled, should take final / into account, to check we are in
		 * the right dir? let's do that only once in the program
		 * 
		 * File baseDir = new File(System.getProperty("user.dir")); // should
		 * check this if
		 * (!baseDir.getAbsolutePath().equals(Dynamic.getBasePath())) { throw
		 * new Error( "MatrixSort.setup() -- Wrong Directory " +
		 * baseDir.getAbsolutePath() + " expected " + Dynamic.getBasePath()); }
		 */


		// take temp file for copying data
		this.tempFile = TempFileManager.newTempFile();
		tempFileName = tempFile.getAbsolutePath();
		tempBaseName = tempFile.getName();
		if (tempBaseName.endsWith(".csv")) {
			tempBaseName = tempBaseName.substring(0, tempBaseName.length() - 4);
		}
		
		// create file writer
		LogST.logP(2, "MatrixSort.setup() -- using header "+ this.getSchema().fieldNames().toString());		
		tempCSV = new CSVFile(this.tempFile.getParentFile().getAbsolutePath()+File.separator, tempBaseName, "");
		tempCSV.setHeader(this.getSchema().fieldNames());

		
		// /*** this is created later on **/
		// if (true){
		// // we will use a FileInput for reading; check that it doesn't try to
		// start reading too soon, otherwise move it
		// // to exec()
		// this.readSortedModule = new MatrixFileInput(this.name,
		// tempFile.getName(), this.getSchema(),
		// (List<String>) Collections.EMPTY_LIST);
		// readSortedModule.setup();
		// }
		// /*****/
		// changes to apply: use FileUtils.copyFile or Java 7 Files.copy

		LogST.logP(1, "MatrixSort.setup() done : " + this);
	}

	/************************************* POSTPROCESSING SUPPORT ****************************/	
	/**
	 * UNTESTED set the sort module for using an already existing input file; we
	 * need this as the condition is detected after class creation. The method
	 * shall be called after setup, this is a bit of a problem later on.
	 * @param fullname 
	 */
	public void overrideInputFile(String fullname) {
		LogST.logP(0, "MatrixSort:overrideInputFile() changing input to "
				+ fullname);
		// TODO check that it exists?
		readyFileName = fullname;
		skipInputFileCreation = true;
	};

	
	 public void changeInput (MatrixModule m)
	 {
		 this.inputTable.schemaMatches(m.getSchema().name); 
		 this.inputTable = m;
		 this.schemaName = m.getSchema().name;
	 }
	
	/**
	 * Get the input module, needed for postprocessing the graph. Use with great
	 * care.
	 * 
	 * @return the input module
	 */
	public MatrixModule getInput() {
		return inputTable;
	}


	/************************************** EXECUTION ****************************************/
	
	public void exec() {}

	/**
	 * Starts the sorting algorithm. Receive all the data; dump it to disc
	 * (possibly in batches); at data end, call the File Sorter; when sorted,
	 * open the file for reading; enable the hasMore(), next() and get methods
	 * 
	 * TODO: need the documentation about branches
	 */
	private void sort() 
	{
		sorted = true;
		
		LogST.logP(0, this.getClass().getSimpleName()+".sort() is sorting the following attributes: "+
				inputTable.attributes()+ " with fields "+sortFieldNames.toString());
		
		if (!skipInputFileCreation)
		{
			/***
			 * In this branch of the if we create the file
			 ***/

			LogST.logP(0, "MatrixSort.sort() " + this.name + " in tempfile creation branch");
			// fixing for large files : I should really build and call a
			// FileOutput module
			// TODO this could be done in the graph modification phase,
			// simplifying code here
			LogST.logP(2, "MatrixSort.sort() -- before reading data into the CSVFile object "+ this.name);
			
			
			// read all the data and save it
			int rowsInBatch = 0;
			int rows = 0;
			boolean appending = false;
			
			while (inputTable.hasMore()) 
			{
				inputTable.next(); // let's really go to the next row
				// manage writing to tempfile
				int columnCount = 0;
				for (Symbol<?> field : inputTable.attributes()) {
					// System.out.println(field.name +" " + field.type);
					tempCSV.setValue(columnCount,
							StringUtil.symbolToString(field));
					columnCount++;
					// maybe add some checks here? if one column skips we mangle
					// all the data
				}
				rows++;
				rowsInBatch++;
				if (rowsInBatch == Dynamic.writeCSVBatchSize) {
					// cut&paste! make it a function? in CSVFile?
					try {
						LogST.logP(2, "MatrixSort before saving to disc File");
						tempCSV.save(appending);
						appending = true;
						LogST.logP(2, "MatrixSort after save");
					} catch (Exception e) {
						throw new Error(e.toString());
					}
					
					// cute&paste END
					rowsInBatch = 0;
				}
			}
			LogST.logP(2, "MatrixSort after reading " + rows + " rows");
			// save the last batch, if it's not empty
			// note: if the whole file was empty we need to save anyway to
			// create the header
			// I will need to check that FileSorter is ok with that case
			if (rowsInBatch > 0 || rows == 0) {
				try {
					LogST.logP(1,
							"MatrixSort before saving last batch to disc File");
					tempCSV.save(appending);
					LogST.logP(1, "MatrixSort after last batch save");
				} catch (Exception e) {
					throw new Error(e.toString());
				}
				;

				// TODO write out the number of written rows

			}
		} 
		else {
			/***
			 * In this branch of the if we skip temp file creation, and copy the
			 * source file
			 ***/

			LogST.logP(0, "MatrixSort.sort() " + this.name +" in stolen file  branch");
			// check the Symbol fields
			/*
			 * for (Symbol<?> field : inputTable.attributes()) { Object
			 * symbolName = (Object) field.name; System.out.println(field.name
			 * +" " + this.inputTable.getSchema().get(symbolName)); }
			 */
			// undo setup() arrangements
			// - destroy the file writer, we don't need it
			tempCSV = null;

			/**
			 * this will maybe needed some day; currently the close routine
			 * breaks if file was not open at all try { // it was never written
			 * to, but we need to close it (may be a zip/gzip)
			 * tempCSV.closeFile(); } catch (IOException e) { throw new
			 * Error("MatrixSort:sort() deleting tempCSV for file "
			 * +tempFileName+"got Exception"+e); }
			 **/
			/*
			 * // - destroy the old tempfile, remove it from the bookkeeper if
			 * needed TempFileManager.deleteTempFileQuietly(tempFileName); // -
			 * modify the sorting routine call to target the new file name
			 */
			// TODO we actually need to make a copy of the file as the sort will
			// write to it in the end

			// TODO this will be the basename for temp names in FileSorter,
			// check for any issue there

			// *****************************************************************
			// this becomes a file copy
			// tempFileName = readyFileName;
			File source = new File(readyFileName);
			File dest = new File(tempFileName);
			LogST.logP(0, this.getClass().getSimpleName()+".sort() copying "+source+" to "+dest);
			try {
				copyFile(source, dest);
			} catch (IOException e) {
				LogST.logException(e);
				throw new Error(e.toString());
			}
		}
		/***** file is ready either way : we actually call sort it ********/

		// FIXME we don't know how the FileSorter comparator will handle the
		// various TheMatrix types
		//
		File f = new File(tempFileName);
		long MB = f.length() / 1024 / 1024; 
		LogST.logP(0, "MatrixSort.sort() calling sorter with " + f.getName() +
				"with size "+MB+"MB on fields " + sortFieldNames.toString());
		
		// sorting
		long before = System.currentTimeMillis();
		NewFileSort.sort(tempFileName, sortFieldNames.toArray(new String[0]), inputTable.getSchema());
		long after = System.currentTimeMillis();
		long duration_sec = (after - before)  / 1000;
			
		LogST.logP(0, "MatrixSort.sort() lasted "+duration_sec); 
		
		/******* complete the initialization of the reader module *****/

		// we will use a FileInput for reading
		this.readSortedModule = new MatrixFileInput(this.name, tempFile.getName(), this.getSchema(),
						(List<String>) Collections.EMPTY_LIST, true);
		readSortedModule.setup();		
		LogST.logP(1, "MatrixSort.sort() created reader : "+ this.readSortedModule);

		/***
		 * This is needed if we want the following modules to refer the new
		 * inputModule directly. If they refer to the sort module instead (the
		 * inputModule is hidden) then we shall not fiddle with the consumer
		 * list here.
		 * 
		 * migrateOnlyConsumer(this,readSortedModule);
		 ***/
		// release lock
		phase.release();
	}

	/**
	 * Returns true if there are more lines to read from the sorted data.
	 * hasMore() of Sort is actually reading from the sorted file after it has
	 * been sorted on disk. It has to<br>
	 * stall until all the data is collected, then<br>
	 * trigger the sorting code<br>
	 * trigger the initialization of a FileInput Module from the sorted output<br>
	 * from then, piggyback on the hasMore of the FileInput.<br>
	 * Eventually, it'll need to check that there are more receivers for the
	 * same input (maybe create more FileInput modules ?)
	 */
	@Override
	public boolean hasMore() {
		phase.acquireUninterruptibly(); // if data is not there, wait until exec
										// will unlock us
		if (!sorted)
			this.sort();
		boolean result = this.readSortedModule.hasMore();
		if (!result) {
			LogST.logP(1, "MatrixSort.hasmore() for module " + this.name
					+ " end of sorted file");
			if (Dynamic.keepTemporaryFiles) {
				// FIXME unimplemented
				LogST.logP(1, "MatrixSort.hasmore() for module " + this.name
						+ " _should_ delete temporary file" + this.tempFileName);
			}
		}
		phase.release();
		return result;
		// TODO on EOF, write out the number of read rows and of discarder rows

	}

	/**
	 * Skips to the next row of the sorted data. next() of Sort is actually
	 * reading from the sorted file after it has been sorted on disk. It has to<br>
	 * stall until all the data is collected, then<br>
	 * trigger the sorting code<br>
	 * trigger the initialization of a FileInput Module from the sorted output<br>
	 * from then, piggyback on the hasMore of the FileInput.<br>
	 * Eventually, it'll need to check that there are more receivers for the
	 * same input (maybe create more FileInput modules ?)
	 */
	@Override
	public void next() {
		phase.acquireUninterruptibly(); // if data is not there, wait until exec
										// will unlock us
		if (!sorted)
			this.sort();

		this.readSortedModule.next();
		this.setAll(readSortedModule);

		phase.release();
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Forwards the data access to the actual reading module
	 */
	@Override
	public List<Symbol<?>> attributes() {
		phase.acquireUninterruptibly(); // if data is not there, wait until exec
										// will unlock us
		// never happened so far, but this may actually be a potential error
		if (!sorted)
			LogST.logP(0,"Warning : MatrixSort.attributes() called before sort");
		
		// throw new Error ("MatrixSort.attributes() called before sort");
		phase.release();
		return this.readSortedModule.attributes();
	}

	/**
	 * Forwards the data access to the actual reading module
	 */
	// public Symbol<?> get(Object name) {
	// phase.acquireUninterruptibly(); // if data is not there, wait until exec
	// will unlock us
	// // never happened so far, but this may actually be a potential error
	// if(!sorted)
	// LogST.logP(0, "Warning : MatrixSort.get() called before sort");
	// //throw new Error ("MatrixSort.get() called before sort");
	// //we ignore the error for now
	// phase.release();
	// return this.readSortedModule.get(name);
	// }

	
	/********************************************** SUPPORT METHODS *******************/
	
	@Override
	public String toString() {
		return String
				.format("SortModule named '%s'\n with parameters:\n  %s\n  sort field names: %s\n\n",
						name, inputTable.name, sortFieldNames);
	}

	/**
	 * likely not going to be supported, but may be
	 */
	@Override
	public void reset() {
		throw new UnsupportedOperationException("Not supported yet.");
	}


	/**
	 * Utility function to manage copying a large file either via the OS native
	 * IO (quickest, on 64bit JVM) or via standard Java streams. On a 32bit JVM,
	 * the slower method works for files larger than the available address
	 * space, that cannot be memory-mapped.
	 * 
	 * FIXME this function and the related imports (NIO, InputStream and
	 * OutputStream) should be moved to an utility class. There should also be
	 * some logging of runtime info.
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	private static void copyFile(File sourceFile, File destFile)
			throws IOException {
		LogST.logP(1, "copyFile() " + sourceFile + " " + destFile);

		// check if 64 bit or not
		if (Dynamic.getDynamicInfo().isJvm64bit()) {
			// 64 bit java = NIO is ok

			if (!destFile.exists()) {
				LogST.logP(1,
						"copyFile, 64bit NIO mode, Creating destination file");
				destFile.createNewFile();
			}

			FileChannel source = null;
			FileChannel destination = null;

			try {
				source = new FileInputStream(sourceFile).getChannel();
				destination = new FileOutputStream(destFile).getChannel();
				destination.transferFrom(source, 0, source.size());
			} finally {
				LogST.logP(1, "copyFile 64bit finally");
				if (source == null && destination == null)
					LogST.logP(0, "copyFile error");
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
			}
		} else {
			// if 32 bit Java, no NIO

			LogST.logP(1, "copyFile, 32bit FileI/OStream mode, creating buffer");

			InputStream input = null;
			OutputStream output = null;
			try {
				// create a buffer of 1 MB in size; here, so that
				byte[] buf = new byte[1024 * 1024];

				input = new FileInputStream(sourceFile);
				output = new FileOutputStream(destFile);
				// byte[] buf = new byte[1024];
				int bytesRead;
				while ((bytesRead = input.read(buf)) > 0) {
					output.write(buf, 0, bytesRead);
				}
			} finally {
				LogST.logP(1, "copyFile 32bit finally");
				if (input == null && output == null)
					LogST.logP(0, "copyFile error");

				if (input != null)
					input.close();
				if (output != null)
					output.close();
			}
		}
		LogST.logP(1, "copyFile copy done, exiting");
	}

}
