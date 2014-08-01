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
package it.cnr.isti.thematrix.configuration.setting;

import it.cnr.isti.thematrix.exception.UnsupportedDatabaseDriverException;
import it.cnr.isti.thematrix.dbms.Kind;
import it.cnr.isti.thematrix.dbms.Kind.DbDriver;

import javax.xml.bind.annotation.XmlElement;

public class DbConnection
{
	@XmlElement
	private String serverName;

	@XmlElement
	private int portNumber;

	@XmlElement
	private String sid;

	@XmlElement
	private String user;

	@XmlElement
	private String type;

	public String getUser()
	{
		return user;
	}

	/*** 
	 * get the type of DB we are connected to
	 */
	public DbDriver getType()
	{
		return  Kind.toDbDriver(type);
	}
	// TODO add here support for the enum type holding the supported types of DB drivers
	// the conversion should be done only once, and throw error on failure
	/*** 
	 * get the server name where DB is stored
	 */
	public String getServerName() {
		return serverName;
	}
	
	/*** 
	 * get the port number to access DB
	 */
	public int getPortNumber() {
		return portNumber;
	}
	
	/*** 
	 * get the sid(db name) we are connected to
	 */
	public String getSid() {
		return sid;
	}
	/**
	 * Gets the connection string to the DBMS.
	 * 
	 * @throws UnsupportedDatabaseDriverException
	 */
	@Deprecated
	public String getConnectionString() throws UnsupportedDatabaseDriverException
	{
		String result = null;

		if (type.equalsIgnoreCase("oracle"))
		{
			result = "jdbc:oracle:thin:@//" + serverName + ":" + portNumber + "/" + sid;
		}
		else
		{
			if (type.equalsIgnoreCase("sqlserver"))
			{
				result = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";instanceName=" + sid;
			}
			else
				if (type.equalsIgnoreCase("mysql"))
				{
					result = "jdbc:mysql://" + serverName + ":" + portNumber + "/" + sid+"?zeroDateTimeBehavior=convertToNull";
				}
				else
				{
					throw new UnsupportedDatabaseDriverException("Driver for " + type + "databases are not supported yet");
				}
		}
		return result;
	}
}
