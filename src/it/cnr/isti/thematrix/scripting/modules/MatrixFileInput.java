package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.common.Enums;
import it.cnr.isti.thematrix.common.Enums.CompressionType;
import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.MappingSingleton;
import it.cnr.isti.thematrix.exception.JDBCConnectionException;
import it.cnr.isti.thematrix.exception.SyntaxErrorInMappingException;
import it.cnr.isti.thematrix.exception.UnsupportedDatabaseDriverException;
import it.cnr.isti.thematrix.mapping.MappingManager;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;
import it.cnr.isti.thematrix.mapping.utils.TempFileManager;
import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import it.cnr.isti.thematrix.scripting.utils.DateUtil;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.filefilter.NameFileFilter;


/**
 * Class implementing the FileInputModule, which allows to read in IAD CSV files with assigned schema. <br>
 * This class relies on many classes developed when extending Jerboa into TheMatrix; the corresponding initialization
 * code must be called before we call the interpreter.
 * 
 * A constructor exists that specifies the input file is a temporary one, so no connection is allowed to the DBMS,
 * functions skip most of the related checks and initializations, and module will not report bogus errors. <br>
 * 
 * TODO hooked-in code to interact with the DBMS when a file is not found in the IAD directory, exploiting the code in
 * the MappingManager, Mapper and related classes. This will need some refinement and more error checking. If we cannot
 * download a file immediately (query generation) we should not allow the script to continue (exception is raised) but
 * we should make sure that ALL needed queries are generated. This is currently not true, we only generate the first one
 * we meet. Possible solution: encode a dev/null-like CSVFile that can be returned instead, returns a few empty rows
 * then closes. <br>
 * 
 * TODO workaround added for the file name, to address a small design issue of the syntax conversion; nothing that is
 * not a CSV file should get there, and the low-level name of the file should not in generally be fixed by the script. <br>
 * 
 * FIXME note that the most general constructors could have the last parameter as a hook to call if the file is not
 * found: to create it from the DB, or to pull the hasMore()/next calls of the writer of a temporary file (could it also
 * improve the SortModule ?). UNLIKELY Carefully consider when switching from disk based to memory based buffering.
 * 
 * 
 * @author edoardovacchi, massimo
 */
public class MatrixFileInput extends MatrixModule 
{
	private static final long serialVersionUID = 701236603521894984L;

	private final String inputFilename;
	private final String baseName;
	private final DatasetSchema inputSchema;
	private CSVFile inputCSV = null;
	private boolean fileIsTemporary = false;
	private CompressionType compression;
	// FIXME used only in toString() ?
	private final List<String> orderByList;  
	
	/**
	 * Constructor (chained to the most general one) which accepts a schema name.
	 * 
	 * @param name			name of module
	 * @param inputFilename name of the file to read
	 * @param inputSchema	name of the input schema
	 * @param orderByList	list of fields (redundant?)
	 */
	public MatrixFileInput(String name, String inputFilename, String inputSchema, List<String> orderByList) {
		this(name,inputFilename, TheMatrixSys.getPredefinedSchema(inputSchema), orderByList);
 	}

	/**
	 * 
	 * Constructor taking a DatasetSchema object instead of a schema name.
	 * 
	 * @param name			name of module
	 * @param inputFilename name of the file to read
	 * @param inputSchema	the input schema to be used for reading
	 * @param orderByList	list of fields (redundant?)
	 */
	public MatrixFileInput(String name, String inputFilename, DatasetSchema inputSchema, List<String> orderByList) {
		this(name, inputFilename, inputSchema, orderByList, false);
	}
	
	/**
	 * Most general constructor, that also allows to define the input file as a temporary one.
	 * 
	 * This constructor allows to specify the input as a temporary file: i.e. a file
	 * that is not possible to download from DBMS, and is allowed not to exist yet at module creation time.
	 * 
	 * 
	 * TODO check the RuntimeException, maybe use a subclass
	 *
	 * 
	 * @param name
	 *            name of module
	 * @param inputFilename
	 *            name of the file to read
	 * @param inputSchema
	 *            the input schema to be used for reading
	 * @param orderByList
	 *            list of fields (redundant?)
	 * @param flag
	 *            if true, the input file is a temporary.
	 */
	public MatrixFileInput(String name, String inputFilename, DatasetSchema inputSchema, List<String> orderByList, boolean flag) {
		super(name);
		this.inputFilename = inputFilename;
		this.baseName = Enums.getBaseNameFile(inputFilename);
		this.compression = Enums.parseCompressionExtension(inputFilename); // we do not check the file now!

//		this.compression = Enums.parseCompressionExtension(Dynamic.getIadPath(),inputFilename); // this will be null if file is not there

		// null = unsupported or missing file
		this.inputSchema = inputSchema;
		this.orderByList = orderByList;
		this.fileIsTemporary = flag;
	} 
	
	/**
	 * This is a Hack used for graph postprocessing. Returns the filename if it is a plain CSV file that the FileSorter
	 * routines (called by MatrixSort) can read, and if the file exists already. If the file is not plain, returns the Empty string. 
	 * 
	 * TODO Does not bother to check if the file is temporary or not, possibly unsafe?
	 * 
	 * TODO when we rewrite the access to CSV files in the sorting routine, the compression layer should be below it,
	 * and we can modify this routine to a simple filename getter method.
	 * 
	 * @return the filename, if it can be stolen, empty string otherwise.
	 */
	public String fileThatCanBeStolen() {
		File f;
		if (this.compression == CompressionType.NONE) 
		{
			f = new File(Dynamic.getIadPath()+inputFilename);
			if (f.exists() && f.length()>0)			
				return f.getAbsolutePath();
		}
		return "";
	}
	
	@Override
	public void setup() {
		this.setSchema(inputSchema);
		/**
		 * add data module initialization when first called by the interpreter  
		 * UNCLEAR/OBSOLETE COMMENT ???
		 */
		// log less important for temporary files
		LogST.logP(fileIsTemporary?2:1, "MatrixFileInput.setup() : "+this.toString());
	}

	/**
	 * Perform all actions to open a CSV file and allow reading from it via standard Module methods. Possibly download
	 * the data from DB or create a query for manual execution. The new openFile reacts to the instance variable
	 * fileIsTemporary; if it is true, then no DB download will be attempted. Performs check if the files is temporary
	 * and, if it's true, skip the check if file exist.
	 * 
	 * If the file is temporary, now it must exists and we check its compression type is the one we expected; if the
	 * file can be mapped via MappingManager and/or downloaded from DB, we will adapt to the compression type we find on
	 * disk.
	 * 
	 * 
	 */
	private void openFile() 
	
	{
		LogST.logP(0, "MatrixFileInput.openFile() -- Module: " + this.name);
		
		/******************************
		 * assert receivers == 1
		 * Check performed here in case wrong program graph postprocessing changes the list of receivers after setup().
		 */
		if (getReferenceCount() > 1) {
			LogST.logP(0, "MatrixFileInput, multiple consumers not supported");
			throw new Error ("MatrixFileInput "+this.name+": multiple consumers not supported");
		}
		/******************************/
		
		/*
		 * Open the specified file, initializing the inputCSV field.
		 * 
		 * Reference code is TheMatrix/Jerboa Mapper class <br>
		 * For now we will not deal here with file/value remapping and DBMS download; it will need to be connected via
		 * the MappingManager, but adding a simpler function which only works on the specific dataset
		 */
		
		MappingManager fileMapper = new MappingManager();
		boolean fileOK = false; // true if we found the CSV; (or, later, if downloaded it successfully)

//		boolean isTemporary = TempFileManager.isTemporary(Dynamic.getIadPath(), baseName + Enums.getFileExtension(Dynamic.bufferCompression));

		// now the file shall exist: so we can check its actual suffix
		this.compression = Enums.parseCompressionExtension(Dynamic.getIadPath(),inputFilename); // this will be null if file is not there
	
		
		// FIXME: why the heck we define a new isTemporary variable inside the method, if an instance
		// variable (fileIsTemporary) is there and not used?
		
		boolean isTemporary = compression!= null? 
				TempFileManager.isTemporary(Dynamic.getIadPath(), baseName + Enums.getFileExtension(compression)) : false;
				
		if (isTemporary)
			fileOK = true;
		else
		{
			try {
				File path = TempFileManager.getPathForFile(inputFilename);
				fileOK = fileMapper.checkCSVFileExistence(path.getAbsolutePath()+File.separator, baseName);
			} catch (Exception e) {
				LogST.logP(0, "MatrixFileInput.opneFile() exception " + e.toString() + " for file " + baseName);
				LogST.logException(e);
				throw new RuntimeException ("ERROR: MatrixFileInput() exception in mapping file "+baseName);
			};
			if (!fileOK) {
				fileOK = openFileDownloadFromDB(fileMapper);
			}
			// now the file SHALL exist, so let's detect its compression
			if (fileOK) {
				File path = TempFileManager.getPathForFile(inputFilename);
				this.compression = Enums.parseCompressionExtension(path.getAbsolutePath()+File.separator, inputFilename);
				if (compression == null) //OUCH
					throw new RuntimeException ("ERROR: MatrixFileInput() internal error");
			}
		}

		/*** 
		 * 3 cases now: 
		 * a) CSV found OR downloaded from DB -- all OK
		 * b) CSV not dowloaded, query generated  -- additional message to the user
		 * c) file not found and no download could be attempted, -- we already wrote on log
		 */
		
		if (fileOK) {
			// get the proper path and create csv iterator
			File path = TempFileManager.getPathForFile(inputFilename);
			inputCSV = new CSVFile(path.getAbsolutePath()+"/", baseName, "",this.compression);

			// provide it with the schema to enable data format checking
			inputCSV.setSchema(this.inputSchema);

			// load buffer; should check the header with the schema <<-- now it is
			inputCSV.loadBatch(Dynamic.prefetchCSVSize);
		}
		else if (!fileIsTemporary){ // here we handle case b)
			LogST.logP(0, "MatrixFileInput, DB download halted for file " + baseName);
		}
		
		/**
		 * FIXME deal with the case of the query not executed in a more user friendly way, see comment inside
		 * openFileDownloadFrmDB().
		 **/
	}

	/**
	 * Download CSV data from the DBMS in case a valid CSV file is not found but we have a DB mapping for that
	 * filename. 
	 * 
	 * TODO Maybe a file that is not downloadable should not reach here at all.
	 * 
	 * @param fileMapper class providing mapping information for DM-downloadable files
	 * 
	 * @return true if the file was successfully downloaded.
	 */
	private boolean openFileDownloadFromDB (MappingManager fileMapper ) {

		//		If we get here, we know the CSV file is not found or invalid
		boolean fileOK = false; // true if we were able to download the CSV, it becomes the return value
		Collection<String> mappedFileNames = null; // the list of names defined in the mapping.xml config
		boolean workDone = false; // true only after successful creation
	
		/************** new code -- interaction with the Database ******************/

		try { // check if the file belongs in those defined by our mapping
			mappedFileNames = MappingSingleton.getInstance().mapping.getDatasetNames();
		} catch (Exception e1) {
			LogST.logP(0, "MatrixFileInput.openFile() - ERROR - exception while reading the mapping file "+e1.toString());
			e1.printStackTrace();
		}

		if (mappedFileNames == null || !mappedFileNames.contains(baseName)) {
			LogST.logP(0, "MatrixFileInput.openFileDownLoadFromDB() - ERROR - file "+baseName+" has no mapping");
			throw new Error("MatrixFileInput.openFileDownLoadFromDB() No mapping for file");
			//			System.exit(0); // FIXME we should throw exception
		}

		// if it is there, start routine to retrieve it
		try {
			// when retrieving, encode the values with the recoding tables // NOT YET
			// and dump to the CSV
			fileMapper.createDataset(new ArrayList<String>(Arrays.asList(baseName)));
			workDone = true;
		}
		/*************** Real work ends here *******************/
		catch (NoSuchAlgorithmException e) {
			LogST.logP(0, "MatrixFileInput.openFile() - MD5 encoding algorithm not found");
			e.printStackTrace();
		} catch (JAXBException e) {
			LogST.logP(0, "MatrixFileInput.openFile() - JAXB error");
			e.printStackTrace();
		} catch (IOException e) {
			LogST.logP(0, "MatrixFileInput.openFile() - file not found");
			e.printStackTrace();
		} catch (SyntaxErrorInMappingException e) {
			LogST.logP(0, "MatrixFileInput.openFile() - malformed file mapping");
			e.printStackTrace();
		} catch (SQLException e) {
			LogST.logP(0, "MatrixFileInput.openFile() - SQL error");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			LogST.logP(0, "MatrixFileInput.openFile() - JDBC driver not found");
			e.printStackTrace();
		} catch (JDBCConnectionException e) {
			LogST.logP(0, "MatrixFileInput.openFile() - can't open JDBC connection");
			e.printStackTrace();
		} catch (UnsupportedDatabaseDriverException e) {
			LogST.logP(0, "MatrixFileInput.openFile() - JDB driver needed is not supported");
			e.printStackTrace();
		}

		if (workDone) { // only if we did not get any exception check for the CSV

			/**
			 * TODO refactor, the Mapping Manager should return this information; for now we check again
			 */
			try {
				fileOK = fileMapper.checkCSVFileExistence(baseName);
			} catch (Exception e) {
				LogST.logP(0, "MatrixFileInput, exception " + e.toString() + " for file " + baseName
						+ " in setup() not dowloaded");
			};

			LogST.logP(0, "MatrixFileInput.openFile() - DBMS download done for file" + baseName + " at time "
					+ new java.util.Date().toString());

			/**************
			 * END of interaction with the Database ***********************
			 * 
			 * now if we really got the data, we are fine; but if a query for manual execution was produced instead, no
			 * file to open --> the script execution cannot continue; we should gracefully generate any more required
			 * data and exit;
			 * 
			 * in the future, this can be implemented by launching an internal exception, caught in the interpreter, or
			 * floated to the outermost eval(), whose catch will scan all the modules in the script, triggering the
			 * openFile() of all FileInput modules
			 */
		}

		// 3 possible cases: a) csv downloaded from DB b) csv not downloaded, query generated c) error occurred
		// let the user know in case b) he has to run the query manually
		if (!fileOK && Dynamic.ignoreDBConnection) {
			LogST.logP(0, "MatrixFileInput - no DBMS connection - query dumped to text file\n"
					+ "Please execute the query, place the result in the directory of IAD files, validate the files.");
		}
		// case a) returns true, b) c) return false.
		return fileOK;
	}
	
	
	/**
	 * Debugging method, returns basic info about the module. 
	 * 
	 */
	public String toString() {
		return String.format("FileInputModule named '%s'\n with from file: '%s'\nordered by %s", name, inputFilename,
				orderByList);

	}

	/**
	 * The FileInput module does not support rewinding the file.
	 */
	@Override
	public void reset() {
//		throw new UnsupportedOperationException("Reset not supported yet.");
		LogST.logP(2,"NO-OP: MatrixFileInput.reset()");
	}

	public void exec() {
	}

	/**
	 * Check if there are more data, and as a side effect, open the file. 
	 */
	@Override
	public boolean hasMore() {
		if (inputCSV == null) 
			{ openFile(); }
		return inputCSV.hasMore();
	}

	/**
	 * Get next row and parse its fields into Symbol values. Should some way support caching a set of rows.
	 * 
	 * FIXME Actual parsing of the data fields should be in a different class and not inside here.
	 */
	@Override
	public void next() {
		int i=0;
		
		// these vars are here just for reporting
		String report_val = null;
		int report_i = -2;

		while (hasMore())		
			try {
				List<String> columns = inputCSV.next();
				List<Symbol<?>> attrs = this.attributes();
				int nHead = attrs.size();
				for (i = 0; i < nHead; i++) 
				{
					Symbol<?> s = attrs.get(i);
					String val = columns.get(i);
					report_val = val; report_i=i; // for reporting
					if (val.isEmpty()) {
						s.setValue(null);
					}
					else
						switch (s.type) {
							case INT :
							{
								s.setValue(Integer.parseInt(val));
								break;
							}
							case FLOAT :
							{
								s.setValue(Float.parseFloat(val));
								break;
							}
							case BOOLEAN :
							{
								try {
									int parsedIntValue = Integer.parseInt(val);
									if (parsedIntValue==0) s.setValue(false);
									else s.setValue(true);
								} catch (NumberFormatException ex) {
									s.setValue(Boolean.parseBoolean(val));
								}
								break;
							}
							case STRING:
							{
								s.setValue(val);
								break;
							}
							case DATE :
							{
								s.setValue(DateUtil.parse(val));
								break;
							}
						}
				}
				return; // for ended without any exception
			}
			/***
			 * here we should catch any parsing exception and report them to the user in useful way; we
			 * interact with the input routine so that the whole row is marked as bad.
			 */
			catch (IllegalArgumentException e) { 
				// catches NumberFormatE from int/float as well as exceptions form DateUtil
				LogST.logP(1, "MatrixFileInput : discarding input line, caught exception while parsing field " + i
						+ " " + attributes().get(i).toString());
				//				LogST.logException(e); // only in logs!
				LogST.logP(2, "Exception caught: "+e.toString());
				
				// currently we mostly do the same for temporary and permanent files
				if (fileIsTemporary == false)
					LogST.errorParsing(this.name, this.inputFilename, report_val, inputCSV.getRowCursor()+"", i+" = "+this.inputSchema.attributes().get(i).name, attributes().get(i).type.toString());
				else
					LogST.errorParsing(this.name, "temporary file"/*this.inputFilename*/, report_val, inputCSV.getRowCursor()+"", i+" = "+this.inputSchema.attributes().get(i).name, attributes().get(i).type.toString());
									
				// this will re-execute the while body, discarding the current line
				continue;
			}
		
		/**
		 * if we get out of the while it means either next() was called on exhausted input, or at least one input line
		 * was discarded because of parsing errors; 
		 * 
		 * FIXME should throw a specific exception!
		 */
		LogST.logP(0, "MatrixFileInput : empty input line, caught exception while parsing ");
	}

}
