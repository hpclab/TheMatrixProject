package it.cnr.isti.thematrix.dbms;

/**
 * Class to hold the minimal information needed to decide what is the kind of DBMS we are attached to, including the enum with supported database types. 
 * Refactoring DBMS support to make it more modular and more easily support multiple DBs.
 * 
 * @author massimo
 *
 */
public class Kind {
	
	/***
	 * enum used to abstract the driver info after parsing the XML
	 * FIXME should throw error if unrecognized
	 */
	public enum DbDriver { noDriver, oracleDriver, mysqlDriver, postgresqlDriver, sqlserverDriver, sasPlainDriver};

	static public DbDriver toDbDriver(String s) {
		if (s.equalsIgnoreCase("oracle")) return DbDriver.oracleDriver;
		if (s.equalsIgnoreCase("mysql")) return DbDriver.mysqlDriver;
		if (s.equalsIgnoreCase("postgresql")) return DbDriver.postgresqlDriver;
		if (s.equalsIgnoreCase("sqlserver")) return DbDriver.sqlserverDriver;
		if (s.equalsIgnoreCase("sas")) return DbDriver.sasPlainDriver;
		if (s.equalsIgnoreCase("none")) return DbDriver.noDriver;
		//FIXME here we should return an error
		return DbDriver.noDriver;
	}
	
}
