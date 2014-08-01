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

import javax.xml.bind.annotation.XmlElement;

public class Path
{
	@XmlElement
	private String results;

	/**
	 * Path to disk location where iad .csv files are stored.
	 */
	@XmlElement
	private String iad;

	@XmlElement
	private String mapping;

	@XmlElement
	private String scripts;

	/**
	 * Path to disk location where SQL queries are stored.
	 */
	@XmlElement
	private String queries;

	@XmlElement
	private String lookupTable;

	public String getQueries()
	{
		return queries;
	}

	public String getLookup()
	{
		return lookupTable;
	}

	public String getIad()
	{
		return iad;
	}

	public String getResult()
	{
		return queries;
	}

	public String getMapping()
	{
		return mapping;
	}

	public String getScripts()
	{
		return scripts;
	}

}
