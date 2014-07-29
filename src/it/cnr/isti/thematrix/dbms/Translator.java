package it.cnr.isti.thematrix.dbms;

import it.cnr.isti.thematrix.exception.UnsupportedDatabaseDriverException;
import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.setting.TheMatrix;
import it.cnr.isti.thematrix.dbms.Kind.DbDriver;

/**
 * Class providing various substrings and (mostly static) methods to support localization to different databases.
 * 
 * FIXME support for SAS, PostgreSQL is incomplete
 * 
 * @author massimo
 *
 */
public class Translator {

	
	/**
	 * Gets the connection string to the DBMS.
	 * 
	 * @throws UnsupportedDatabaseDriverException
	 */
	public static String getConnectionString(DbDriver type, String serverName, String portNumber, String sid) throws UnsupportedDatabaseDriverException
	{
		String result = null;
		switch (type)
		{
			case oracleDriver:
				result = "jdbc:oracle:thin:@//" + serverName + ":" + portNumber + "/" + sid;
				break;
			case mysqlDriver:
				result = "jdbc:mysql://" + serverName + ":" + portNumber + "/" + sid+"?zeroDateTimeBehavior=convertToNull";
				break;
			case postgresqlDriver:
				throw new UnsupportedDatabaseDriverException("Driver for " + type + "databases are not supported yet");
				//break;
			case sqlserverDriver:
				result = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";instanceName=" + sid;
				break;
			case sasPlainDriver:
				/* a sas dataset has a two part name of the form  library.datasetname 
				 * for now we assume the SAS sid tag contains both parts together, 
				 * as we will need to use it that way in the SQl statements
				 * 
				 * HOWEVER it seems the library.table name should be set in the properties
				 */
				result = "jdbc:sasiom://" + serverName + ":" + portNumber /* + library name . dataset name*/;
				throw new UnsupportedDatabaseDriverException("Driver for " + type + "databases are not supported yet");
				//break;
			case noDriver:
				result = "";
				break;
			default: // this should be unreachable
				throw new UnsupportedDatabaseDriverException("Driver for " + type + "databases are not supported yet");
		}
		return result;
	}

	/**
	 * gets the JDBC driver name for a db kind.
	 * @param type enum-value of DB needed.
	 * @return the string with the JDBC class name
	 * @throws UnsupportedDatabaseDriverException
	 */
	public static String getJDBCDriverName (DbDriver type) throws UnsupportedDatabaseDriverException
	{
		String result = null;
		switch (type)
		{
			case oracleDriver:
				result = "oracle.jdbc.driver.OracleDriver";
				break;
			case mysqlDriver:
				result = "com.mysql.jdbc.Driver";
				break;
			case postgresqlDriver:
				throw new UnsupportedDatabaseDriverException("Driver for " + type + "databases are not supported yet");
				//break;
			case sqlserverDriver:
				result = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
				break;
			case sasPlainDriver:
				/**
				 * SAS has two kinds of internal drivers with different class names  <br>
				 * IOM driver 			com.sas.rio.MVADriver <br>
				 * SAS/SHARE driver 	com.sas.net.sharenet.ShareNetDriver  <br>
				 */
				result = "com.sas.rio.MVADriver";
//				throw new UnsupportedDatabaseDriverException("Driver for " + type + "databases are not supported yet");
				break;
			case noDriver:
				throw new UnsupportedDatabaseDriverException("Driver for " + type + "databases are not supported yet");
				//break;
			default: // this should be unreachable
				throw new UnsupportedDatabaseDriverException("Driver for " + type + "databases are not supported yet");
		}
		return result;

	}

	// methods to generate the nonstd parts of queries

	// we need a method to craft/return the string limiting query result to N rows
	
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
	 * TODO: Code supports Oracle, MySQL, postgres, SQLServer, but <i>only Oracle syntax</i> has been tested so far. <br>
	 * FIXME: not all drivers are supported <br>
	 * 
	 * @param freeQuery
	 *            String with the text of the query we want to add the limit clause to.
	 * @return a String with the final SQL query.
	 */
	public static String limitQuerySize(String freeQuery, DbDriver dbType, Integer maxRows) {

//		TheMatrix config = ConfigSingleton.getInstance().theMatrix;
//		Integer maxRows = config.getTestQuerySizeLimit();
//		DbDriver dbType = config.getDbDriver();

		LogST.logP(0, "WARNING : SQL query-size test limit is enabled, do not trust script execution results.");
		// test incompleto dei rami dello switch

		switch (dbType) {
			case oracleDriver :
				return freeQuery + " WHERE ROWNUM <= " + maxRows.toString();
			case sqlserverDriver :
				/*
				 * SQLSERVER pre- 2012 WHERE row_number BETWEEN @start_row AND
				 * 
				 * @end_row
				 * 
				 * see http://stackoverflow.com/questions/2135418/equivalent-of-limit -and-offset-for-sql-server see
				 * http://msdn.microsoft.com/it-it/library/ms188774.aspx
				 */
				// return " WHERE ROW_NUMBER BETWEEN 0 AND " +
				// maxRows.toString();
				// we use ROWCOUNT as it should still be supported for select
				// statement even in SLQServer 2012
				return "SET ROWCOUNT " + maxRows.toString() + "; " + freeQuery;
			case mysqlDriver :
			case postgresqlDriver :
				/*
				 * MySQL, PostgreSQL : use limit clause see http://dev.mysql.com/doc/refman/5.0/en/select.html see
				 * http://www.postgresql.org/docs/7.3/static/queries-limit.html
				 */
				return freeQuery + " LIMIT " + maxRows.toString();
			case noDriver :
			case sasPlainDriver:
			default :
				/* If we got here, there was unimplemented code on our path */
				throw new Error("Unexpected/unknown DB driver " + dbType.toString() + " in limitQuerySize()");
		}
	}

/***
 * reference info for SAS 92:
 * http://support.sas.com/documentation/cdl/en/jdbcref/59666/HTML/default/viewer.htm#n19z6p7zgfflrwn1in8swhevsdwi.htm	
 */
	
	
}
