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
package it.cnr.isti.thematrix.mapping.utils;

/**
 * This class holds query-related information.
 * 
 */
public class QueryDescriptor 
{
	/**
	 * The SQL query expressed as a <code>String</code>.
	 */
	public String query;

	/**
	 * A <code>String</code> holding the name(s) of the tables involved in the query.
	 */
	public String tableName;

	/**
	 * Creates a new instance of <code>QueryDescriptor</code>.
	 * 
	 * @param query The SQL query expressed as a <code>String</code>.
	 * @param tableName The name of the table.
	 */
	public QueryDescriptor(String query, String tableName) 
	{
		this.query = query;
		this.tableName = tableName;
	}
}
