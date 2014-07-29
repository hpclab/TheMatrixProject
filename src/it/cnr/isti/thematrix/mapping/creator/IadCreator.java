package it.cnr.isti.thematrix.mapping.creator;

import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.setting.DbConnection;
import it.cnr.isti.thematrix.configuration.setting.TheMatrix;
import it.cnr.isti.thematrix.dbms.Kind;
import it.cnr.isti.thematrix.dbms.Kind.DbDriver;
import it.cnr.isti.thematrix.dbms.Translator;
import it.cnr.isti.thematrix.exception.DatasetRowLengthMismatch;
import it.cnr.isti.thematrix.exception.JDBCConnectionException;
import it.cnr.isti.thematrix.exception.UnsupportedDatabaseDriverException;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;
import it.cnr.isti.thematrix.mapping.utils.QueryDescriptor;
import it.cnr.isti.thematrix.mapping.utils.QuerySet;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBException;

/**
 * Class to execute queries on the DBMS and retrieve datasets, called by MappingManager. This class relies on the
 * QueryCreator to create the SQL queries. Both classes use the QuerySet class for holding queries. <br>
 * 
 * Important: the class seems to support multiple queries to produce columns of a single IAD table, even when no JOIN is
 * in place. It's not clear if this should be supported and if it is used currently. UNLESS this is the support for
 * horizontally split-up queries (in that case there are issues in the implementation). <br>
 * 
 * FIXME the Batch size is distinct from MappingManager but locally defined
 * 
 */
public class IadCreator {
	// How many rows we suggest the DBMS we will look each time when examine the result of a single query.
	private static int DB_FETCH_SIZE = 20000;

	// How many rows to be saved in a single tmp LIAD CSV file.
	private static final int BATCH_SIZE = 20000;

	private static String dbPassword = null;

	private static Collection<String> generatedFiles = new ArrayList<String>();

	/**
	 * Method to execute a set of queries over an instance of a DBMS connection, producing a CSV IAD file. Calls smaller
	 * methods to perform the real tasks: <code>createConnection</code>, <code>execute queries</code>,
	 * <code>closeConnection</code>.
	 * 
	 * @param dbConnection
	 *            An instance of <code>Connection</code> object.
	 * @param queries
	 *            The set of queries to be executed.
	 * @param path
	 *            The path used to save IAD file.
	 * @param version
	 *            Undocumented, not used.
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws ClassNotFoundException
	 * @throws JDBCConnectionException
	 * @throws JAXBException
	 * @throws UnsupportedDatabaseDriverException
	 */
	public static void executeQuerySet(DbConnection dbConnection, QuerySet queries, String path, String version)
			throws SQLException, IOException, NoSuchAlgorithmException, ClassNotFoundException,
			JDBCConnectionException, JAXBException, UnsupportedDatabaseDriverException {
		generatedFiles.clear();

		Connection connection = createConnection(dbConnection);

		performQueries(connection, queries, path);

		if (!closeConnection(connection)) { throw new JDBCConnectionException("Error while closing connection"); }
	}

	/**
	 * Executes a set of queries on the DBMS, with a provided open connection
	 * 
	 * @param connection
	 *            An instance of <code>Connection</code> object.
	 * @param queries
	 *            The set of queries to be executed.
	 * @param path
	 *            The path used to save the IAD file.
	 * 
	 * @throws JAXBException
	 * @throws NoSuchAlgorithmException
	 * @throws DatasetRowLengthMismatch
	 */
	private static void performQueries(Connection connection, QuerySet queries, String path) throws SQLException,
			IOException, NoSuchAlgorithmException, JAXBException {
		int count = 0;
		DbDriver dbType = ConfigSingleton.getInstance().theMatrix.getDbDriver();
		
		try { // this try goes with the for body + the dumptable

			/**
			 * this code seems not to work with mysql (tries to dump the whole result). Check the differenes with this URL
			 * http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-implementation-notes.html
			 * 
			 * the situation may require <code>stmt.setFetchSize(Integer.MIN_VALUE);</code> and getting the rows one by one...
			 * 
			 * see also http://stackoverflow.com/questions/2447324/streaming-large-result-sets-with-mysql
			 * 
			 * https://svn.apache.org/repos/asf/jena/tags/jena-sdb-1.3.5/src/com/hp/hpl/jena/sdb/sql/SDBConnection.java
			 */
			
			for (QueryDescriptor queryDesc : queries.getQuery()) {
				LogST.logP(1, "---------------------------------------------------------------");
				LogST.logP(1, "Matrix IadCreator - getting statement " + count);
				Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

				LogST.logP(1, "Matrix IadCreator - setting fetch size to " + DB_FETCH_SIZE);
				if(dbType == Kind.DbDriver.mysqlDriver) stmt.setFetchSize(Integer.MIN_VALUE);
				else stmt.setFetchSize(DB_FETCH_SIZE);

				LogST.logP(1, "Matrix IadCreator - statement " + count + " created");

				LogST.logP(1, "Matrix IadCreator - query about to be executed: \"" + queryDesc.query + "\"");
				LogST.logP(1, "---------------------------------------------------------------\n");
				ResultSet rset = null;
				try {
					rset = stmt.executeQuery(queryDesc.query);
					if(dbType == Kind.DbDriver.mysqlDriver) dumpTableMySql(rset, queries.getIADDataset());
					else dumpTable(rset, queries.getIADDataset());

					count++;
					LogST.logP(1, "Matrix IadCreator - statement executed ");
					LogST.logP(1, "---------------------------------------------------------------\n");
				} catch (SQLException e) {
					LogST.logP(0, "Matrix IadCreator - failed SQL statement with error code " + e.getErrorCode()
							+ "\n   Reason" + e.toString() + "\n    SQL stte" + e.getSQLState());
					throw new Error("IadCreator SQL query failed", e);
				} finally {
					if (rset != null){ // we may not have a result if stmt failed
						LogST.logP(1, "Matrix IadCreator - result set to close: " + count);
						rset.close(); 
					}
					LogST.logP(1, "Matrix IadCreator - statement to close: " + count);
					stmt.close();
					LogST.logP(1, "Matrix IadCreator - statement and result set " + count + " closed");
				}
			}

		} // here we catch exceptions from the dumptable
		catch (DatasetRowLengthMismatch e) {
			LogST.logP(0, " ----> Matrix IadCreator ERROR -" + e.toString() + "\n  il dataset "
					+ queries.getIADDataset() + " NON PUO' ESSERE GENERATO <---- ");
			LogST.logException(e);
		} catch (SQLException e) {
			LogST.logP(0, " ----> Matrix IadCreator ERROR - unexpected exception" + e.toString() + "\n  il dataset "
					+ queries.getIADDataset() + " NON PUO' ESSERE GENERATO <---- ");
			LogST.logException(e);
		} 
	}

	/**
	 * Returns the header for the temporary CSV files. <br>
	 * TODO document HOW it is generated.
	 * 
	 * @param rsets
	 * @return the header as a collection of Strings (field names)
	 * @throws SQLException
	 */
	private static Collection<String> createHeader(ArrayList<ResultSet> rsets) throws SQLException {
//		StringBuilder buffer = new StringBuilder();
		ArrayList<String> header = new ArrayList<String>();

		LogST.logP(1, "Matrix IadCreator createHeader - starting header");
		for (int i = 0; i < rsets.size(); i++) {
			// ResultSet rset = rsets.get(i);
			ResultSetMetaData rsmd = rsets.get(i).getMetaData();
			int column = rsmd.getColumnCount();
			// LogST.logP(3,"rset " + i + "   n col " + column + "\n");
			for (int columnCount = 1; columnCount <= column; columnCount++) {
				// String v = rsmd.getColumnName(columnCount);
				// LogST.logP(3,"column " + columnCount + " value: " + v);
				header.add(rsmd.getColumnName(columnCount));
			}
		}

		LogST.logP(1, "Matrix IadCreator createHeader - header resulting: ");
		String tmpHeader = "[";
		String sep = "";
		for (String s : header) {
			tmpHeader = tmpHeader + sep + s;
			sep = ",";
			// LogST.logP(3,s);
		};
		LogST.logP(1, tmpHeader+"]");
		LogST.logP(1, "\nMatrix IadCreator createHeader - header done");
		return header;
	}

	/**
	 * Makes a dump of a DBMS table in CSV format; CLASS IS DEPRECATED, IT IS UNSAFE. The dump is broken into several batches of BATCH_SIZE in order to
	 * avoid constructing very large SQL statement results in memory; it is saved periodically to the CSV file <br>
	 * 
	 * TODO add code to check the number of received and dumped rows and log it to file - MC <br>
	 * TODO if the file was there but NOT valid, it should be truncated before the start! Otherwise it gets appended...
	 * 
	 * FIXME the path parameter is unused, refactor?
	 * 
	 * FIXME the management of DB_FETCH_SIZE and BATCH_SIZE is inconsistent, method will only work if they are the same!!!
	 * 
	 * @param rsets
	 *            The actual text of the query (or of the results? problem).
	 * @param path
	 *            The path used to save IAD file (in csv format).
	 * @param datasetName
	 *            The name of the file to save a dataset
	 * 
	 * 
	 * @throws JAXBException
	 * @throws NoSuchAlgorithmException
	 * @throws DatasetRowLengthMismatch
	 */
	@Deprecated
	private static void dumpTables(ArrayList<ResultSet> rsets, String datasetName) throws SQLException,
			IOException, NoSuchAlgorithmException, JAXBException, DatasetRowLengthMismatch {

		if (rsets.size() != 1) throw new DatasetRowLengthMismatch("IadCreator.dumpTables() does not support multiple rsets");
		
		boolean running = true;
		TheMatrix theMatrix = ConfigSingleton.getInstance().theMatrix;
		CSVFile csv = new CSVFile(theMatrix.getPath().getIad(), datasetName, theMatrix.getVersion());
		// Si crea un header comune unico
		csv.setHeader(createHeader(rsets));
		try {
			ValueRemapper.applyMappingToCsvHeader(csv, datasetName);
		} catch (Exception e) {
			LogST.logP(-1, "Error creating mapped header");
			LogST.logP(-1, e.toString());
			e.printStackTrace(new PrintWriter(LogST.getInstance().getWriter()));
			throw new Error("Cannot recover from mapping dataset errors in IadCreator.dumpTable(), applyMappingToCsvHeader()");
		}
		int iterationCount = 0;
		LogST.logP(1, "Setting log level to 3, it was " + LogST.getLogLevel() + "\n");
		LogST.enable(3); // maximum verbosity

		/**
		 * OK. apparently we have 3 loops, the innermost one takes care of rows, but stops if there are too many; the
		 * center one loops over all the different query result sets (assuming there are several in the same
		 * table...which is not true atm). Then the collected data is saved (appended) to the real file. The outermost
		 * loop checks if we had more rows to process; in that case it must fire up again the other two. What happens at
		 * this point may have to do with the issue with lost/spurious rows.
		 */

		{
			// Sanity Checking
			if (rsets.size() == 0) throw new DatasetRowLengthMismatch("Matrix Error - empty set of queries");
			/**
			 * these tricks do NOT work with a forward-only cursor!
			 * 
			ResultSet rset = rsets.get(1);
			if (rset.last()) throw new DatasetRowLengthMismatch("Matrix Error - empty result of first query");
			// if last() returns false, we have 0 rows. Otherwise, that is the last one and we get its number.
			// Remember SQL counts up from 1
			int rowcount = rset.getRow();
			rset.beforeFirst();

			LogST.logP(1, "Matrix IadCreator - query set check reports " + rowcount + " rows for first query set");
			 *
			 **/

		}
		// bisogna ciclare su tre dimensioni diverse : >> quali?? MC
		// si controlla che le righe delle tabelle non siano finite
		// while (running) MC this while should NEVER be used if the routine and
		// the JDBC results are correct -- FIXME
		while (running) {
			// si imposta come last l'ultima colonna scritta per proseguire wide
			// range ------------------- what? MC
			int nextColumn = 0;
			int rowCountLast = 0;
			LogST.logP(0, "Matrix IadCreator - running iteration " + iterationCount + " over " + rsets.size()
					+ " result sets");

			// per l'insieme di tutti i resultSet delle diverse query;
			for (int i = 0; i < rsets.size(); i++) {
				LogST.logP(0, "Matrix IadCreator - getting result set related to statement " + i + "; "
						+ "starting row " + iterationCount * BATCH_SIZE);
				ResultSet rset = rsets.get(i);
				// serve il numero delle colonne
				ResultSetMetaData rsmd = rset.getMetaData();
				int availableColumns = rsmd.getColumnCount();
				running = rset.next();
				int rowCount = 0;
				boolean batchDone = false;
				// check that we don't have too many lines per iteration
				// use the temporary csv file manager for a batch of rows

				/**
				 * The issue here seems to be that the csv file manager is built for columns...
				 */
				while (running && !batchDone) { // FIXME here running should not be tested IMHO
					if (rowCount < BATCH_SIZE) {
						for (int columnCount = 0; columnCount < availableColumns; columnCount++) {
							csv.setValue(nextColumn + columnCount, rset.getString(columnCount + 1));
						}
						rowCount++;
						running = rset.next(); // step to next row, false if no next row

						/**
						 * FIXME This is wrong: by changing running, we will skip all further processing of other rset
						 * elements. We should only exit for the outer cycle when we are done with the whole rset. It
						 * only works because (when) there is only one rset element.
						 */

					}
					else { // end of a batch
						LogST.logP(0, "Matrix IadCreator - switching resultset with rowCount " + rowCount);
						batchDone = true;
					}
				} // end of while on rows <BATCH_SIZE

				// Sanity check (not that strong) compare the lengths of last two written columns
				if (i != 0 && rowCountLast != rowCount) {
					throw new DatasetRowLengthMismatch("Matrix Error - le tabelle di mapping sono di lunghezza diversa");
				}
				else
					rowCountLast = rowCount;

				// Si salva l'indice di colonna dal quale continuare
				nextColumn = nextColumn + availableColumns;
				LogST.logP(0, "Matrix IadCreator - wrote " + (rowCount) + " row statement " + i
						+ " next start column: " + nextColumn);

			} // end of for on rsets.size(), go to next rset element if any

			try {
				ValueRemapper.applyMappingToDataValues(csv, datasetName);
			} catch (Exception e) {
				LogST.logP(-1, "Matrix IadCreator - error while mapping body");
				e.printStackTrace();
			}
			LogST.logP(0, "Matrix IadCreator - saving temp file");
			csv.save(iterationCount > 0); // truncate file at the first iteration <=> append only after
			iterationCount++;
		} // was end of : while(running)
		if (running) // should really define a different exception here
			throw new DatasetRowLengthMismatch("TheMatrix Error - unmatched/lost row within SQL results");

		// Now we need to close the file, or the content will be incorrect 
		csv.closeFile();
		
		// si crea il suo metaxml una volta terminato
		try {
			CSVFile.createMetaXml(csv.getPath(), csv.getFileName(), csv.getVersion());
		} catch (Exception e) {
			LogST.logException(e);
			LogST.logP(-1,"ERROR: IadCreator.dumpTable() could not create XML descriptor for file "+datasetName);
			throw new RuntimeException("IadCreator.dumpTable() XML descriptor failedfor file "+datasetName);
		}
		
		LogST.logP(0, "Matrix IadCreator - dump table finished");
		// reset the fragment counter for the next query execution.
	}
	
	/**
	 * Makes a dump of a DBMS table in CSV format; The dump is broken into several batches of BATCH_SIZE in order to
	 * avoid constructing very large SQL statement results in memory; it is saved periodically to the CSV file <br>
	 * 
	 * TODO add code to check the number of received and dumped rows and log it to file - MC <br>
	 * TODO if the file was there but NOT valid, it should be truncated before the start! Otherwise it gets appended...
	 * 
	 * 
	 * FIXME the management of DB_FETCH_SIZE and BATCH_SIZE is inconsistent, method will only work if they are the same!!!
	 * 
	 * @param resultSet
	 *            The actual text of the query (or of the results? problem).
	 * @param datasetName
	 *            The name of the file to save a dataset
	 * 
	 * @throws JAXBException
	 * @throws NoSuchAlgorithmException
	 * @throws DatasetRowLengthMismatch
	 */
	private static void dumpTable(ResultSet resultSet, String datasetName) throws SQLException,
	IOException, NoSuchAlgorithmException, JAXBException, DatasetRowLengthMismatch {
	
		int previousLogLevel;

		boolean running = true;
		TheMatrix theMatrix = ConfigSingleton.getInstance().theMatrix;
		CSVFile csv = new CSVFile(theMatrix.getPath().getIad(), datasetName, theMatrix.getVersion());

		//prepare header, we have to insert a resultSet into an ArrayList!!!
		ArrayList<ResultSet> rsets = new ArrayList<ResultSet>();
		rsets.add(resultSet);
		csv.setHeader(createHeader(rsets));
		try {
			ValueRemapper.applyMappingToCsvHeader(csv, datasetName);
		} catch (Exception e) {
			LogST.logP(-1, "Error creating mapped header");
			LogST.logP(-1, e.toString());
			e.printStackTrace(new PrintWriter(LogST.getInstance().getWriter()));
			throw new Error("Cannot recover from mapping dataset errors in IadCreator.dumpTable(), applyMappingToCsvHeader()");
		}
		int iterationCount = 0;

		/**
		 * FIXME raise log level to 3, will be set back to the correct value at end of method
		 */
		previousLogLevel=LogST.getLogLevel();
		LogST.logP(1, "Setting log level to 3, it was " + previousLogLevel + "\n");
		LogST.enable(3); // maximum verbosity

		boolean batchDone = false;
		
		while (running) {
	
			int nextColumn = 0;
			int rowCountLast = 0; //is unused!!
			LogST.logP(0, "Matrix IadCreator - running iteration " + iterationCount + " over " + rsets.size()
					+ " result sets");
			// serve il numero delle colonne
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int availableColumns = rsmd.getColumnCount();
			if (batchDone) batchDone = false; 
			else running = resultSet.next();
			int rowCount = 0;
			
			// check that we don't have too many lines per iteration
			// use the temporary csv file manager for a batch of rows
		
			/**
			 * The issue here seems to be that the csv file manager is built for columns...
			 */
			while (running && !batchDone) { // FIXME here running should not be tested IMHO
				if (rowCount < BATCH_SIZE) {
					for (int columnCount = 0; columnCount < availableColumns; columnCount++) {
						csv.setValue(nextColumn + columnCount, resultSet.getString(columnCount + 1));
					}
					rowCount++;
					running = resultSet.next(); // step to next row, false if no next row
		
				}
				else { // end of a batch
					LogST.logP(0, "Matrix IadCreator - switching resultset with rowCount " + rowCount);
					batchDone = true;
				}
			} // end of while on rows <BATCH_SIZE
		
			//Sanity check (not that strong) compare the lengths of last two written columns
			//this code is never used if the result set is only one
			/*
			if (i != 0 && rowCountLast != rowCount) {
				throw new DatasetRowLengthMismatch("Matrix Error - le tabelle di mapping sono di lunghezza diversa");
			}
			else
				rowCountLast = rowCount;
				// Si salva l'indice di colonna dal quale continuare
			nextColumn = nextColumn + availableColumns;
			LogST.logP(0, "Matrix IadCreator - wrote " + (rowCount) + " row statement " + i
						+ " next start column: " + nextColumn);
		    */
			try {
				ValueRemapper.applyMappingToDataValues(csv, datasetName);
			} catch (Exception e) {
				LogST.logP(-1, "Matrix IadCreator - error while mapping body");
				LogST.logException(e);
			}
			LogST.logP(0, "Matrix IadCreator - saving temp file");
			csv.save(iterationCount > 0); // truncate file at the first iteration <=> append only after
			iterationCount++;
			
			csv.resetContent(); //reset content of the csv file
		} // was end of : while(running)
		if (running) // should really define a different exception here
			throw new DatasetRowLengthMismatch("TheMatrix Error - unmatched/lost row within SQL results");
		// Now we need to close the file, or the content will be incorrect 
		csv.closeFile();
		
		// si crea il suo metaxml una volta terminato
		try {
			CSVFile.createMetaXml(csv.getPath(), csv.getFileName(), csv.getVersion());
		} catch (Exception e) {
			LogST.logException(e);
			LogST.logP(-1,"ERROR: IadCreator.dumpTable() could not create XML descriptor for file "+datasetName);
			throw new RuntimeException("IadCreator.dumpTable() XML descriptor failedfor file "+datasetName);
		}
		
		/**
		 * FIXME set back log level to the correct value
		 */
		LogST.logP(1, "Setting log level back to " + previousLogLevel + "\n");
		LogST.enable(previousLogLevel); // maximum verbosity
	
		LogST.logP(0, "Matrix IadCreator - dumpTable finished");
		// reset the fragment counter for the next query execution.

	}
	
	
	/**
	 * Makes a dump of a DBMS table of MySQL DB in CSV format; The dump is broken into several batches of BATCH_SIZE in order to
	 * avoid constructing very large SQL statement results in memory; it is saved periodically to the CSV file <br>
	 * 
	 * TODO add code to check the number of received and dumped rows and log it to file - MC <br>
	 * TODO if the file was there but NOT valid, it should be truncated before the start! Otherwise it gets appended...
	 * 
	 * 
	 * FIXME the management of DB_FETCH_SIZE and BATCH_SIZE is inconsistent, method will only work if they are the same!!!
	 * 
	 * @param resultSet
	 *            The actual text of the query (or of the results? problem).
	 * @param datasetName
	 *            The name of the file to save a dataset
	 * 
	 * @throws JAXBException
	 * @throws NoSuchAlgorithmException
	 * @throws DatasetRowLengthMismatch
	 */
	private static void dumpTableMySql(ResultSet resultSet, String datasetName) throws SQLException, NoSuchAlgorithmException, IOException, JAXBException {
	    // ResultSet is initially before the first data set

		int previousLogLevel;

		boolean running = true;
		TheMatrix theMatrix = ConfigSingleton.getInstance().theMatrix;
		CSVFile csv = new CSVFile(theMatrix.getPath().getIad(), datasetName, theMatrix.getVersion());
		
		//prepare header, we have to insert a resultSet into an ArrayList!!!
		ArrayList<ResultSet> rsets = new ArrayList<ResultSet>();
		rsets.add(resultSet);
		csv.setHeader(createHeader(rsets));
		try {
			ValueRemapper.applyMappingToCsvHeader(csv, datasetName);
		} catch (Exception e) {
			LogST.logP(-1, "Error creating mapped header");
			LogST.logP(-1, e.toString());
			e.printStackTrace(new PrintWriter(LogST.getInstance().getWriter()));
			throw new Error("Cannot recover from mapping dataset errors in IadCreator.dumpTableMySql(), applyMappingToCsvHeader()");
		}
		int iterationCount = 0;
		/**
		 * FIXME raise log level to 3, will be set back to the correct value at end of method
		 */
		previousLogLevel=LogST.getLogLevel();
		LogST.logP(1, "Setting log level to 3, it was " + previousLogLevel + "\n");
		LogST.enable(3); // maximum verbosity
		
		resultSet.next();

		while (running) {
			boolean batchDone = false;
			int rowCount = 0;
			int nextColumn = 0;
			//int rowCountLast = 0; is unsed
			LogST.logP(0, "Matrix IadCreator - running iteration " + iterationCount + " over " + rsets.size()
					+ " result sets");
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int availableColumns = rsmd.getColumnCount();
			while (running && !batchDone) { // FIXME here running should not be tested IMHO
				if (rowCount < Math.min(DB_FETCH_SIZE, BATCH_SIZE)) {
					
					for (int columnCount = 0; columnCount < availableColumns; columnCount++) {
						csv.setValue(nextColumn + columnCount, resultSet.getString(columnCount + 1));
						}
					running = resultSet.next();
					rowCount++;
				}else { // end of a batch
					LogST.logP(0, "Matrix IadCreator - switching resultset with rowCount " + rowCount);
					batchDone = true;
				}
		    } // end of while on rows <BATCH_SIZE
			try {
				ValueRemapper.applyMappingToDataValues(csv, datasetName);
			} catch (Exception e) {
				LogST.logP(-1, "Matrix IadCreator - error while mapping body");
				LogST.logException(e);
			}
			LogST.logP(0, "Matrix IadCreator - saving temp file");
			csv.save(iterationCount > 0);// truncate file at the first iteration <=> append only after
			iterationCount++;
			
			csv.resetContent(); //reset content of the csv file
		}
		// Now we need to close the file, or the content will be incorrect 
		csv.closeFile();
		// si crea il suo metaxml una volta terminato
		try {
			CSVFile.createMetaXml(csv.getPath(), csv.getFileName(), csv.getVersion());
		} catch (Exception e) {
			LogST.logException(e);
			LogST.logP(-1,"ERROR: IadCreator.dumpTableMySql() could not create XML descriptor for file "+datasetName);
			throw new RuntimeException("IadCreator.dumpTableMySql() XML descriptor failed for file "+datasetName);
		}
		
		/**
		 * FIXME set back log level to the correct value
		 */
		LogST.logP(1, "Setting log level back to " + previousLogLevel + "\n");
		LogST.enable(previousLogLevel); // maximum verbosity

		LogST.logP(0, "Matrix IadCreator - dumpTableMySQL finished");
	
	}

	/**
	 * Close JDBC connection.
	 */
	private static boolean closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Creates the JDBC connection to the DBMS; beforehand, it verifies if the password is available, or it reads it
	 * from console.
	 * 
	 * @param dbConn
	 *            our own public class holding the connection info
	 * 
	 * @return undocumented, TODO
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws UnsupportedDatabaseDriverException
	 * @throws Error
	 */
	private static Connection createConnection(DbConnection dbConn) throws ClassNotFoundException, SQLException,
			UnsupportedDatabaseDriverException, Error {
		String urlConnection = Translator.getConnectionString(dbConn.getType(), dbConn.getServerName(), dbConn.getPortNumber()+"", dbConn.getSid());
		String username = dbConn.getUser();

		Connection connection = null;

		try {
			if (dbPassword == null) {
				dbPassword = readPasswordFromConsole(username);
			}
			else {
				LogST.logP(0, "Matrix IadCreator - db password previously saved\n");
			}
		} catch (Exception e1) {
			throw new SQLException("Missing password to connect to DBMS");
		}

		String driverName;
		driverName = Translator.getJDBCDriverName(dbConn.getType()); // can throw Unsupported
		Class.forName(driverName);

		/* this if-chain is ugly ... */

		/**	
		if (dbConn.getType().equals("oracle")) {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}
		else if (dbConn.getType().equals("sqlserver")) {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		}
		else if (dbConn.getType().equalsIgnoreCase("mysql")) {
			Class.forName("com.mysql.jdbc.Driver");
		}
		else {
			throw new UnsupportedDatabaseDriverException("Driver for " + dbConn.getType()
					+ " databases are not supported yet");
		}
		 **/
		
		/* open the connection, at last! */
		try {
			// added to allow debugging problematic connections; should be
			// possible to disable dynamically
			java.io.PrintWriter w = new java.io.PrintWriter(new java.io.OutputStreamWriter(System.out));
			DriverManager.setLogWriter(w);
			// end of debug code

			/***
			 * FIXME this will need to be more general and used properties, as SAS will need the library to be there
			 */
			
			connection = DriverManager.getConnection(urlConnection, username, dbPassword);
			connection.setAutoCommit(false);

			LogST.logP(1, "IadCreator createConnection successful");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Error("IadCreator createConnection failed", e);
		}
		return connection;
	}

	/**
	 * Prompts the user for the password on the console
	 * 
	 * @param user
	 *            the username
	 * 
	 * @return the password
	 * 
	 * @throws Exception
	 */
	private static String readPasswordFromConsole(String user) throws Exception {
		Console c = System.console();
		String password = "";

		if (c == null) {
			System.err.println("No console.");
			System.out.println(String.format("You are logging in as: " + user + "\n"));
			System.out.print(String.format("Enter the password of that user account: "));

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			password = br.readLine();
			System.out.println("");
		}
		else {
			c.format("You are logging in as: " + user + "\n");
			char[] passwordChar = c.readPassword("Enter the password of that user account: ");
			password = new String(passwordChar);
			System.out.println("");
		}

		return new String(password);
	}

}
