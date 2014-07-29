package it.cnr.isti.thematrix.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.MissingResourceException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.WriterOutputStream;


/**
 * Class to hold all the logging related stuff inside TheMatrix. 
 * It is modeled as a singleton, all methods should be called after a getInstance(), 
 * with the exception of the logging method, which is static, for simplicity of use, 
 * and reimplements getInstance to issues a warning if forced to initialize the singleton.
 * <br>
 * It uses a logging level to decide what to print, 0<=logLevel<=3. 
 * Setting logLevel to N disables all log prints which have a higher level, 
 * so that 0 is minimum logging.
 *
 * @author Massimo Coppola
 *
 */
public class LogST {

	// private FileWriter logStream=null; 	/* private log output descriptor */
	private BufferedWriter logStream=null; 	/* private log output descriptor; TODO: refactor to a FileWriter */

	private static int logLevel;
	private static final String logName = "timeline.txt";

	private static LogST instance;
	
	/** safe carriage return for compatibility among OSs **/
	private static String separator = System.getProperty("line.separator"); 
	
	/** output file for errors on data **/
	private static File errorFile = new File("data_errors.text");
 
	/**
	 * Saves last line of log output, to allow shortening logs if it is repeated
	 * many times. I use a Stringbuilder to avoid caching a reference to a
	 * String that we want to be deallocable as soon as we leave this class.
	 * Only used by normal log output atm, not by data logging or exceptions.
	 */
	private static StringBuilder lastLine = new StringBuilder(1024); //never reallocate the buffer

	/**
	 * How many times last log line repeated (see lastLine).
	 */
	private static int lastLineRepeats=0;
	
	/**
	 * Hash code (as a string) of last line in the log, for quick comparison
	 * with lastLine content. Initialized with the hash of the empty string. 
	 */
	private static int lastHashCode=new String().hashCode();
	
	/**
	 * Static constructor. Create the error log file and prints the welcome message. 
	 */
	static
	{
		createErrorFile();
		logWelcome();
	}
	
	/**
	 * Gets the only instance of this class.
	 * @return the instance
	 */
	public static LogST getInstance() 
	{
		if (instance == null) 	{ instance = new LogST(); }
		return instance;
	}

	/***
	 * accessor to the BufferedWriter (for backward compatibility)
	 * TODO: refactor code to use LogST and move to FileWriter 
	 * @return the instance BufferedWriter
	 */
	public BufferedWriter getWriter() {
		return logStream;
	}
	
	/**
	 * Print the stack trace of the exception on the log, regardless of the current log level, and quietly (so it won't clutter the console of the
	 * casual user).
	 * 
	 * @param e the exception object to print
	 */
	public static void logException(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String trace = sw.toString(); // stack trace as a string

		logP(1,trace); // we  avoid printing exception on the console at loglevel 0

		/*
		try {
			instance.logStream.write(trace + "\n");
			instance.logStream.flush();
		} catch (IOException e2) {
			throw new Error("Can't write to " + LogST.logName);
		}
		*/
	}
	
	
	/**
	 * Private constructor for the singleton pattern.
	 * This functions opens in append mode 
	 * a log file, and keeps its descriptor in the instance. 
	 * 
	 * @throws MissingResourceException
	 */
	private LogST ()
	{ 
		logLevel = 0; 
		if (logStream ==null)
			try		{
				logStream = new BufferedWriter(new FileWriter(logName, false /* true */));
			}
			catch (IOException e1)		{
				e1.printStackTrace();
				throw new Error ("Cant open "+LogST.logName+" log file for writing!");
			}
	}
		
	/**
	 * This functions enables logging by appending to
	 * a log file whose descriptor is in the instance. 
	 * Descriptor is kept also when disabling logging. 
	 * A log level is specified which is assumed to be between 0 and 3, with 0 = disabled.
	 *  
	 * @param level any value <=0 disables logging, higher values select progressively more messages
	 */
	public static void enable(int level) {
		logLevel = level<=0? 0 : (level>=3 ? 3 : level); // cap at 0 and 3
		if (instance!=null) logP(0,"Switch to loglevel"+level+"\n");
	}

	/**
	 * Getter method for log level.
	 * @return current logging level l, 0<= l <=3.
	 */
	public static int getLogLevel(){ return logLevel; }
	
	/**
	 * Print a startup message in the logfile, no info on version and date
	 */
	//public void startupMessage() {
	//	startupMessage("Unknown","Unknown");
	//}
	
	/**
	 * Print a startup message in the logfile, with info on version and date, java System properties
	 *
	 * @param version the program version
	 * @param version2 the reference date as a string
	 */
	public void startupMessage(String version, String version2) 
	{
		java.util.Date d = new java.util.Date();
		
		StringBuilder welcomeMessage = new StringBuilder();
		welcomeMessage.append(d.toString() +"********************************************* \n");
		welcomeMessage.append(d.toString() +"* "+version+" released on "+version2+" *  \n");
		welcomeMessage.append(d.toString() +"*********************************************  \n");
		welcomeMessage.append(d.toString() +"Java Runtime Properties  \n\n");
		welcomeMessage.append(d.toString() +System.getProperties().toString());
		welcomeMessage.append(d.toString() +"\n\n");
		welcomeMessage.append(d.toString() +"*********************************************  \n");
		welcomeMessage.append("-> Avvio test: " + d.toString() + "\n");
		
		logP(0, welcomeMessage.toString());		
	}
	
	/**
	 * This functions disables logging and flushes the log file; 
	 * descriptor is kept in the instance.
	 */
	public void disable() {
		//instance.
		logLevel=0;
		try { instance.logStream.flush(); }
		catch (IOException e) {
			throw new Error ("Can't flush file " + LogST.logName);		
		}
	}
	
	/***
	 * close the log file and destroy the current singleton instance
	 */
	public void close() {
		try { 
			logGoodbye();
			logStream.close();
		}
		catch (IOException e) {
			throw new Error ("Can't close file " + LogST.logName);		
		}
		instance = null;
	}
	
	
	/**
	 * Static function to actually log into the file AND onto System.out. Adds trailing "\n"; we want it static.
	 * Synchronized since release 1.28.
	 * 
	 * @param level
	 *            logging level of the message.
	 * @param message
	 *            message to be print.
	 */
	public synchronized static void logP (int level, String message) 
	{
		// re-intsatiate instance if it was not
    	if (instance == null) 	
		{ 
			instance = new LogST();
			// instance.startupMessage();
			LogST.logP(1, "WARNING: log file open by LogST due to missing singleton initialization\n");
		}

		// we do not shorten the console output
    	try 
    	{
    		if (level == 0)
    			writeOnOutStreams("");
		
    		if (logLevel >= level)
    		{
				// log shortening code
				/* if hash code matches and they are the same */
				/* null is actually an error, but let's not be picky here */
				if (message.hashCode() == lastHashCode
						&& message != null
						&& lastLine.indexOf(message) == 0
						&& lastLine.length() == message.length()) {
					lastLineRepeats++;
					// and skip printing it for now
				} 
				else 
				{
					if (lastLineRepeats!=0 ) {
						// we had some repetitions before to give account for
						String out = "["+level+"] -+- Previous message repeated "+lastLineRepeats+ " times\n";
						writeOnOutStreams(out);
					}
					lastLineRepeats =0;
					lastLine.setLength(0);lastLine.append(message);
					lastHashCode=message.hashCode();

					// the real logging
					//instance.logStream.write("["+level+"]"+message+"\n");
					//instance.logStream.flush();
					
					writeOnOutStreams("["+level+"]"+message+"\n");
				}
				
			}
    	}
		catch (IOException e) {
			throw new Error ("Can't write to " + LogST.logName);
		}
	}
	
	/**
	 * Actual writing on the streams
	 * @param text
	 * @throws IOException
	 */
	private static void writeOnOutStreams(String text) throws IOException
	{
		instance.logStream.write(text);
		instance.logStream.flush();
		System.out.print(text);
		System.out.flush();
	}
	
	public static void logMemory(String message)
	{
		try
		{
			instance.logStream.write("[M]"+message+"\n");
			instance.logStream.flush();
		}
		catch (IOException e) 
		{
			System.out.println("[E] Can't write to " + LogST.logName);
			throw new Error ("Can't write to " + LogST.logName);
		}
	}
	
	/**
	 * idle for a second after last log... so throwing exceptions immediately afterwards doesn't mess up the screen.
	 */
	public static void spindown() {
		try {
			  Thread.sleep(1000);
			} catch (InterruptedException ie) {};
	}
	
	// ================================================================================ //
	
	/**
	 * Prints the welcome message
	 */
	public static void logWelcome()
	{
		String message = getCurrentDate() + "************** TheMatrix log started **************" + separator;
		errorLog(message);
	}
	
	/**
	 * Prints the goodbye message
	 */
	public static void logGoodbye()
	{
		String message = getCurrentDate() + " ************** TheMatrix log shut down **************" + separator;
		errorLog(message);
	}

	
	/**
	 * To call when the header read from the file does not match with the schema
	 * defined for the file
	 * @param module_name
	 * @param source
	 * @param actual_header
	 * @param expected_schema
	 */
	public static void errorSchema(String module_name, String source, String actual_header, String expected_schema)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getCurrentDate()).append("ERROR: header does not match schema.").append(" Module: ").append(module_name)
		.append(" Source: ").append(source).append(separator)
		.append("Read     header: ").append(actual_header).append(separator)
		.append("Expected schema: ").append(expected_schema).append(separator);
		
		errorLog(sb.toString());
	}
	
	/**
	 * To call when a value in a line of the cvs file cannot be parsed.
	 * @param module_name
	 * @param source
	 * @param symbol
	 * @param row
	 * @param column
	 * @param expected_type
	 */
	public static void errorParsing(String module_name, String source, String symbol, String row, String column, String expected_type)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getCurrentDate()).append("ERROR: data type mismatch.").append(" Module: ").append(module_name)
		.append(" Source: ").append(source).append(" Symbol ").append(symbol).append(" column: ").append(column).append(" on line ").append(row)
		.append("; Expected type: ").append(expected_type).append(separator);
		
		errorLog(sb.toString());
	}
	
	/**
	 * Error on reading a data file.
	 * @param module_name
	 * @param file
	 */
	public static void errorDataFile(String module_name, String file)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getCurrentDate()).append("ERROR: data file missing or corrupted.")
		.append(" Module: ").append(module_name)
		.append(" File name: ").append(file).append(separator);
		
		errorLog(sb.toString());
	}
	
	/**
	 * Error on reading a configuration file.
	 * @param module_name
	 * @param file
	 */
	public static void errorConfigurationFile(String file)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getCurrentDate()).append("ERROR: configuration file missing or corrupted.")
		.append(" File name: ").append(file).append(separator);
		
		errorLog(sb.toString());
	}
	
	
	public static void errorMappingAttribute(String file, String attribute, String parent)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getCurrentDate()).append("ERROR: The attribute "+attribute+" of the element "+parent+" is not present or empty. File: "+file);
		sb.append(separator);
		
		errorLog(sb.toString());
	}
	
	public static void errorMappingElement(String file, String element, String parent)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getCurrentDate()).append("ERROR: The element "+element+" within "+parent+" is not present. File: "+file);
		sb.append(separator);
		
		errorLog(sb.toString());
	}
	
	public static void errorCustom(String message)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getCurrentDate()).append(message).append(separator);
		
		errorLog(sb.toString());
	}

	/**
	 * To call when an inconsistency in data is found during the application of a function.
	 * The message is personalized by the caller.
	 * @param module_name
	 * @param function
	 * @param message
	 */
	public static void errorDataInconsistency(String module_name, String function, String message)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getCurrentDate()).append("ERROR: data inconsistency during execution of "+function)
		.append(" Module: ").append(module_name).append(" Message: ").append(message).append(separator);
		
		errorLog(sb.toString());
	}

	/**
	 * Formats and returns the current date.
	 * @return
	 */
	private static String getCurrentDate()
	{
		SimpleDateFormat dt = new SimpleDateFormat("[dd-MM-yy HH:mm:ss] ");
		Date date = new Date();
		
		return dt.format(date);
	}
	
	/**
	 * Create the error file.
	 * May throw an error if something goes wrong when creating the file.
	 */
	private static void createErrorFile()
	{
		try
		{
			if (errorFile.exists())
				errorFile.delete();
			errorFile.createNewFile();
		}
		catch (Exception e)
		{
			throw new Error("Failed to create the log file "+errorFile.getName()+". Exeception: "+e.getMessage());
		} 
	}
	
	/**
	 * Actually writes on:
	 * (i) the error file, 
	 * (ii) console, and 
	 * (iii) on the debug file.
	 * May throw an error if something goes wrong when writing on the file.
	 * @param message
	 */
	private static void errorLog(String message)
	{
		try
		{		
			FileUtils.writeStringToFile(errorFile, message, true);
			logP(1, message);
		}
		catch (Exception e) 
		{
			throw new Error("Failed to write on the log file "+errorFile.getName()+". Exeception: "+e.getMessage());
		} 
	}

}
