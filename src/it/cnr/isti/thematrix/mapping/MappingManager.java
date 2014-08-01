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
package it.cnr.isti.thematrix.mapping;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import it.cnr.isti.thematrix.common.Enums;
import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.MappingSingleton;
import it.cnr.isti.thematrix.configuration.mapping.Dataset;
import it.cnr.isti.thematrix.configuration.setting.DbConnection;
import it.cnr.isti.thematrix.configuration.setting.TheMatrix;
import it.cnr.isti.thematrix.exception.SyntaxErrorInMappingException;
import it.cnr.isti.thematrix.exception.JDBCConnectionException;
import it.cnr.isti.thematrix.exception.UnsupportedDatabaseDriverException;
import it.cnr.isti.thematrix.mapping.creator.ValueRemapper;
import it.cnr.isti.thematrix.mapping.creator.QueryCreator;
import it.cnr.isti.thematrix.mapping.creator.IadCreator;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;
import it.cnr.isti.thematrix.mapping.utils.QuerySet;
import it.cnr.isti.thematrix.mapping.utils.TempFileManager;

/**
 * This class manages the mapping of IAD datasets into CSV datasets. If needed, it can trigger the retrieval of datasets
 * from DBMS (or the generation of queries for manual execution) via IADCreator. <br>
 * 
 * FIXME the Batch size is distinct from IadCreator but locally defined
 * 
 * @author massimo
 * 
 */
public class MappingManager {
	/** How many rows are fetched in a single batch from a CSV file. */
	private final int CSV_BATCH_SIZE = 20000;

	/**
	 * Creates a specified set of IAD datasets. The method can trigger mapping of IAD CSV files and involve DMBS
	 * queries. <br>
	 * TODO streamline warning/error/progress messages, too much duplication and ambiguous messages TODO: use TheMatrix
	 * custom exceptions.
	 * 
	 * @param datasetNames
	 *            The <code>Collection<String></code> of IAD datasets that need be mapped.
	 * 
	 * @throws JAXBException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public void createDataset(Collection<String> datasetNames) throws JAXBException, IOException,
			NoSuchAlgorithmException, SyntaxErrorInMappingException, SQLException, ClassNotFoundException,
			JDBCConnectionException, UnsupportedDatabaseDriverException {
		TheMatrix matrix = ConfigSingleton.getInstance().theMatrix;
		String version = matrix.getVersion();

		LogST.logP(1, "-> Avvio controlli di consistenza\n");

		// here we check which files are actually missing: some of them may have been manually added
		// after having performed previously generated SQL queries.
		// For those still missing, we generate and possibly execute the SQL queries
		Collection<String> missingIad = checkCSVFileExistence(datasetNames);

		if (!missingIad.isEmpty()) {
			LogST.logP(1, "Matrix MappingManager - iad datasets missing; checking liad datasets");
			LogST.logP(0, "-> Dataset IAD mancanti; avvio controlli di consistenza per dataset LIAD\n");

			for (String missing : missingIad) {
				Dataset dataset = MappingSingleton.getInstance().mapping.getDatasetByName(missing);

				Collection<String> files = null;
				if (dataset.getJoinClause() == null) {
					/* this seems incorrect: if no Join is defined, we get all tables? why? to do what? */
					// FIXME the Join Clause and Name cannot currently be null -- dead code?
					LogST.logP(1, "Matrix MappingManager - trying getAllLIADTables()");
					files = dataset.getAllLIADTables();
				}
				else {
					LogST.logP(1, "Matrix MappingManager - using getJoinName()");
					files = new ArrayList<String>();
					files.add(dataset.getJoinName());
				}

				// if all LIAD files are found, the mapping process can proceed,
				// otherwise a retrieval procedure is started for those missing (or corrupted) files.
				//
				// FIXME this code should be in a separate loop placed afterwards, and reworking of the files variable
				// initialization is needed (maybe also of the checkCSVFileExistence interface ).
				// FIXME Besides, according to the log translatePersistentCsv is never called.
				if (checkCSVFileExistence(files).isEmpty()) {
					LogST.logP(0, "-> Dataset \"LIAD\" presenti, inizio procedura di mapping\n");
					LogST.logP(1,
							"Matrix MappingManager - all necessary liad dataset found, starting mapping procedure");

					translatePersistentCsv(missing); // what does it do? it is apparently never called atm.
				}
				else {
					retrieveCSV(missing, version);
				}
			}
		}
		else {
			// out.write("-> Dataset \"IAD\" trovati\n");
			LogST.logP(1, "Matrix MappingManager - all needed datasets found under \"" + matrix.getPath().getIad()
					+ "\" folder");
		}

		LogST.logP(1, "-> Procedura terminata\n");
	}

	/**
	 * Performs CSV existence and integrity checks, performing an update if necessary. Will NOT validate checksum if
	 * file is temporary.
	 * 
	 * FIXME there is a bug with temporary files when they are compressed DO NOT ENABLE COMPRESSION YET
	 * 
	 * TODO fix for compressed files
	 * TODO shall it be made public? <br>
	 * TODO refactor to use the base method which checks a single file <br>
	 * 
	 * @param datasetNames
	 *            A <code>Collection<String></code> of the file names that need to be checked.
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 * @throws NoSuchAlgorithmException
	 * 
	 * @return A <code>Collection<String></code> containing the names of missing of corrupted CSV files.
	 */
	private Collection<String> checkCSVFileExistence(Collection<String> datasetNames) throws NoSuchAlgorithmException,
			JAXBException, IOException {
		String iadPath = ConfigSingleton.getInstance().theMatrix.getPath().getIad();

		Collection<String> result = new ArrayList<String>();

		for (String datasetName : datasetNames) {
			if (!CSVFile.checkExistence(iadPath, datasetName) // FIXME the ".csv" below is NOT correct
					|| (!TempFileManager.isTemporary(iadPath, datasetName + ".csv") && !CSVFile.validateCheckSum(
							iadPath, datasetName))) {
				LogST.logP(1, "Matrix MappingManager - missing " + datasetName + ".csv file");
				result.add(datasetName);
			}
		}
		return result;
	}

	/**
	 * Performs CSV existence and integrity checks, returns false if file not found/not validated.
	 * The integrity check is always done.
	 * This function cannot be called on a temporary file
	 * 
	 * @param datasetName
	 *            a single dataset name without extension
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 * @throws NoSuchAlgorithmException
	 * 
	 * @return true if file is found and verified.
	 */
	public boolean checkCSVFileExistence(String datasetName)
	throws NoSuchAlgorithmException, JAXBException, IOException 
	{
		return this.checkCSVFileExistence(Dynamic.getIadPath(), datasetName);
//		String iadPath = Dynamic.getIadPath();//ConfigSingleton.getInstance().theMatrix.getPath().getIad();
//		boolean result = true;
//
//		if (!CSVFile.checkExistence(iadPath, datasetName, false)
//				|| (!CSVFile.validateCheckSum(iadPath, datasetName))) 
//		{
//			LogST.logP(0, "Matrix MappingManager - checkCSV Existence, missing or invalid checksum " + datasetName + /*".csv file"*/ " file");
//			result = false;
//		}
//
//		LogST.logP(0, "Matrix MappingManager - checkCSV Existence, name: " + iadPath+datasetName+ " found: "+result);
//		return result;
	}

	public boolean checkCSVFileExistence(String dir, String fileName) 
	throws NoSuchAlgorithmException, JAXBException, IOException
	{
		boolean result = true;
		boolean existence;
		boolean checksum;
		
		
		// we skip checksum validation for temporary files
		if (TempFileManager.isTemporary(dir, fileName+".csv"))
		{
			existence = CSVFile.checkExistence(dir, fileName, true);
			checksum = true;
		}
		else
		{
			existence = CSVFile.checkExistence(dir, fileName, false);
			checksum = CSVFile.validateCheckSum(dir, fileName);
		}
			
		
		
		if (!existence || !checksum) 
		{
			LogST.logP(0, "Matrix MappingManager - checkCSV Existence, missing or invalid checksum " + fileName + /*".csv file"*/ " file");
			result = false;
		}

		LogST.logP(0, "Matrix MappingManager - checkCSV Existence, name: " + dir+fileName+ " found: "+result);
		return result;
	}
	
	/**
	 * ADD PROPER DOCUMENTATION Performs the mapping of header and values of a CSV producing a IAD compliant dataset.
	 * 
	 * Cycle though the LIAD files defined in the configuration, check that each one exists (implies MD5 checksum at the
	 * moment) and load a <code>CSV_BATCH_SIZE</code> batch of lines. For each file missing, set up the download from
	 * DBMS or the generation of the SQL query, according to the configuration; if possible create the CSV file. <br>
	 * 
	 * FIXME the code seems to take care only of the first batch of the file!
	 * 
	 * @param datasetName
	 *            The IAD name of the dataset to be mapped.
	 * 
	 * @throws JAXBException
	 * @throws SyntaxErrorInMappingException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	@Deprecated
	private void translatePersistentCsv(String datasetName) throws JAXBException, SyntaxErrorInMappingException,
			NoSuchAlgorithmException, IOException {
		TheMatrix theMatrix = ConfigSingleton.getInstance().theMatrix;
		String iadPath = theMatrix.getPath().getIad();

		CSVFile liadCsv = new CSVFile(iadPath, datasetName, theMatrix.getVersion());
		LogST.logP(1, "Matrix MappingManager - persistent dataset loaded");

		// FIXME: Si puo migliorare per evitare di chiamare solo sulla prima riga
		while (liadCsv.hasNext()) { // cycle on known liad datasets
			// try loading the file directly
			LogST.logP(1, "Matrix MappingManager - persistent dataset loading");
			liadCsv.loadBatch(CSV_BATCH_SIZE);
			LogST.logP(1, "Matrix MappingManager - persistent dataset " + CSV_BATCH_SIZE + " rows loaded");

			// the header is empty if no file is found (?) -> retrieve from DBMS
			if (liadCsv.getHeader().isEmpty()) {
				LogST.logP(1, "Matrix MappingManager - persistent dataset header mapping");
				ValueRemapper.applyMappingToCsvHeader(liadCsv, datasetName);
				LogST.logP(1, "Matrix MappingManager - persistent dataset header mapped");
			}

			LogST.logP(1, "Matrix MappingManager - persistent dataset body mapping");
			ValueRemapper.applyMappingToDataValues(liadCsv, datasetName);
			LogST.logP(1, "Matrix MappingManager - persistent dataset body mapped");

			// liadCsv.saveTo(true, iadPath, datasetName); // FIXME why on earth do we append here!
			liadCsv.saveTo(iadPath, datasetName, false); // write modified data
			LogST.logP(1, "Matrix MappingManager - dataset saved");
		}

		LogST.logP(1, "Matrix MappingManager - complete mapped dataset created");
	}

	/**
	 * Performs the retrieval of a IAD dataset (only one) from DBMS. It creates the <code>QuerySet</code> needed for the
	 * given dataset name via the <code>QueryCreator</code>. The <code>QuerySet</code> is either executed via
	 * <code>IadCreator</code> methods, or dumped to a text file via <code>QueryCreator</code>.
	 * 
	 * Note: although a collection, the QuerySet can currently only contain _one_ Query associated to _the_ only
	 * dbConnection.
	 * 
	 * @param datasetName
	 *            The name of the IAD dataset that has to be retrieved.
	 * @param scriptName
	 *            The name of the script that has triggered the mapping process.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws JAXBException
	 *             IOException
	 * @throws SyntaxErrorInMappingException
	 * @throws JDBCConnectionException
	 * @throws UnsupportedDatabaseDriverException
	 * 
	 */
	private void retrieveCSV(String datasetName, String scriptName) throws NoSuchAlgorithmException, SQLException,
			ClassNotFoundException, JAXBException, IOException, SyntaxErrorInMappingException, JDBCConnectionException,
			UnsupportedDatabaseDriverException {
		TheMatrix matrix = ConfigSingleton.getInstance().theMatrix;
		LogST.logP(0, "-> Avvio creazione query\n");
		DbConnection dbConnection = ConfigSingleton.getInstance().theMatrix.getDbConnection();

		// Create the whole set of queries used to map the dataset.
		QuerySet queries = new QuerySet(datasetName, QueryCreator.getQueryDescriptor(datasetName));
		LogST.logP(0, "-> Queries created for " + queries.getIADDataset() + "\n");

		// effect of command line arg -ignoreDBConnection : disregard the DB and just dump the queries to disk
		if (dbConnection == null || Dynamic.ignoreDBConnection) {
			LogST.logP(0, "Matrix MappingManager - dumping queries for manual execution");
			QueryCreator.dumpQueries(queries, scriptName);
			LogST.logP(0, "Matrix MappingManager - queries dumped");
		}
		else {
			String path = matrix.getPath().getIad();
			String version = matrix.getVersion();
			// java.util.Date d = new java.util.Date();

			LogST.logP(1, "-> Avvio procedura di recupero da DBMS dei dataset : " + (new java.util.Date()).toString()
					+ "\n");
			IadCreator.executeQuerySet(dbConnection, queries, path, version);

			// d = new java.util.Date();
			LogST.logP(1, "-> Procedura di recupero conclusa, dataset \"" + queries.getIADDataset() + "\" created: "
					+ (new java.util.Date()).toString() + "\n");
		}
	}
}
