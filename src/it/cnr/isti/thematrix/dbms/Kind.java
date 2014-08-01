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
