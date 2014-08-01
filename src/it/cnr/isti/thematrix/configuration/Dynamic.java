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
package it.cnr.isti.thematrix.configuration;

import it.cnr.isti.thematrix.common.Enums.CompressionType;

import java.io.File;

/***
 * This class is here to hold dynamically generated configuration information
 * that we want to treat more or less like a singleton. Default values, updated
 * with information from program launch parameter may be stored here. In some
 * specific methods (e.g. parseTestQuerySizeLimit() ) we need to access the
 * other singletons, whose initialization <b>may depend</b> on this class (e.g.
 * getting to the default directory to read configuration files). In those
 * cases, the related method of Dynamic <b>must not</b> be called before the
 * other singletons were initialized. This class shall be used to set up
 * run-time parameters within unit tests
 * 
 * FIXME check this singleton, should it really have those static fields with
 * unguarded accessor methods?
 * 
 * FIXME add code to detect use of uninitialized fields related to other
 * singletons in order to prevent the issue.
 * 
 * 
 * @author massimo
 * 
 */
public class Dynamic {

	static private Dynamic instance;

	/**
	 * FIXME will contain the absolute path to use for accessing the main config
	 * and data files (in their respective subdirs). Currently a duplicate of
	 * the same var in ConfigSingleton.
	 */
	static private String basePath;

	/**
	 * More fields to hold the ready-made path we will occasionally need. These
	 * paths are ready for use in a <code>File(String, String)</code> method
	 * call.<br>
	 * TODO complete code refactoring in the future, to allow execution
	 * independently of the current dir.
	 */
	static private String scriptPath;
	static private String iadPath;
	static private String resultsPath;

	/**
	 * true if we are on a test run, and all query results shall be bounded in
	 * size
	 */
	static public boolean doLimitQueryResults;

	/**
	 * stores the size limit (see XML config file)
	 */
	static private Integer testQuerySizeLimit;

	/**
	 * When true, tells the CSV manager to not perform checksum of whole files.<br>
	 * TODO Currently unsafe, useful for tests and for very large files.
	 */
	// / static public boolean skipCSVFullChecksum;

	/**
	 * True if we want to always dump the SQL queries to disk as text files,
	 * ignoring the DB connection which may allow to execute them.
	 */
	static public boolean ignoreDBConnection = false;

	/**
	 * true if we want to prefetch a batch from CSV data files as soon as we
	 * open them in Module setup(). TODO should maybe distinguish between CSV
	 * and nested script results
	 */
	static public boolean prefetchCSVFilesOnSetup = true;

	/**
	 * batch size in rows for opening CSV files when reading. FIXME unify with
	 * other similar constants
	 */
	static public int prefetchCSVSize = 20000;

	/**
	 * batch size in rows for writing into CSV files FIXME unify with other
	 * similar constants
	 */
	static public int writeCSVBatchSize = 20000;

	/**
	 * Enable result caching: script result files are retained in cache;
	 * defaults to true. TODO unimplemented, so for now is set to false
	 */
	static public boolean cachingEnabled = false;

	/**
	 * If true, the TheMatrix routines will not delete any temporary files
	 * generated; does not apply to result file sin cache; does not apply to
	 * Jerboa-generated temporary files. TODO check that this is obeyed by all
	 * routines.
	 */
	static public boolean keepTemporaryFiles = false;

	/**
	 * true if we want to prefetch a batch of rows from a DBMS as soon as we
	 * open them in Module setup(). <br>
	 * TODO check implementation
	 */
	static public boolean prefetchDBDataOnSetup = false;

	/**
	 * Default logging level {L: 0<=L<=3} as set by command line. <br>
	 * FIXME Current static default is 3.
	 */
	public static int loggingLevel = 3;

	/**
	 * Current script file name to execute, no path. Used for convenience in
	 * reading parameters.
	 */
	public static String scriptFileName = null;

	/**
	 * If true, skip most of the computation by running only the script parsing
	 * and the module setup phases of the interpreter. <br>
	 * FIXME expected behaviour currently not implemented for nested
	 * interpreters and sort modules
	 */
	public static boolean dryRun = false;

	/**
	 * Enable use of the full schema definition for IAD tables, possibly new
	 * revisions. Defaults to PreliminaryIAD, the schemas used during the
	 * testing phase as provided by ARS. PartialIAD is the reduced IAD, the IAD
	 * schema we are telling the tester to map. Third will be full IAD at the
	 * program release.
	 */
	public enum selectedIADVersion {
		PreliminaryIAD, PartialIAD, FullIAD2013
	};

	public static selectedIADVersion versionOfIAD = selectedIADVersion.PartialIAD;

	/**
	 * set to true in order to dump the current schema definitions to an XML
	 * template for the mapping file
	 **/
	public static boolean dumpMapping = false;

	/**
	 * Size of creation for BufferModules used for stream branching scripts, in
	 * DatasetRecord units; it must be a power of two and qit is currently fixed
	 * at launch time.
	 */
	public static int multiReaderBufferSize = 1 << 15;

	/**
	 * If true, enables memory buffers for multiple modules reading from the
	 * same stream; if false, force a disk buffer (no size limits). By default
	 * use disk buffers, as the memory implementation is not easily made
	 * compatible with the SortModule.
	 */
	public static boolean useMultiReaderMemoryBuffers = false;

	/**
	 * Static field for compression of buffer files
	 */
	public static CompressionType bufferCompression = CompressionType.NONE; // .ZIP;
	// FIXME disabled, needs a global switch to set it up
	
	/**
	 * true if we are on a 64bit JVM; only valid for Sun JVM (we do not support
	 * other ones!)
	 */
	private boolean jvm64bit;


	/**************** singleton pattern implementation methods ****************/
	public static Dynamic getDynamicInfo() {
		if (instance == null)
			instance = new Dynamic();
		return instance;
	}

	private Dynamic() {
		doLimitQueryResults = false;
		// skipFullChecksum=false; /// TBD
		testQuerySizeLimit = 0;
		
		/*
		 * check what JVM we are running; this test _intentionally_ only works
		 * on Sun/Oracle JVM
		 */
		String property = System.getProperties().getProperty("sun.arch.data.model");
		if (property != null) jvm64bit = (property == "64");
	}

	/**************** access methods **********************/

	public int getTestQuerySizeLimit() {
		return testQuerySizeLimit;
	}

	/**
	 * this function reads the query size limit from settings.xml. NEEDS to have
	 * the config singleton up. TODO check that we do not have a circular
	 * dependency. TODO we WILL have it if I put the base directory information
	 * in Dynamic
	 */
	public void parseTestQuerySizeLimit() throws IllegalStateException {
		if (!ConfigSingleton.configured())
			throw new IllegalStateException(
					"Dynamic.parseTestQuerySizeLimit() : attempted ConfigSingleton access before its inizialization");

		testQuerySizeLimit = ConfigSingleton.getInstance().theMatrix
				.getTestQuerySizeLimit();
		if (testQuerySizeLimit == 0)
			doLimitQueryResults = false;
		else {
			doLimitQueryResults = true;
			// enforce max/min values
			if (testQuerySizeLimit < 10)
				testQuerySizeLimit = 10;
			if (testQuerySizeLimit > 1000000)
				testQuerySizeLimit = 1000000;
		}
	}

	/**
	 * Read the kind of compression from settings, set the value in dynamic.
	 * Accepts (case insensitive) all valid CompressionType values as well as
	 * empty string and the "no" string as synonym of no compression.
	 * 
	 * FIXME needs to be merged with parseTestQuery and similar methods.
	 * 
	 * @throws IllegalStateException
	 */
	public void parseBufferCompression() throws IllegalStateException {
		if (!ConfigSingleton.configured())
			throw new IllegalStateException(
					"Dynamic.parseBufferCompression() : attempted ConfigSingleton access before its inizialization");
		String xmlBufferCompression = (ConfigSingleton.getInstance().theMatrix
				.getBufferCompression()).trim();
		try {
			if (xmlBufferCompression.length() == 0
					|| xmlBufferCompression.equalsIgnoreCase("no"))
				bufferCompression = CompressionType.NONE;
			else {
				CompressionType t = CompressionType
						.valueOf(xmlBufferCompression.toUpperCase());
				bufferCompression = t;
			}
		} catch (Exception e) {
			LogST.logP(
					-1,
					"parseBufferCompression() error in settings.xml <BufferCompression> missing/unrecognized value, ignoring it.");
			bufferCompression = CompressionType.NONE;
		}
	}

	/******************************* file path methods ******************************************/

	/**
	 * Set up the base path for Dynamic.
	 * 
	 * TODO add error checking, or call here the sanity checks from Test.
	 */
	static public void setBasePath(String p) {
		if (p.endsWith(File.separator))
			basePath = p;
		else
			basePath = p + File.separator;
	}

	/**
	 * Set up the TheMatrix script path.
	 * 
	 * TODO add error checking, or call here the sanity checks from Test.
	 */
	static public void setScriptPath(String p) {
		if (p.endsWith(File.separator))
			scriptPath = p;
		else
			scriptPath = p + File.separator;
	}

	/**
	 * Set up the TheMatrix iad path.
	 * 
	 * TODO add error checking, or call here the sanity checks from Test.
	 */
	static public void setIadPath(String p) {
		if (p.endsWith(File.separator))
			iadPath = p;
		else
			iadPath = p + File.separator;
	}

	/**
	 * Set up all paths for Dynamic with default values relative to basePath. Do
	 * not overwrite those which are not null, because set from command line.
	 * 
	 * TODO maybe we should do something like<br>
	 * <code>String joinedPath = new File(path1, path2).toString();</code>
	 * 
	 * @return
	 */
	static public void setPaths() {
		// check that basePath is not null?
		if (scriptPath == null)
			scriptPath = basePath + "scripts" + File.separator;
		if (iadPath == null)
			iadPath = basePath + "iad" + File.separator;
		if (resultsPath == null)
			resultsPath = basePath + "results" + File.separator;
	}

	static public String getBasePath() {
		return basePath;
	};

	static public String getScriptPath() {
		return scriptPath;
	};

	static public String getIadPath() {
		return iadPath;
	};

	static public String getResultsPath() {
		return resultsPath;
	};

	static public String getLookupsPath() {
		return basePath+"lookups"+File.separator;
	};

	/************** other accessors **************/
	
	/**
	 * Returns the word size of the current JVM (32 or 64 bit).
	 * 
	 * @return
	 */
	public boolean isJvm64bit() {
		return jvm64bit;
	}
	
}
