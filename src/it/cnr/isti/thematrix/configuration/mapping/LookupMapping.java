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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Class for a mapping descriptor of a lookup mapping, handles the lookupMapping XML tag.
 * Extends AbstractMapping.  
 * TODO: document better this class
 * This class interfaces with XML parsing via annotations.
 */
public class LookupMapping extends AbstractMapping
{
	@XmlAttribute
	private Boolean inverseLookup;

	@XmlElement
	private String lookupTable;

	@XmlElement
	private String lookupAttribute;

	@XmlElement
	private Mapping mapping;

	/**
	 * Gets whether the inverse lookup operation is permitted.
	 */
	public boolean getInverseLookup()
	{
		return inverseLookup;
	}

	/**
	 */
	public String getLookupTable()
	{
		return lookupTable;
	}
	
	/**
	 */
	public String getLookupAttribute()
	{
		return lookupAttribute;
	}

	/**
	 * Gets the name of the LIAD attribute.
	 */
	@Override
	public String getLIADAttribute()
	{
		return mapping.getSourceAttribute();
	}

	/**
	 * Gets the name of the LIAD table.
	 */
	@Override
	public String getLIADTable()
	{
		return mapping.getSourceTable();
	}

	@Override
	public void doCheck(ConfigurationMappingContext context) 
	{
		String parent = "<lookupMapping name=\""+name+"\">";
		
		if ((name == null) || name.trim().isEmpty())
		{
			LogST.errorMappingAttribute(context.file, "name", "<lookupMapping>");
			context.errors ++;
		}
		
		if (inverseLookup == null)
		{
			LogST.errorMappingElement(context.file, "inverseLookup", parent);
			context.errors ++;
		}
		
		if ((lookupTable == null) || lookupTable.trim().isEmpty())
		{
			LogST.errorMappingElement(context.file, "lookupTable", parent);
			context.errors ++;
		}
		
		if ((lookupAttribute == null) || lookupAttribute.trim().isEmpty())
		{
			LogST.errorMappingElement(context.file, "lookupAttribute", parent);
			context.errors ++;
		}

		if (mapping == null)
		{
			LogST.errorMappingElement(context.file, "mapping", parent);
			context.errors ++;
		}
		else
			mapping.doCheck(context);	
	}
}
