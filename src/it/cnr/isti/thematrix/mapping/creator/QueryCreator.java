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
package it.cnr.isti.thematrix.mapping.creator;

import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.MappingSingleton;
import it.cnr.isti.thematrix.configuration.mapping.Dataset;
import it.cnr.isti.thematrix.configuration.setting.TheMatrix;
import it.cnr.isti.thematrix.exception.SyntaxErrorInMappingException;
import it.cnr.isti.thematrix.mapping.utils.QueryDescriptor;
import it.cnr.isti.thematrix.mapping.utils.QuerySet;
import it.cnr.isti.thematrix.dbms.Kind.DbDriver;
import it.cnr.isti.thematrix.dbms.Translator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

/**
 * This class generates the test of SQL queries needed to extract a IAD dataset file from a DBMS. Method
 * <code>computeQuerySet</code> does the hard part. <br>
 * TODO : REFACTOR! all internal-only functions should receive the dataset instance and not look up again in the
 * singleton. (and be made private).
 */
public class QueryCreator {
	/**
	 * Method that returns a collection of queries starting from a dataset name and a list of attributes to map.
	 * 
	 * TODO check that the inner call to <code>computeQuerySet</code> actually does all the dirty work.
	 * 
	 * @param datasetName
	 * @param attributeNames
	 * 
	 * @return A <code>Collection<QueryDescriptor></code>
	 */
	public static Collection<QueryDescriptor> getQueryDescriptor(String datasetName, Collection<String> attributeNames) {
		return computeQuerySet(datasetName, attributeNames);
	}

	/**
	 * Return a Collection of <code>QueryDescriptor</code> for queries needed to build a whole IAD dataset (all
	 * attributes), by looking up in the MappingSingleton.
	 * 
	 * It gets attribute name list from <code>MappingSingleton</code>, then calls more general version of itself.
	 * 
	 * @param datasetName
	 *            The name of a IAD dataset.
	 * @return A <code>Collection<QueryDescriptor></code> holding the attribute names to be retrieved
	 * 
	 * @throws SyntaxErrorInMappingException
	 * 
	 */
	public static Collection<QueryDescriptor> getQueryDescriptor(String datasetName)
			throws SyntaxErrorInMappingException {
		Dataset d = null;

		try {
			d = MappingSingleton.getInstance().mapping.getDatasetByName(datasetName);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		Collection<String> attributeNames = d.getAllIADAttributes();

		return getQueryDescriptor(datasetName, attributeNames);
	}

	/**
	 * Computes the set of SQL queries required to extract data from a DBMS to map a IAD dataset. <br>
	 * TODO should be made private (breaks TestQueryCreator); should not look again for the dataset by name.
	 * 
	 * @param dataset
	 *            The name of the IAD dataset.
	 * @param attributeNames
	 *            A <code>Collection<String></code> representing the attributes
	 * 
	 * @return A <code>Collection<QueryDescriptor></code>
	 */
	public static Collection<QueryDescriptor> computeQuerySet(String dataset, Collection<String> attributeNames) {
		Dataset d = null;

		try {
			d = MappingSingleton.getInstance().mapping.getDatasetByName(dataset);
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		String joinClause = d.getJoinClause();

		Collection<QueryDescriptor> result = new ArrayList<QueryDescriptor>();
		Collection<String> LIADTableNames = d.getLIADTables(attributeNames);
		// TODO verify code here for the ==null case; does it make sense to have
		// multiple source tables if there is NO join?
		if (joinClause == null) {
			for (String LIADTableName : LIADTableNames) {
				result.add(new QueryDescriptor(buildQuery(LIADTableName, d, attributeNames), LIADTableName));
			}
		}
		else {
			result.add(new QueryDescriptor(buildQueryWithJoin(LIADTableNames, d, attributeNames, joinClause), d
					.getJoinName()));
		}

		return result;
	}

	/**
	 * Generates an SQL query which does NOT require a Join clause (single source table). Creates the list of
	 * tablename.fields to extract, translated to the DB schema column names, and properly formatted for the DB; add the
	 * from clause. Can limit the query output size during test runs. <br>
	 * TODO Refactor, buildQuery should call / share implementation with buildQueryWithJoin
	 * 
	 * @param LIADTableName
	 *            the name of the LIAD table from which we want to extract the attributes
	 * @param d
	 *            the IAD <code>Dataset</code> to be mapped.
	 * @param attributeNames
	 *            A <code>Collection<String></code> of IAD attributes that has to be mapped.
	 * 
	 * @return The query expressed as a <code>String</code>.
	 */
	private static String buildQuery(String LIADTableName, Dataset d, Collection<String> attributeNames) {
		String queryString = "select ";
		Collection<String> liadColumns = d.getLIADAttributes(LIADTableName, attributeNames);

		// Handle the first column name as a specific case.
		Iterator<String> iter = liadColumns.iterator();
		queryString += LIADTableName + "." + iter.next();

		// Handle the remaining columns.
		while (iter.hasNext()) {
			queryString += ", " + LIADTableName + "." + iter.next();
		}

		// Se non e` necessario effettuare join tra le tabelle LIAD.
		queryString += " from " + LIADTableName;

		// only if we are on a test run
		if (Dynamic.doLimitQueryResults) {
			// TODO report doing this on the log; either here or in the called
			queryString = limitQuerySize(queryString);
			LogST.logP(1, "limitQuerySize called with result :" + queryString + "\n");
		}
		else {
			LogST.logP(1, "limitQuerySize skipped \n");
		}

		return queryString;
	}

	/**
	 * Generates a SQL query for a IAD dataset whose mapping DOES use a join clause (multiple source table). Creates the
	 * list of tablename.fields to extract collecting from each table, translated to the DB schema column names, and
	 * properly formatted for the DB; add the from clause and the JOIN clause passed as argument. Can limit the query
	 * output size during test runs.
	 * 
	 * @param LIADTableNames
	 *            The <code>Collection<String></code> of LIAD tables from which to query against.
	 * @param d
	 *            The IAD <code>Dataset</code> to be mapped.
	 * @param attributeNames
	 *            A <code>Collection<String></code> of IAD attributes that has to be mapped.
	 * @param joinClause
	 *            A <code>String</code> containing the join clause for the query.
	 * 
	 * @return The query expressed as a <code>String</code>.
	 */
	private static String buildQueryWithJoin(Collection<String> LIADTableNames, Dataset d,
			Collection<String> attributeNames, String joinClause) {
		String queryString = "select ";

		Collection<String> liadColumns = new ArrayList<String>();
		for (String LIADTableName : LIADTableNames) {
			Collection<String> liadAtts = d.getLIADAttributes(LIADTableName, attributeNames);
			for (String liadAtt : liadAtts) {
				liadColumns.add(LIADTableName + "." + liadAtt);
			}
		}

		// Handle the first column name as a specific case.
		Iterator<String> iter = liadColumns.iterator();
		queryString += iter.next();

		// Handle the remaining columns.
		while (iter.hasNext()) {
			queryString += ", " + iter.next();
		}

		// NB: join clause is inserted "verbatim".
		queryString += " from " + joinClause;

		// only if we are on a test run
		if (Dynamic.doLimitQueryResults) queryString = limitQuerySize(queryString);

		return queryString;
	}

	/**
	 * Generate and add an extra query clause which limits the size of the query result to a few thousand rows. It must
	 * choose the clause form that is understood by the current DBMS. Since the extra clause may need to be at the
	 * beginning or end of query, we need to mingle with the formed query.
	 * 
	 * Used only during tests to avoid that unlimited join op.s bring down the DBMS. <b>Shall not</b> be used in real
	 * queries, it will produce grossly incorrect results.
	 * 
	 * Warning: needs extra care in checking the correct size of the returned result (off-by-one issues).
	 * 
	 * TODO: Code supports Oracle, MySQL, postgres, SQLServer, but <i>only Oracle syntax</i> has been tested so far.<br>
	 * FIXME: will need changes when multiple drivers can be enabled at once.
	 * 
	 * @param freeQuery
	 *            String with the text of the query we want to add the limit clause to.
	 * @return a String with the final SQL query.
	 */
	private static String limitQuerySize(String freeQuery) {
		TheMatrix config = ConfigSingleton.getInstance().theMatrix;

		Integer maxRows = config.getTestQuerySizeLimit();
		DbDriver dbType = config.getDbDriver();

		return Translator.limitQuerySize(freeQuery, dbType, maxRows);
	}

	/**
	 * Writes a set of queries to a text file.
	 * 
	 * @param querySet
	 *            The <code>QuerySet</code> containing queries to be written on file.
	 * @param scriptName
	 *            The name of script whose execution has implied the creation of the query set.
	 * 
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static void dumpQueries(QuerySet querySet, String scriptName) throws JAXBException, IOException {
		TheMatrix theMatrix = ConfigSingleton.getInstance().theMatrix;

		String queryPath = theMatrix.getPath().getQueries();
		String version = theMatrix.getVersion();
		java.util.Date date = new java.util.Date();

		String filePath = queryPath + "queryset-" + querySet.getIADDataset() + "-" + scriptName + "-" + version + "-"
				+ date.toString() + ".txt";

		FileWriter out = new FileWriter(filePath, false);

		for (QueryDescriptor queryDesc : querySet.getQuery()) {
			out.write(querySet.getIADDataset() + "\n");
			out.write("\ttableName = " + queryDesc.tableName + ",\t query = " + queryDesc.query + "\n");
		}
		out.close();
	}
}
