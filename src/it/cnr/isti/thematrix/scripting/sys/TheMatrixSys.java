package it.cnr.isti.thematrix.scripting.sys;

import it.cnr.isti.thematrix.configuration.ConfigChecker;
import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.Dynamic.selectedIADVersion;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.mapping.utils.TempFileManager;
import it.cnr.isti.thematrix.runtime.MemoryChecker;
import it.cnr.isti.thematrix.scripting.MatrixLang;
import it.cnr.isti.thematrix.scripting.functions.OperatorDefinitions;
import it.cnr.isti.thematrix.scripting.modules.BufferFileModule;
import it.cnr.isti.thematrix.scripting.modules.BufferModule;
import it.cnr.isti.thematrix.scripting.modules.MatrixFileInput;
import it.cnr.isti.thematrix.scripting.modules.MatrixFileOutput;
import it.cnr.isti.thematrix.scripting.modules.MatrixParameters;
import it.cnr.isti.thematrix.scripting.modules.MatrixSort;
import it.cnr.isti.thematrix.scripting.utils.DataType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import neverlang.runtime.EvaluationException;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.FileUtils;


/**
 * Main class of the TheMatrix interpreter, holding the common definitions and
 * objects needed by the interpreter.
 * 
 * The class mostly behaves like a singleton (with static fields and methods)
 * <i>except</i> when a new interpreters has to be recursively activated (due to
 * a subscript being called from the current script). In this case, the class
 * will push a copy of itself on its own internal stack structure, and create a
 * fresh copy for the sake of executing the subscript.
 * 
 * TODO refactor initialization stuff to a new TheMatrixSysDefinitions package
 * (temporary schemas)
 * 
 * TODO refactor methods that access the moduleTable to belong to the
 * ModuleTable class?  (thus removing the need to have them all static...)
 *
 * TODO (memento) check here
 * http://www.basilv.com/psd/blog/2012/visualizing-java-package-dependencies
 * 
 * @author edoardovacchi, massimo
 * 
 */
public class TheMatrixSys {

	/**
	 * Overall program version, for now put here in the configuration singleton.
	 * 
	 * It does NOT need to be the same as specified in the settings file. Fixed
	 * format "TheMatrix--?.??.??" so that two version strings can be compared.
	 * 
	 * TODO refactor, move elsewhere.
	 */
	public final static String TheMatrixProgramVersion = "TheMatrix--1.51.10";

	/**
	 * Release date
	 * 
	 * TODO refactor, move elsewhere
	 */
	public final static String TheMatrixReleaseDate = "2014-03-25";

	protected static SchemaTable predefinedSchemata = new SchemaTable();
	protected static OpTable opTable = new OpTable();
	protected static OpTable funcTable = new OpTable();

	/**
	 * adding protected fields with a getter method allow to reinitialize them
	 * for a nested interpreter and recover their original value when the nested
	 * call ends.
	 */

	/**
	 * a table of symbol instances. This is a map that contains all and only the elements in
	 * {@link #paramList}
	 * 
	 */
	protected SymbolTable paramTable = new SymbolTable();
	
	/**
	 * a list of {@link Symbol} instances that represents the typed values that a script expects, 
	 * as defined in its {@link MatrixParameters}. The Symbol instances shall occur 
	 * in the order that the {@link MatrixParameters} instance specify them.
	 * For instance, if ParametersModule defines two parameters:
	 * 
	 * <code>[{ SOMEINT: int}; {SOMESTRING: string}]</code>
	 * 
	 * then this list should contain, in the same order, the values for those 
	 * parameter definitions, e.g.: 
	 * <code>List(new Symbol<Integer>(15, Type.INT), new Symbol<String>("hello world", Type.STRING))</code>
	 * where "15" is some arbitrary number and "hello world" is some arbitrary string.
	 * 
	 * This is generally useful when the paramList is synthesized, e.g., it is being passed 
	 * from an outer interpreter (if the script being executed is nested), or for testing purposes.
	 * 
	 * The {@link MatrixParameters} instance iterates over this list and checks whether the argument 
	 * *types* match. Names are not required to match.
	 * 
	 *   Cf. {@link #paramList}
	 * 
	 */
	protected List<Symbol<?>> paramList = new ArrayList<Symbol<?>>();

	protected ModuleTable moduleTable = new ModuleTable();
	protected String currentModule;

	// FIXME questa non e' usata???
	// protected String[] args;
	
	/**
	 * The list of string arguments that are (or may be) read from the CLI. 
	 * The String arguments will be processed by the first (and only) 
	 * {@link MatrixParameters} instance.  Cf. {@link #paramList}
	 * 
	 * The string arguments shall occur in the order that the {@link MatrixParameters} 
	 * instance specify them. For instance, if ParametersModule defines two parameters:
	 * 
	 * <code>{ SOMEINT: int}; {SOMESTRING: string}]</code>
	 * 
	 * then this list should contain, in the same order, the values for those 
	 * parameter definitions, e.g.: <code>List("15", "hello world")</code>
	 * where "15" is some arbitrary number and "hello world" is some arbitrary string.
	 * 
	 * 
	 */
	protected List<String> argList = Collections.emptyList();

	// singleton instance
	public static boolean isError = false;
	
	/**
	 * instance of the actual Neverlang intepreter
	 */
	private static MatrixLang matrixLang = new MatrixLang();
	
	/**
	 * innermost interpreter state instance (current)
	 */
	private static TheMatrixSys inst;
	
	/**
	 * stack of nested interpreter state instances
	 */
	private static Stack<TheMatrixSys> state = new Stack<TheMatrixSys>();

	/**
	 * The list of output modules to be taken care of for the current script.
	 * Will contain at least one module (which may or may not be an output
	 * module, see defaultOutput.
	 * 
	 * TODO in order to correctly support multiple FileOutput modules in nested
	 * scripts maybe this will need to be a protected field, allowing it to be
	 * reset / dealt with separately at the boundary of the two instances of the
	 * interpreter
	 */
	private static List<MatrixFileOutput> listOM = null;
	
	/**
	 * Second list of FileOutputModules (see listOM) which is only used after
	 * the script execution; see also performGraphPostProcessing().
	 * 
	 * We don't pull from FileOutputMs that have consumers during script
	 * execution (data loss would result). However, since we cannot be sure that
	 * all the output is extracted by the other stages (this depends on the
	 * semantic of the program, so data-dependent) at end of script we pull any
	 * remaining data from those modules, which are recorded in this list.
	 */
	private static List<MatrixFileOutput> listOMPostExecution = null;

	/**
	 * true if and only if the script does contain no output modules, it
	 * activates the debug-time behaviour of extracting the output from the last
	 * module in the script chain returned by getCurrentModule()
	 * 
	 * TODO in order to correctly support multiple FileOutput modules in nested
	 * scripts maybe this will need to be a protected field, allowing it to be
	 * reset / dealt with separately at the boundary of the two instances of the
	 * interpreter
	 */
	private static boolean defaultOutput = false;

	/**
	 * Set to true if any terminal exceptions are caught in the processing, so
	 * that we can do the cleanup and still report the error happened.
	 */
	private static boolean completeWithErrors = false;
	
	/**
	 * Set to true if we want a dump from printGraphConsumers() before and after
	 * the post processing step. It is protected as we may need to change it in
	 * ModuleGraphPostProcessor
	 */
	protected static boolean dumpConsumerGraph = false;
	
	/**
	 * Set to true a filename (local directory) if we want to save theoutput from printGraphConsumers() at each post processing step
	 */
	private static String nameConsumerGraph =null;

	/**
	 * Method to access the current (innermost) instane of the interpreter.
	 * 
	 * FIXME This method shall be used only from the static class
	 * ModuleGraphPostProcessing. We will need to move both classes in a
	 * dedicated package space.
	 * 
	 * @return the current instance pointer to the TheMatrixSys class
	 */
	protected static TheMatrixSys getInstance() {
		return inst;
	}

	
	// private static List<File> tempFilesToDelete = new ArrayList<File>();
	/**
	 * Static initialization block; here insert initialization to be performed
	 * just once (at first TheMatrixSys reference).
	 * 
	 * This static block is removed; future unit testing shall call
	 * internal_main with appropriate parameters.
	 * 
	 * call here prepareSystem() and implement there all once-and-for-all
	 * initialization code <br>
	 * 
	 */
	/**
	 * static {
	 * 
	 * prepareSystem(); // initialize system, e.g. symbol tables for schemas and
	 * operators
	 * 
	 * // most of the initialization code for TheMatrix classes has been moved
	 * to main() Dynamic.getDynamicInfo(); // singleton with dynamic run-time
	 * info
	 * 
	 * saveState();
	 * 
	 * boolean assertsEnabled = false; assert assertsEnabled = true; //
	 * Intentional side effect!!! if (!assertsEnabled)
	 * System.err.println("Assertions are disabled");
	 * 
	 * // call internal_main to complete initialization, with special
	 * parameters. // try { internalMain(new String[0]); // } catch (Exception
	 * e) { // };
	 * 
	 * }
	 **/

	/**
	 * saves state that is local to the current interpreter to prepare
	 * the execution of another (nested) interpreter. States are maintained in a stack
	 */
	public static void saveState() {
		inst = new TheMatrixSys();
		state.push(inst);
	}

	/**
	 * restores the old state, pops current state from the stack
	 * @return the object representing the state that is being popped from the stack
	 */
	public static TheMatrixSys restoreState() {
		TheMatrixSys oldState = state.pop();
		inst = state.peek();
		return oldState;
	}

	public static boolean isDryRun() {
		return Dynamic.dryRun;
	}

	public static OpTable getOpTable() {
		return opTable;
	}

	public static OpTable getFuncTable() {
		return funcTable;
	}

	public static DatasetSchema getPredefinedSchema(String identifier) {
		if (!predefinedSchemata.containsKey(identifier))
			throw new RuntimeException("Unknown schema " + identifier);
		return predefinedSchemata.get(identifier);
	}

	public static SchemaTable getPredefinedSchemata() {
		return predefinedSchemata;
	}

	public static void reset() {
		state.pop(); // discard
		saveState();
	}

	/**
	 * 
	 * @return a map of symbols representing the parameters for this script
	 */
	public static SymbolTable getParamTable() {
		return inst.paramTable;
	}

	/*
	 * 
	 */
	public static List<Symbol<?>> getParamList() {
		return inst.paramList;
	}

	public static void setParamList(Symbol<?>... ss) {
		setParamList(Arrays.asList(ss));
	}

	/**
	 * set the list of symbols representing the values of the parameters
	 * for this script. This also set {@link #paramTable}
	 * @param paramList
	 */
	public static void setParamList(List<Symbol<?>> paramList) {
		inst.paramList = paramList;
		SymbolTable t = new SymbolTable();
		t.putAll(paramList);
		inst.paramTable = t;
	}

	@Deprecated
	public static void setParams(Symbol<?>... ss) {
		setParams(Arrays.asList(ss));
	}

	@Deprecated
	public static void setParams(List<Symbol<?>> ss) {
		SymbolTable t = new SymbolTable();
		t.putAll(ss);
		setParams(t);
	}

	@Deprecated
	public static void setParams(SymbolTable t) {
		inst.paramTable = t;
	}

	@SuppressWarnings("unchecked")
	public static <T extends MatrixModule> T getModule(String identifier) {
		return (T) inst.moduleTable.get(identifier);
	}

	/**
	 * ModuleTable.put() used by the parser and by this class to add new
	 * modules, checking for duplicates. In case of duplicate name we raise an
	 * exception to be caught later on (outside of the eval() ).
	 * 
	 * TODO This is currently done extending RuntimeException, as the
	 * Neverlang-generated code does not have matching throws declarations.
	 * 
	 * @param m
	 */
	public static void addModule(MatrixModule m) {
		MatrixModule m1;
		m1 = inst.moduleTable.put(m.name, m);

		if (m1 != null)
			throw new RedefinedModuleNameException(
					"TheMatrix module name conflict: module " + m.name
							+ " already exists");
	}

	/**
	 * ModuleTable.put() used by this class and by module implementations to
	 * substitute modules which already exist. It checks that a module with the
	 * same name exists; if there is none, we raise an exception to be caught
	 * later on (outside of the eval() ).
	 * 
	 * FIXME probably not needed, the one with a name specified is actually
	 * called TODO This is currently done extending RuntimeException, as the
	 * Neverlang-generated code does not have matching throws declarations.
	 * 
	 * @param m
	 */
	public static void substituteModule(MatrixModule m) {
		MatrixModule m1;
		m1 = inst.moduleTable.put(m.name, m);
		if (m1 == null)
			throw new UndefinedModuleNameException(
					"TheMatrix substituteModule() module " + m.name
							+ " not found");
	}

	/**
	 * Module table put used by the parser when processing MatrixScriptInput
	 * Modules; it checks that a module with the same name actually exists. Note
	 * that the module name in the lookup table is the one specified in this
	 * call, which is NOT necessarily the same one the module has in the
	 * referenced script. In case the module is not found we raise an exception
	 * to be caught later on (outside of the eval() ).
	 * 
	 * TODO This is currently done extending RuntimeException, as the
	 * Neverlang-generated code does not have matching throws declarations.
	 * 
	 * @param name
	 * @param m
	 */
	public static void substituteModule(String name, MatrixModule m) {
		MatrixModule m1;
		m1 = inst.moduleTable.put(name, m);
		if (m1 == null)
			throw new UndefinedModuleNameException(
					"TheMatrix substituteModule() module " + m.name
							+ " not found");
	}

	public static void setCurrentModule(MatrixModule m) {
		inst.currentModule = m.name;
	}

	public static MatrixModule getCurrentModule() {
		if (inst.currentModule == null)
			throw new RuntimeException("No dataset has been loaded yet!");
		return inst.moduleTable.get(inst.currentModule);
	}

	/**
	 * It expects a list of String arguments, possibly passed in by the CLI.
	 * The arguments are processed by the first (and only) {@link MatrixParameters} 
	 * that occurs in a script. 
	 * 
	 * @param argList
	 */
	public static void setArguments(List<String> argList) {
		inst.argList = argList;
	}

	/**
	 * overloaded version of {@link #setArguments(List)}
	 * @param argarr
	 */
	public static void setArguments(String[] argarr) {
		inst.argList = new ArrayList<String>(Arrays.asList(argarr));
	}

	/**
	 * Called by {@link MatrixParameters#setup()} when processing parameters
	 * 
	 * @return a list of String arguments to be processed
	 */
	public static List<String> getArgumentList() {
		return inst.argList;
	}

	/**
	 * Largest part of the main() of this class, used to initialize tons of data
	 * without running the script eval(); written in order to be called from a
	 * static bloc, or possibly from a separate class.
	 * 
	 * Returns a boolean: if false, it means we should not continue normal
	 * execution due to non recoverable error.
	 * 
	 * @param args
	 * @throws IOException
	 */
	private static boolean internalMain(String[] args) {

		// this enables logging and opens the file
		LogST.getInstance();

		// create the inst object
		saveState();
		
		Dynamic.getDynamicInfo(); // we need to initialize our singleton

		LogST.enable(Dynamic.loggingLevel); // with default logging level

		/*************** parameters ***/
		readCommandLine(args);

		LogST.enable(Dynamic.loggingLevel); // logging level from parameter, if
											// stated

		/*************** deal with directory structure setup */
		// we assume to be run in TheMatrix base dir
		Dynamic.setBasePath(System.getProperty("user.dir"));
		Dynamic.setPaths();

		/*** further checks on parameter values ***/
		if (Dynamic.getScriptPath() == null) {
			throw new Error("Script path is null.");
		} // impossible?

		// the script name is checked in the real main as here it may not be
		// present

		/*************** Initialize logging and singleton with TheMatrix configuration *******************/

		java.util.Date d = new java.util.Date();

		String currDir = System.getProperty("user.dir");

		// N.B. this should be the only println in the program; thereafter, only
		// use LogST;
		// equivalent message is recorded opens the log, done independently by
		// LogST
		System.out.println("Matrix Test - avvio: " + d.toString()
				+ " Directory " + currDir);

		LogST.getInstance().startupMessage(TheMatrixProgramVersion,
				TheMatrixReleaseDate);
		LogST.logP(
				0,
				"-> Configuration check - settings.xml - query size limit is "
						+ ConfigSingleton.getInstanceFromPath(Dynamic
								.getBasePath()).theMatrix
								.getTestQuerySizeLimit().toString() + "\n");

		/********** check configuration and directory structure */
		// if we got here past ConfigSingleton, then settings.xml has been read
		// test disabled as also sanityCheck depends on the current dir...
		// TODO refactor/remove that Test class entirely
		if (!ConfigChecker.checkDirs())
			throw new Error("Failed directory configuration check");
		// this should cause the mapping singleton to be created and the mapping
		// configuration parsed, if not already
		if (!ConfigChecker.checkMapping())
			throw new Error("Failed mapping configuration check");

		/***********
		 * parse further information from the configuration file into the
		 * Dynamic singleton
		 ****/
		Dynamic.getDynamicInfo().parseTestQuerySizeLimit();
		LogST.logP(0, "MatrixSys: test Query Size enabled: "
				+ Dynamic.doLimitQueryResults + " with size "
				+ Dynamic.getDynamicInfo().getTestQuerySizeLimit());

		Dynamic.getDynamicInfo().parseBufferCompression();
		LogST.logP(0, "MatrixSys: buffer compression is: "
				+ Dynamic.bufferCompression.toString());

		/*************** setup for the interpreter **/

		prepareSystem(); // initialize system, e.g. symbol tables for schemas
							// and operators
		/***********
		 * Emit some diagnostic info on data sizes. assumes preparesystem has
		 * been called within the static block. TODO remove from here
		 *****/
		MappingUtils.IADSchemataExpectedOutputSize();

		/****************
		 * process the --dumpMapping option : print schemata and then exit
		 ***/
		if (Dynamic.dumpMapping) {
			// TheMatrixIADDefinition.IADSchemataToString();
			MappingUtils.IADSchemataToXMLMapping(false); // use void mappings
			return false; // do not continue running
		}

		/****** end of TheMatrix initialization code ***************/
		return true;
	}

	/**
	 * This is the main called at program startup, taking care of all
	 * initializations and of script execution. Delegates part of the work to
	 * reusable auxiliary methods, which are typically static and return a
	 * boolean result.
	 * 
	 * @param args
	 *            ordinary meaning
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		// call internal main for most of the initializations
		boolean continueRunning = internalMain(args);
		if (!continueRunning) {
			LogST.logP(0, "Further processing disabled by user option or error");
			LogST.logP(
					0,
					"TheMatrix -> run complete: "
							+ new java.util.Date().toString());
			LogST.getInstance().close();
			return;
		}
		// now we now we can continue

		/*************** continue processing parameters *********/
		if (Dynamic.scriptFileName == null) {
			throw new Error("Script name is missing.");
		}

		LogST.logP(0, "Opening dir" + Dynamic.getScriptPath());
		File scriptSrc = new File(Dynamic.getScriptPath(),
				Dynamic.scriptFileName);
		if (!scriptSrc.exists() || !scriptSrc.canRead()) {
			System.err
					.println("Invalid input file specified as script (not found or unreadable)");
			LogST.logP(0,
					"TheMatrixSys.main() - Script file not found or not readable : "
							+ scriptSrc.getAbsolutePath() + "\n");
			System.exit(1);
		}

		/**
		 * Create and start memory-occupation checking thread.
		 */

		MemoryChecker memThread = new MemoryChecker(60);
		memThread.start();

		/*********
		 * Script Evaluation Core : <br>
		 * 1) load/parse script;<br>
		 * 2) post-process and modify module chain if needed; this is offloaded
		 * to a separate method <br>
		 * 3) pull results out of the output module(s)<br>
		 * 
		 * Note: daisy-chained buffer should work, but should not be used. We do
		 * not check atm.
		 *****/

		scriptSrc = new File(Dynamic.getScriptPath(), Dynamic.scriptFileName);
		
		try {
			// load and parse the script
			eval(FileUtils.fileToString(scriptSrc.getAbsolutePath()));

			LogST.logP(0, "TheMatrixSys.main() : script parsed, postprocessing ************************");

			// postprocessing of the script graph
			if (!performModuleGraphPostProcessing(Dynamic.scriptFileName))
				throw new Exception("Script post-processing failure");

			LogST.logP(0, "TheMatrixSys.main() : starting execution ***********************************");

			// does the pull data action that executes the script 
			pullOutput();
			
		} 
		catch (EvaluationException e) 
		{
			completeWithErrors = true;
			LogST.logP(0, "TheMatrixSys.main() - eval/pull cycle got Exception from Neverlang parser -- syntax error?");
			
			String message = e.getException().getMessage();
			ASTNode n = e.getOrigin();
			String originInfo = getInfo(n);
			LogST.logP(0, originInfo + " raises an error, with messsage: " + message);
			
			LogST.logException(e);
		} catch (Exception e) {
			completeWithErrors = true;
			LogST.logP(0, "TheMatrixSys.main() Unhandled Exception " + e);
			// e.printStackTrace(new
			// PrintWriter(LogST.getInstance().getWriter()));
			// e.printStackTrace();
			LogST.logException(e);
		} finally {
			memThread.stopChecking();
			LogST.spindown();
		}

		LogST.logP(0, "Deleting temporary files");
		TempFileManager.deleteAllFiles();
		LogST.logP(0,
				"TheMatrix -> run complete: " + new java.util.Date().toString());

		if (completeWithErrors)
			LogST.logP(
					0,
					"Unrecoverable Errors were detected during execution, check the above output and/or the timeline log for details");

		LogST.getInstance().close();
	}

	/**
	 * Deal with pulling output from all active output modules in the script.
	 * Uses the two listOM* variables generated by postprocessing. Takes care of
	 * terminal modules first, getting batches of rows from each output; round robin schedule among the non-empty ones. It then drains out non terminal pending modules if
	 * any, each one all of the remaining output .
	 */
	static private void pullOutput () {
		/**
		 * pulling output loop; this is needed to extract the data from the
		 * last module and make the script actually perform its work; it
		 * will change when we have multiple output modules
		 */
		if (Dynamic.dryRun) {
			LogST.logP(0,
					"TheMatrixSys.pullOutput() dry-run : skipping (last) output stage(s)");
		} else if (listOM.size() == 1) { // case with one output only
			LogST.logP(0,
					"TheMatrixSys.pullOutput() pulling output from module chain");
			MatrixModule out = listOM.get(0);
			LogST.logP(0, "TheMatrixSys.pullOutput() output filename " + out.name);
			int i = 0;
			while (out.hasMore()) {
				out.next();
				i++;
			}
			LogST.logP(1, "TheMatrixSys.pullOutput() end of output after " + i
					+ " rows (not including headers)");

		} else { // case with multiple output modules go together for now

			/******** case with file buffers ********/
			/******** case with in-memory buffers ********/
			final int oBatchSize = 100; // fixed batch size for now

			LogST.logP(0,
					"TheMatrixSys.pullOutput() pulling output from multiple outputs");
			/**
			 * how it's done (temporary): get the list of all output
			 * modules, pull out from each one a row/batch of rows buffering
			 * issues should be taken care of by the BufferModule
			 */
			for (MatrixModule out : listOM) {
				LogST.logP(0, "TheMatrixSys.pullOutput() output filename "
						+ out.name);
			}
			LogST.logP(0, "TheMatrixSys.pullOutput() starting output ");

			List<MatrixModule> activeOM = new ArrayList<MatrixModule>(
					listOM); // for optimization, currently unused

			int numRows[] = new int[listOM.size()];
			boolean active[] = new boolean[listOM.size()];
			for (int i = 0; i < active.length; i++)
				active[i] = true;

			// outer loop of indefinite length
			while (true) {
				// are we done yet?
				boolean notdone = false;
				for (int i = 0; i < active.length; i++) {
					notdone = notdone || active[i];
				}
				if (!notdone)
					break;
				else
					LogST.logP(0,
							"TheMatrixSys.pullOutput() : stepping to next output batch");

				//
				int mIndex = 0;
				for (MatrixModule m : listOM) {
					int rows = 0;
					/**
					 * rework to print message when a stream ends
					 */
					if (active[mIndex]) {
						while ((active[mIndex] = m.hasMore())
								&& rows < oBatchSize) {
							m.next();
							rows++;
						}
						numRows[mIndex] += rows;
					}
					mIndex++; // go to next output module

				}
			}

			LogST.logP(1, "TheMatrixSys.pullOutput() end of multiouput");
			{
				int i = 0;
				for (MatrixModule out : listOM) {
					LogST.logP(0, "TheMatrixSys.pullOutput() output file "
							+ out.name + " ended after " + numRows[i]
							+ " rows (not including headers)");
					i++;
				}
			}

		}
		LogST.logP(0, "TheMatrixSys.pullOutput() : post-execution (output drain) **********************************");
		/**
		 * here we have to check that all pending output from non-terminal
		 * FileOutputModules is drained. We pull one at a time (safer, due
		 * to possible consequences they have on each other).
		 */
		for (MatrixModule m: listOMPostExecution ) {
			if (m.hasMore()) {
				LogST.logP(1, "TheMatrixSys.pullOutput() draining module "+m.name);
				int rows =0;
				while (m.hasMore()) {
					m.next();
					rows ++;
				}
				LogST.logP(1, "TheMatrixSys.pullOutput() got "+rows+" rows from module "+m.name);
			}
			else
				LogST.logP(1, "TheMatrixSys.pullOutput() skipping module "+m.name);
		}
		LogST.logP(0, "TheMatrixSys.pullOutput() : post-execution done on "+listOMPostExecution.size() +" modules **");
		
	}
	

	/********************************************************************************************
	 * BEGIN
	 * 
	 * THIS PART OF THE CLASS SHALL BE MOVED TO THE ModuleGraphPostprocessor class 
	 ********************************************************************************************/
	
	
	/**
	 * Performs various kind of postprocessing operations on the graph of
	 * modules contained in the moduleTable, inserting/deleting modules to
	 * support specific language features without the user knowing it or stating
	 * explicitly.
	 * 
	 * Returns a boolean: if false, it means we should not continue normal
	 * execution due to non recoverable error.
	 * 
	 * Affects the object state and returns values by modifying important
	 * instance variables besides the moduleTable: see listOM, defaultOutput
	 * 
	 * 
	 * Currently supports the following postprocessing operations:<br>
	 * 1) add BufferModules wherever multiple module read from a same source
	 * module <br>
	 * 2) find the output modules defined at the outer level of scripting <br>
	 * 3) find the sort modules which actually read from a temporary file and
	 * shortcut them to avoid the extra copy <br>
	 * 4) explicitly create at least an output module, if they are missing <br>
	 * 
	 * Note: daisy-chained buffer should work, but should not be used. We do not
	 * check atm. Besides, see within the comments the design of a mechanism for
	 * additional modules to postprocess DBMS downloaded data.
	 * 
	 * @return false means the method has failed some way, true means continue
	 *         execution.
	 */
	private static boolean performModuleGraphPostProcessing(String fileName) {
		String graphName = fileName.trim(); 
		graphName = graphName.substring(0, graphName.length()-4); //strip the .txt
//		graphName = graphName.replace('-', '_');
		graphName = graphName.replaceAll("[-\\s\\.]", "_"); //remove any whitespace, dot or minus
		if (dumpConsumerGraph || nameConsumerGraph != null)
		{
			LogST.logP(0, "Module Consumer Graph before postprocessing");
			printConsumerGraph(graphName,'0');
		}
		/***** module chain post-processing ***************************************/

		/**
		 * Find where BufferModules are needed, and insert them; i.e. allow any
		 * module to push the same data stream to multiple destination modules.
		 * Most of the insertion work is done within the BufferModule class, as
		 * all the parameters are known there.
		 * 
		 * Reworked on 9/12/2013; now that FileOutputMs can be used as passing
		 * stages, we need to avoid pulling output from them if there is already
		 * a module draining, otherwise we will discard data.
		 * 
		 * Two options: 1) add buffers before them (ugly and waste of resources)
		 * 2) do not pull output if the FileOutput is already connected (check
		 * its Consumers is not empty). I am implementing solution 2) (la
		 * seconda che hai detto...). Warning: we cannot be sure that all the
		 * output is extracted by the other stages (this depends on the semantic
		 * of the program, so data-dependent). Final solution: add the
		 * FileOutput modules which have consumers to a second list, which we
		 * don't pull from by default; at end of script, pull any remaining data
		 * from modules of that list.
		 * 
		 * 
		 */
		for (MatrixModule m1 : new LinkedList<MatrixModule>(
				inst.moduleTable.values())) {
			LogST.logP(1, "PostProcessing: "+m1.getConsumers().size()+" consumers on module "+ m1.name);
			if (m1.getConsumers().size() > 1) {
				LogST.logP(0, "PostProcessing: adding Buffer after module "+ m1.name);
				if (Dynamic.useMultiReaderMemoryBuffers) {
					// note that this module will add itself to the table, and
					// recursively create proxies
					new BufferModule(m1.name + "_Buffer", m1.name,
							Dynamic.multiReaderBufferSize);
				} else {
					// note that this module will add itself to the table, and
					// recursively create proxies, input
					// and output modules; no size spec is currently supported
					new BufferFileModule(m1.name + "_Buffer", m1.name, 0);
				}
			}
		}

		/**
		 * TO DO
		 * 
		 * adding the ScriptinputModule for mapping postprocessing may go here;
		 * not before here! (needs the buffers in place)
		 */

		/***
		 * Algorithm for adding a mapping post-processing script
		 * 
		 * for each input module MI {
		 * 
		 * check in the mapping singleton if the input file F is a file that can
		 * be downloaded from DBMS (if not skip this module)
		 * 
		 * check if the file F exists and is valid (if it is there, skip this
		 * module) [check how to in code of MatrixFileInput]
		 * 
		 * check that there is only one module reading from MI (if not, throw
		 * error and stop) [it is the size of the consumers list of MI]
		 * 
		 * if we are there, the file F will need to be downloaded from DBMS, so
		 * {
		 * 
		 * change the module MI file name from F to write on a temporary file
		 * like F.stage1.csv (this change needs to reach the underlying level of
		 * mapping, as normally MI would NOT write any file)
		 * 
		 * create a Scriptinputmodule with the script name that is associated to
		 * F in the mapping XML config
		 * 
		 * associate the Scriptinputmodule SCI to read from F.stage1.csv (this
		 * needs to be a parameter of the file, or be hard-coded in the script)
		 * 
		 * NOTE : the module SCI also needs to write the file that we initially
		 * wanted to be mapped; again, this can be passed as parameter or
		 * hard-coded in the file.
		 * 
		 * get the module that was reading from MI, and make it read from SCI
		 * 
		 * }
		 * 
		 * }
		 * 
		 * How to make the MI produce the F.stage1.file :
		 * 
		 * 1) add all MI that are modified to a listMI variable.
		 * 
		 * 2) at the end of postprocessing, pull all the modules in listMI all
		 * of their input, one module at a time (this will trigger the actual
		 * DBMS downloading procedure); this _before_ pulling the output from
		 * listOM.
		 * 
		 * alternative to step 2: call the MI function that dowloads the data
		 * from DBMS; may be simpler to change the file name doing this way
		 * 
		 * CAVEAT : check that there are no two MI with the same input file. It
		 * may happen, nothing prevents it in the script syntax, and it will
		 * wreak havock in previous pseudo-code.
		 * 
		 */

		
		
		
		/**
		 * Find the output modules at the outer level of scripting, to be pulled
		 * at script execution time; ignores modules added by postprocessing
		 * (e.g. to-disk buffers)
		 */
		listOM = getUserOutputModules(true); 
		
		if (listOM.size() == 0)
			defaultOutput = true;

		/**
		 * Find output modules at the outer level of scripting which must NOT be
		 * pulled at script exeution time; they may need to be emptied at the
		 * end of the script. Ignores modules added by postprocessing.
		 **/
		listOMPostExecution = getUserOutputModules(false); 
		
		/**
		 * TO DO
		 * 
		 * adding the ScriptinputModule for mapping postprocessing may go here;
		 * not after here! (Input->Sort must NOT be optimized if we need to add
		 * a ScriptInputModule in between)
		 */

		/**
		 * Find any sort modules which actually read from a temporary file and
		 * shortcut them, skipping the useless copy to a different temporary
		 * file.
		 * 
		 * FIXME this should be changed to work with reading from a BufferFileInput/BufferProxyModule.
		 */
		for (MatrixModule m : inst.moduleTable.values()) {
			// it is a sort module
			if (m instanceof MatrixSort) {
				MatrixSort ms = (MatrixSort) m;
				if (ms.getInput() instanceof MatrixFileInput) {
					String msName = ((MatrixFileInput) ms.getInput())
							.fileThatCanBeStolen();
					if (!msName.isEmpty()) {
						ms.overrideInputFile(msName);
						LogST.logP(0, "PostProcessing: shorting SortModule "+ m.name+" with previous InputModule");

					}
				}
				/**
				 * TODO actually the file input
				 * 
				 * (A) will never be called: we should remove it from the table
				 * 
				 * (B) may be a buffer: we should move this optimization up the
				 * chain so that the buffer has less readers (may be useless) or
				 * write a stub to link to the buffer file.
				 */
			}
		}

		/**
		 * Explicitly create at least an output module if missing; it's linked
		 * to the current module in this case we want to save to the iad
		 * directory by default
		 **/
		if (defaultOutput /* listOM.size() == 0 equivalent */) {
			/**
			 * FIXME need to call a function that decides the module/file name,
			 * either here or in the MFOut module
			 **/
			MatrixFileOutput out = new MatrixFileOutput("Script_"
					+ Dynamic.scriptFileName + "_test",
					TheMatrixSys.getCurrentModule());
			
			
			// out.setSaveToResultsPath(false); // needed when default changes, probably not neeeded
			out.setup();
			listOM.add(out);
			TheMatrixSys.addModule(out);
			TheMatrixSys.setCurrentModule(out); // so we don't have to make
												// cases afterwards
			LogST.logP(0, "TheMatrixSys.performModuleGraphPostProcessing() : added default output module"
					+ out.name);
		}

		if (listOM.size() > 1) {
			LogST.logP(0,
					"TheMatrixSys.performModuleGraphPostProcessing() : "+ Integer.toString(listOM.size()) +" output modules");
		}
		/********* post processing done *********/
		if (dumpConsumerGraph || nameConsumerGraph != null)
		{
			LogST.logP(0, "Module Consumer Graph after postprocessing");
			printConsumerGraph(graphName,'Z');
		}
		
		return true;
	}

	/*******************************************************************/

	
	/**
	 * Utility function to print the whole module graph, nodes and edges. Output
	 * suitable as a DOT language file for Graphviz output. It scans the whole
	 * inputTable looking at the consumers arrays (not all modules have an
	 * accessible input module or a single input). Can save to a file,
	 * 
	 * @param graphName
	 *            the graph name inside the dot file, use valid characters for a
	 *            DOT identifier
	 * @param phase
	 *            the phase identifier of this save; it is inserted in the file
	 *            name, so use a valid character
	 */
	private static void printConsumerGraph(String graphName, char phase) {

		/**
		 * graph defaults:
		 * 
 sep="+25.0,+100.0";
 overlap=scalexy;
 node [shape=record];
 edge [headport="n"];
		 *
		 **/

		LogST.logP(0, "*****************************************************************************");

		StringBuffer line = new StringBuffer(1024);
		
		if (nameConsumerGraph != null) openGraphFile(nameConsumerGraph+"_"+phase+".dot");
		
		// DOT preamble
		line.append("digraph ConsumerGraph_"+graphName+"_"+phase+" {\n"+
				"sep=\"+25.0,+100.0\";\n"+"overlap=scalexy;\n"+" node [shape=record];\n"+
				"edge [headport=\"n\"];");	
		
/*		LogST.logP(0,"digraph ConsumerGraph_"+graphName+"_"+phase+" {\n"+
				"sep=\"+25.0,+100.0\";\n"+"overlap=scalexy;\n"+" node [shape=record];\n"+
				"edge [headport=\"n\"];"); // this is our DOT preamble
*/		
		emitGraphLine(line); // will also do the Logging and reset the buffer
		
		Collection<MatrixModule> allModules = inst.moduleTable.values();

		// emit the list of nodes
		for (MatrixModule m : allModules) {
			int pos = m.getClass().toString().lastIndexOf('.');
			line.append(cleanUpName(m.name) + " [label =\"{{\\N}|{"+m.getClass().toString().substring(pos+1) +"}}\"];");
			emitGraphLine(line);
		}	

		// now the edge lists, both regular and logical edges
		for (MatrixModule m : allModules) {
			List <MatrixModule> l = m.getConsumers();
			String mName = cleanUpName(m.name);
			for (MatrixModule m2 : l)
			{
				line.append(mName + " -> "+ cleanUpName(m2.name) +";");
				emitGraphLine(line);
//				LogST.logP(0,mName + " -> "+ cleanUpName(m2.name) +";");
			}

			// links which are logical dependencies (e.g. temp file I/O)
			l = m.getLogicalConsumers();
			if (! (l==null)) {  // careful : this arrayList defaults to null
				for (MatrixModule m2 : l)
				{
//					LogST.logP(0,mName + " -> "+ cleanUpName(m2.name) +" [ style = \"dotted\" ];");
					line.append(mName + " -> "+ cleanUpName(m2.name) +" [ style = \"dotted\" ];");
					emitGraphLine(line);
				}
			}
		}
//		LogST.logP(0,"}");
		line.append("}");
		emitGraphLine(line);
		if (nameConsumerGraph != null) 	closeGraphFile();

		LogST.logP(0, "*****************************************************************************");

	}

	/**
	 * Clean up node names for DOT printing. Purges a module name from ASCII
	 * dots and other symbols embedded in it, also checks for a few substrings.
	 * Given syntax and file conventions, it is likely but there is
	 * unfortunately no guarantee that the node names will end up being unique.
	 * 
	 * @return
	 */
	private static String cleanUpName (String s) {
		s = s.replaceAll("-", "_");
		s = s.replaceAll("\\.csv", "_DOT_csv");
		s = s.replaceAll("\\.txt", "_DOT_txt");
		s = s.replaceAll("\\.", "_DOT_");
		return s; 
	}

	static FileWriter graphFileDesc;
	
	/**
	 * Write a graph line to the logs and possibly to the proper Graph file.
	 * @param sb
	 */
	private static void emitGraphLine (StringBuffer sb) {
		LogST.logP(0,sb.toString());
		if (graphFileDesc != null)
		{
			try {
				sb.append("\n");
				graphFileDesc.write(sb.toString());
			} catch (IOException e)
			{
				LogST.logP(0, "ERROR : emitGraphLine() could not write");
				LogST.logException(e);
				graphFileDesc=null; //forget it
			}
		}
		sb.setLength(0);
	}
	
	private static void openGraphFile (String name) {
		
//		LogST.logP(1, "openGraphFile("+name+")");
		try {
			graphFileDesc = new FileWriter(name);
		} catch (IOException e)
		{
			LogST.logP(0, "ERROR : openGraphFile() could not open file for writing "+name);
			LogST.logException(e);
			graphFileDesc=null;
			dumpConsumerGraph=true; // if we cannot save at all, dump the graph in the logs
 		}
	}
	
	private static void closeGraphFile() {
		if (graphFileDesc != null)
			try {
				graphFileDesc.close();
			} catch (IOException e)
			{
				LogST.logP(0, "ERROR : closeGraphFile() got exception");
				LogST.logException(e);
				graphFileDesc=null;
			} finally {
				graphFileDesc = null;
			}
	}

	/**
	 * Find the output modules at the level of scripting managed by this
	 * instance of TheMatrixSys; this function only returns the output modules
	 * added to the script by the user, distinguishing the terminal ones from
	 * those which have a following module reading their output.
	 * 
	 * The method ignores modules added by postprocessing (e.g. to-disk
	 * buffers). The list is used to pull the output at script execution time,
	 * and to connect the FileOutputs of nested scripts.
	 * 
	 * @param terminal
	 *            true if we want terminal nodes (nothing pulling their output); false if we want modules which already have consumers.
	 * 
	 * @return List of output modules
	 */
	public static List<MatrixFileOutput> getUserOutputModules(boolean terminal)
	{
		List <MatrixFileOutput> localListOM = new ArrayList<MatrixFileOutput>();
		for (MatrixModule m1 : inst.moduleTable.values()) {
			if (m1 instanceof MatrixFileOutput
					&& !((MatrixFileOutput) m1).isPostProcessed())
				// only if the (non) terminal ones, as required 
				// terminal module == no one consumes from this module
				if (terminal == m1.getConsumers().isEmpty() )
					localListOM.add((MatrixFileOutput)m1); 
		}
		return localListOM;
	}

	/**
	 * Find the output modules at the level of scripting managed by this
	 * instance of TheMatrixSys; this method returns all output modules
	 * added to the script by the user, but ignores modules added by postprocessing (e.g. to-disk
	 * buffers). 
	 * 
	 * @return List of output modules
	 */
	public static List<MatrixFileOutput> getUserOutputModules()
	{
		List <MatrixFileOutput> localListOM = new ArrayList<MatrixFileOutput>();
		for (MatrixModule m1 : inst.moduleTable.values()) {
			if (m1 instanceof MatrixFileOutput
					&& !((MatrixFileOutput) m1).isPostProcessed())
				localListOM.add((MatrixFileOutput)m1); 
		}
		return localListOM;
	}
	
	/*******************************************************************/
	/********************************************************************************************
	 * THIS PART OF THE CLASS SHALL BE MOVED TO THE ModuleGraphPostprocessor class
	 *  
	 * END
	 ********************************************************************************************/
	
	/**
	 * Method to extract detailed script location information concerning a
	 * parsing problem from an an ASTNode object of the Neverlang interpreter.
	 * The parameter is allowed to be null or invalid (there are cases where the
	 * interpreter cannot detect the ASTNode at fault) and any exception within
	 * this method is mapped to a default message.
	 * 
	 * @param n
	 *            the node where the interpreter parser encountered the problem,
	 *            or null
	 * @return a message explaining either where the error happened, or that
	 *         information is not available
	 */
	private static String getInfo(ASTNode n) {
		try {

			ASTNode nn = n.ntchild(0);

			String moduleId = nn.getValue("moduleId");
			Integer row = nn.getValue("row");
			Integer col = nn.getValue("col");
			return String.format("module: %s, row: %d, col: %d", moduleId, row,
					col);
		} catch (RuntimeException ex) {
			return "Unknown module/location";
		}
	}



	
	/**
	 * Function to parse command-line arguments with jopts-simple.
	 * 
	 * Process command-line parameters (forwarded by main()). Inserts
	 * information in the Dynamic singleton. Throw an error, and do not return
	 * at all if any arguments are malformed. Call usage() as appropriate,
	 * possibly with exit(0) on --help. <br>
	 * 
	 * --configPath absolute path to the main configuration file; UNIMPLEMENTED <br>
	 * 
	 * --help describe usage <br>
	 * --scriptPath <directory name> custom set the path for script execution,
	 * relative path to the main TheMatrix directory<br>
	 * --iadPath <directory name> custom set the path for the IAD files,
	 * relative path to the main TheMatrix directory<br>
	 * --resultPath <directory name> custom set the path for the result files,
	 * relative path to the main TheMatrix directory<br>
	 * --loglevel <integer> sets the default log level to a value between 0
	 * (minimum verbosity) and 3 (maximum)<br>
	 * --dry-run do not compute; do not call the actual compute(), only perform
	 * the setup() of modules; passed to nested interpreters. Note that this is
	 * a partial implementation: will skip pulling the output from the module
	 * chain in main(), but some modules (e.g. sort) and nested scripts will
	 * still try to run. See main() for more information.<br>
	 * --fullIADschema <integer> use the full IAD schema definition if parameter
	 * is not zero. Defaults to 0 during the alpha and beta test.<br>
	 * --ignoreDBconnection disable DBMS access, will only generate the SQL
	 * queries and dump them as text files <br>
	 * --dumpMappingSchema dump to System.out a template of the XML file for
	 * mapping the current schemata to a DBMS. Also enforces a --dry-run.<br>
	 * --MappingTestRun perform the retrieval of all IAD files defined in the
	 * mapping.xml configuration UNIMPLEMENTED<br>
	 * 
	 * First unrecognized option is the file name of the script to execute.
	 * 
	 * TODO Additional arguments after the script name are accepted but
	 * currently ignored (will become the script parameters).
	 * 
	 * @param args
	 */
	private static void readCommandLine(String[] args) {

		boolean dropToHelp = false; // in case of option errors, print help and
									// exit

		OptionParser parser = new OptionParser();
		parser.accepts("help", "describe command-line options and quit")
				.forHelp();
		parser.accepts("scriptPath", "the path to the scripts directory")
				.withRequiredArg().describedAs("path");
		parser.accepts("iadPath", "the path to the directory of IAD data files")
				.withRequiredArg().describedAs("path");
		parser.accepts("resultPath", "the path to the directory for produced files")
				.withRequiredArg().describedAs("path");
		parser.accepts("logLevel",
				"sets the default log level (3 is most verbose)")
				.withRequiredArg().describedAs("0,1,2,3");
		parser.accepts("dry-run",
				"Check script syntax and print diagnostics, do not execute");
		parser.accepts("fullIADschema",
				"Select the IAD schema revision for this run")
				.withRequiredArg().describedAs("0,1,2");
		parser.accepts(
				"ignoreDBconnection",
				"do not try to open a DBMS connection, just dump SQL queries if a mapped file is missing");
		parser.accepts("dumpMappingSchema",
				"dump to console the current IAD mapping schema and quit");
		parser.accepts("MappingTestRun",
				"UNSUPPORTED - If present, only test DBMS mapping and quit");
		parser.accepts("dumpConsumerGraph",
				"dump to console and log the script module graph as represented by consumer lists");
		
		parser.accepts("saveConsumerGraph",
				"save the script graph in all postprocessing phases as a dot file")
				.withRequiredArg()
				.describedAs("base filename of script graphs");
		
		parser.nonOptions("name of the script to run (mandatory), optional arguments (UNSUPPORTED)");

		/**
		 * parse POSIX-style, we avoid the mixing of options and other
		 * parameters as it may confuse the user
		 **/
		parser.posixlyCorrect(true);
		OptionSet options = parser.parse(args);

		LogST.logP(1, "Command line arguments :" + Arrays.toString(args));

		if (options.has("help"))
			usage(parser);

		/*** set the script directory path */
		if (options.has("scriptPath")) {
			// path must be next argument
			Dynamic.setScriptPath((String) options.valueOf("scriptPath")); 
		}

		/*** set the iad directory path */
		if (options.has("iadPath")) {
			// path must be next argument
			//TODO TEST IT
			Dynamic.setIadPath((String) options.valueOf("iadPath")); 
			LogST.logP(0, "--iadPath is experimental : value was \""+options.valueOf("iadPath")+"\"");
		}

		/*** set the result directory path */
		if (options.has("resultPath")) {
			// path must be next argument
			//FIXME UNINMPLEMENTED
			//			Dynamic.setResultPath((String) options.valueOf("resultPath")); 
			LogST.logP(0, "--resultPath unimplemented : value was \""+options.valueOf("resultPath")+"\"");
		}

		/*** --logLevel */
		if (options.has("logLevel")) {
				String s = ((String) options.valueOf("logLevel")).trim();
				int temp = "0123".indexOf(s.charAt(0));
				if (s.length()==1 && temp != -1 ) {// it is log level
					Dynamic.loggingLevel = temp;
				} else {
				System.out.println("Incorrect parameter for the --logLevel option");
				dropToHelp = true; 
			}
		}

		/*** --dry-run disables pulling output(s) from the module chain */
		if (options.has("dry-run"))
			Dynamic.dryRun = true;

		/***
		 * --fullIADschema select a specific revision of IAD from the ones
		 * available
		 */
		if (options.has("fullIADschema")) {
			String s = ((String) options.valueOf("fullIADschema")).trim();
			int temp = "012".indexOf(s.charAt(0));
			if (s.length()==1 && temp != -1 ) {// 0,1,2 accepted
				if (temp == 0)
					Dynamic.versionOfIAD = selectedIADVersion.PreliminaryIAD;
				if (temp == 1)
					Dynamic.versionOfIAD = selectedIADVersion.PartialIAD;
				if (temp == 2)
					Dynamic.versionOfIAD = selectedIADVersion.FullIAD2013;
			} else {
				System.out.println("Incorrect parameter for the --fullIADschema option");
				dropToHelp = true; 
			}
		}

		/***
		 * --ignoreDBconnection sets a flag that will cause SQL queries always
		 * to be saved to a file
		 */
		if (options.has("ignoreDBconnection"))
			Dynamic.ignoreDBConnection = true;

		if (options.has("dumpMappingSchema"))
		{	Dynamic.dumpMapping = true;

			// we also set this to avoid further processing
			Dynamic.dryRun = true; 
		}

		if (options.has("MappingTestRun")){
			Dynamic.doLimitQueryResults = true;
			LogST.logP(0,
					"Option --MappingTestRun is currently unsupported");
			dropToHelp = true; 
		}

		/** dumpConsumerGraph **/
		if (options.has("dumpConsumerGraph")) {
			dumpConsumerGraph = true;
		}

		/** saveConsumerGraph **/
		if (options.has("saveConsumerGraph")) {
			nameConsumerGraph = ((String) options.valueOf("saveConsumerGraph")).trim();
			LogST.logP(0, "processing saveConsumerGraph: "+nameConsumerGraph);
			// we just trust this produces a correct file name once properly processed
			if (nameConsumerGraph.length()==0)
			{
				System.out.println("Incorrect parameter for the --saveConsumerGraph option: "+nameConsumerGraph);
				dropToHelp = true; 
			}
		}
		
/*		LogST.logP(0,"TEST PARSER is mapping test run "+ options.has("MappingTestRun"));
		LogST.logP(0,"TEST PARSER is ignoring DBMS "+ options.has("ignoreDBconnection"));
		LogST.logP(0,"TEST PARSER script path is " + options.valueOf("scriptPath"));
		LogST.logP(0,"TEST PARSER is dry run? " + options.has("dry-run"));
		LogST.logP(0,"TEST PARSER uses IAD schema "+ options.valueOf("fullIADschema"));
		LogST.logP(0,"TEST PARSER is dumpMapping only? "+ options.has("dumpMappingSchema"));
		LogST.logP(0,"TEST PARSER non options are " + options.nonOptionArguments());
*/

		/** script filename, must be present **/
		if (options.nonOptionArguments().isEmpty()) {
			System.out.println("Missing script name");
			dropToHelp = true;
		} else {
			Dynamic.scriptFileName = (String) options.nonOptionArguments().get(0);
		}		
		
		/** parameters of the script **/
		// first argument is the name of the script, so we start from 1
		if (options.nonOptionArguments().size() > 1) 
		{
			List<String> list = new ArrayList<String>();
			for (int i=1; i<options.nonOptionArguments().size(); i++)
			{
				String item = (String) options.nonOptionArguments().get(i);
				list.add(item);
			}

			LogST.logP(1, "Arguments used as script parameters :" + list.toString());
			setArguments(list);
		} else 
			LogST.logP(1, "No script parameters");

		if (dropToHelp) 
			usage(parser);
	}

	/**
	 * Print command line help for program usage, does never return, exploits jopt-simple parser.
	 */
	private static void usage(OptionParser parser) {
		try {
			System.out
					.println("------     TheMatrix comand line help     ------\n");
			parser.printHelpOn(System.out);
			System.out.println("\n");
		} catch (IOException e) {
			// whatdowedo
			System.exit(255);
		} 
		System.exit(0);
	}

	public static void evalFile(String fpath) throws IOException {
		eval(FileUtils.fileToString(fpath));
	}

	public static void eval(String src) {
		matrixLang.interpret(src);
	}

	/**
	 * Method to initialize a number of symbol tables for script execution,
	 * called at system init time. Initialization is called within the static
	 * block of the class; adds definitions for default schemata, filtering
	 * operators, apply-able functions. All actual definitions are in specific
	 * methods in this class and in OperatorDefinitions .
	 * 
	 * TODO move following comment to the proper method/class
	 * 
	 * Schema definitions: IADperson, custom, IADhosp, IADoutpat, IADdrug,
	 * IADexe <br>
	 * Operator definitions: equality for booleans, all comparison ops (6: > <
	 * >= <= = !=) for both types INT and DATE, equality for type STRING. These
	 * operators deal with missing values using the same semantic as STATA (to
	 * ease testing compatibility) where missing integers and dates are mapped
	 * to the highest/farthest future possible value. Note that since dates are
	 * compared using their representation in seconds, two dates in the same day
	 * can be different because of the time of the day. <br>
	 * The semantics for missing boolean values is also the same as STATA; here
	 * missing values are read as true; in general, any int != 0 is a boolean
	 * true value (C style) -- to be handled in the csv parsing routine.<br>
	 * TODO check with CSV reading that this is obeyed. <br>
	 * TODO add string inequality operators; add more operators for boolean?
	 * TODO list the functions defined, possibly extend the function list.<br>
	 * TODO add test unit to check the operators.<br>
	 * 
	 * @author edoardovacchi, massimo
	 */
	public static void prepareSystem() {
		// initialize opTable and schemas
		/*************
		 * Default schema initialization code hand-edited from
		 * code_fragment_generator/code.java
		 *************/

		SchemaTable predefinedSchemata = TheMatrixSys.getPredefinedSchemata();

		/*
		 * use IAD schema definitions from a separate class, here we only deal
		 * with the preliminary schemas used during development, with data
		 * provided by ARS
		 */
		if (Dynamic.versionOfIAD != selectedIADVersion.PreliminaryIAD) {
			TheMatrixIADDefinition.defineIADFullSchemas();
		} else { // preliminary definitions in a separate method of this class
			definePreliminaryIADSchemata();
		}

		/******
		 * default filter operator definitions
		 ******/
		OperatorDefinitions.defineFilterOperators();

		/*****
		 * Default operator functions
		 *****/
		OperatorDefinitions.defineFunctionOperators();

	}

	/**
	 * Auxiliary function defining the first development version of IAD. The
	 * following schemata are NOT those from the agreed IAD definitions, they
	 * match the quasi-IAD schema of the test data provided by ARS; a few
	 * spurious fields are present and many IAD fields are still missing.
	 */
	static void definePreliminaryIADSchemata() {

		// setup the schema for PERSON
		/**
		 * DatasetSchema schema = predefinedSchemata.create("IADperson");
		 * 
		 * schema.put(new Symbol<String>("PERSON_ID", "", DataType.STRING));
		 * schema.put(new Symbol<Date>("DATE_OF_BIRTH", DataType.DATE));
		 * schema.put(new Symbol<Date>("GENDER_CONCEPT_ID", DataType.INT ));
		 * schema.put(new Symbol<Date>("DATE_OF_DEATH", DataType.DATE));
		 * schema.put(new Symbol<Date>("ENDDATE", DataType.DATE));
		 * schema.put(new Symbol<Date>("STARTDATE", DataType.DATE));
		 * schema.put(new Symbol<Integer>("GP_ID", DataType.INT));
		 * schema.put(new Symbol<Integer>("LOCATION_CONCEPT_ID", DataType.INT));
		 */
		predefinedSchemata
				.create("IADperson")
				// many missing fields, 8 here out of 22
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING))
				.put(new Symbol<Date>("DATE_OF_BIRTH", null, DataType.DATE))
				.put(new Symbol<Integer>("GENDER_CONCEPT_ID", null,
						DataType.INT))
				.put(new Symbol<Date>("DATE_OF_DEATH", null, DataType.DATE))
				.put(new Symbol<Date>("ENDDATE", null, DataType.DATE))
				.put(new Symbol<Date>("STARTDATE", null, DataType.DATE))
				.put(new Symbol<Integer>("GP_ID", null, DataType.INT))
				.put(new Symbol<Integer>("LOCATION_CONCEPT_ID", null,
						DataType.INT));

		// empty schema
		predefinedSchemata.create("custom");

		// schema for HOSP
		/**
		 * predefinedSchemata.create("IADhosp") .put(new
		 * Symbol<String>("MAIN_DIAGNOSIS", null, DataType.STRING)) .put(new
		 * Symbol<String>("START_DATE", null, DataType.DATE));
		 */
		predefinedSchemata
				.create("IADhosp")
				// lots of missing fields : 20 here out of 31, plus 2 incorrect
				// fields
				.put(new Symbol<String>("PATIENT_ID", null, DataType.STRING))
				.put(new Symbol<String>("START_DATE", null, DataType.DATE))
				.put(new Symbol<String>("END_DATE", null, DataType.DATE))
				.put(new Symbol<String>("WARD_DISCHARGE", null, DataType.STRING))
				.put(new Symbol<String>("TYPE_DISCHARGE", null, DataType.STRING))
				.put(new Symbol<String>("MAIN_DIAGNOSIS", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_1", null,
						DataType.STRING))
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_2", null,
						DataType.STRING))
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_3", null,
						DataType.STRING))
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_4", null,
						DataType.STRING))
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_5", null,
						DataType.STRING))
				.put(new Symbol<String>("DATE_MAIN_PROC", null, DataType.DATE))
				.put(new Symbol<String>("MAIN_PROC", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_PROC_1", null,
						DataType.STRING))
				.put(new Symbol<String>("SECONDARY_PROC_2", null,
						DataType.STRING))
				.put(new Symbol<String>("SECONDARY_PROC_3", null,
						DataType.STRING))
				.put(new Symbol<String>("SECONDARY_PROC_4", null,
						DataType.STRING))
				.put(new Symbol<String>("SECONDARY_PROC_5", null,
						DataType.STRING))
				.put(new Symbol<String>("TYPE", null, DataType.STRING))
				.put(new Symbol<String>("REGIME", null, DataType.STRING))
				.put(new Symbol<String>("ANPRAT", null, DataType.STRING)) // local
																			// ARS,
																			// to
																			// be
																			// removed
				.put(new Symbol<String>("NPRAT", null, DataType.STRING)); // local
																			// ARS,
																			// to
																			// be
																			// removed

		// schema for OUTPAT
		/**
		 * predefinedSchemata.create("IADoutpat") .put(new
		 * Symbol<String>("PatientID", null, DataType.STRING)) .put(new
		 * Symbol<Date>("PROC_DATE", DataType.DATE)) .put(new
		 * Symbol<String>("PROC_COD", null, DataType.STRING));
		 **/
		predefinedSchemata
				.create("IADoutpat")
				// many missing fields, 6 here out of 21
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING))
				.put(new Symbol<String>("GROUP_CODE", null, DataType.STRING))
				.put(new Symbol<String>("PROC_COD", null, DataType.STRING))
				.put(new Symbol<String>("PROC_START_DATE", null, DataType.DATE))
				.put(new Symbol<String>("PROC_END_DATE", null, DataType.DATE))
				.put(new Symbol<String>("COST", null, DataType.FLOAT))
		// the right name seems to be VALUE, but cost is in the CSV header
		// however, the xls reports VALUE as INT< while here we have floating
		// point values;
		// .put(new Symbol<String>("VALUE", null, DataType.FLOAT));
		;

		// schema for DRUG
		/**
		 * predefinedSchemata.create("IADdrug") .put(new
		 * Symbol<String>("PatientID", null, DataType.STRING)) .put(new
		 * Symbol<String>("ATC", null, DataType.STRING)) .put(new
		 * Symbol<Date>("DRUG_EXPOSURE_START_DATE", DataType.DATE));
		 */
		predefinedSchemata
				.create("IADdrug")
				// REG_PROVIDER_ID
				// LHU_PROVIDER_CONCEPT_ID
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING))
				.put(new Symbol<String>("DRUG_EXPOSURE_START_DATE", null,
						DataType.DATE))
				.put(new Symbol<String>("PRODUCT_CODE", null, DataType.STRING))
				.put(new Symbol<String>("NUMBER_OF_BOXES", null, DataType.INT))
				.put(new Symbol<String>("ATC", null, DataType.STRING))
				.put(new Symbol<String>("DURATION", null, DataType.FLOAT))
				// .put(new Symbol<String>("COST", null, DataType.INT));
				// same type mismatch as in hosp, COST is actually a FLOAT
				.put(new Symbol<String>("COST", null, DataType.FLOAT));

		// schema for EXE
		/**
		 * predefinedSchemata.create("IADexe").put(new
		 * Symbol<String>("PERSON_OD", null, DataType.STRING)) .put(new
		 * Symbol<String>("EXEMPTION_CODE", null, DataType.STRING)) .put(new
		 * Symbol<Date>("EXE_START_DATE", DataType.DATE)) .put(new
		 * Symbol<Date>("EXE_END_DATE", DataType.DATE));
		 */
		predefinedSchemata
				.create("IADexe")
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING))
				.put(new Symbol<String>("EXEMPTION_CODE", null, DataType.STRING))
				.put(new Symbol<String>("EXE_START_DATE", null, DataType.DATE))
		// .put(new Symbol<Date>("EXE_END_DATE", null, DataType.DATE));
		;// EXE_END_DATE

		/**********************************************************************/
		/**
		 * schemata used while testing
		 */
		predefinedSchemata
				.create("TestSchemaX")
				// EXE_END_DATE
				.put(new Symbol<String>("KEY", null, DataType.INT))
				.put(new Symbol<String>("ATTR_X", null, DataType.STRING))
				.put(new Symbol<String>("ATTR_X2", null, DataType.STRING));

		predefinedSchemata
				.create("TestSchemaY")
				// EXE_END_DATE
				.put(new Symbol<String>("EXTKEY", null, DataType.INT))
				.put(new Symbol<String>("ATTRIBUTEY", null, DataType.STRING))
				.put(new Symbol<String>("ATTRIBUTEY2", null, DataType.STRING));

		predefinedSchemata.create("AggregateTestSchema")
				.put(new Symbol<Integer>("ID", null, DataType.INT))
				.put(new Symbol<Integer>("REPEATED_INT", null, DataType.INT))
				.put(new Symbol<Integer>("INT", null, DataType.INT));

	}

}