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
package it.cnr.isti.thematrix.configuration.mapping;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ICheckable;

import javax.xml.bind.annotation.XmlElement;

/**
 * Class for a mapping descriptor, handles the mapping XML tag.
 * TODO: document better this class
 * This class interfaces with XML parsing via annotations.
 */
public class Mapping implements ICheckable
{
	@XmlElement
	private String sourceTable;

	@XmlElement
	private String sourceAttribute;
	
	/**
	 */
	public String getSourceTable()
	{
		return sourceTable;
	}
	
	/**
	 */
	public String getSourceAttribute()
	{
		return sourceAttribute;
	}

	@Override
	public void doCheck(ConfigurationMappingContext context) 
	{
		String parent = "<mapping>";
		
		if ((sourceTable == null) || sourceTable.trim().isEmpty())
		{
			LogST.errorMappingElement(context.file, "sourceTable", parent);
			context.errors ++;
		}

		if ((sourceAttribute == null) || sourceAttribute.trim().isEmpty())
		{
			LogST.errorMappingElement(context.file, "sourceAttribute", parent);
			context.errors ++;
		}
	}
}
