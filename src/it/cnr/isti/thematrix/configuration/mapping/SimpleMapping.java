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

import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;

/**
 * Class for the simple mapping descriptor (straight mapping of IAD and LIAD fields), 
 * handles the simpleMapping XML tag.
 * Extends AbstractMapping.  
 *
 * This class interfaces with XML parsing via annotations.
 */
/**
 * @author massimo
 *
 */
public class SimpleMapping extends AbstractMapping
{	
	@XmlElement
	private RecodingTable recodingTable;
	
	@XmlElement
	private String sourceTable;

	@XmlElement
	private String sourceAttribute;
	
	/**
	 * Gets whether a recoding operation is needed for the mapping. 
	 */
	@Override
	public boolean isRecodingNeeded()
	{
		return recodingTable != null;
	}
	
	/**
	 * Gets the name of the LIAD table.
	 */
	@Override
	public String getLIADTable()
	{
		return sourceTable;
	}
	
	/**
	 * Gets the name of the LIAD attribute.
	 */
	@Override
	public String getLIADAttribute()
	{
		return sourceAttribute;
	}
	
	/**
	 * Get the hashmap implementing the direct recoding of values, src to dest .
	 * @return
	 */
	public HashMap<String,String> getRecodingHashMap(){
		return recodingTable.getAsHashMap();
	}

	@Override
	public void doCheck(ConfigurationMappingContext context)
	{

		String parent = "<simpleMapping name=\""+name+"\">";
		
		if ((name == null) || name.trim().isEmpty())
		{
			LogST.errorMappingAttribute(context.file, "name", "<simpleMapping>");
			context.errors ++;
		}
		
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
		
		if (recodingTable != null) 
		{
			if ((recodingTable.getValues() == null) || (recodingTable.getValues().size() == 0))
			{
				LogST.errorCustom("WARNING: recodingTable in "+parent+" is declared but appears to be empty. File: "+context.file);
				context.warnings ++;
			}
			else
				recodingTable.doCheck(context);
		}
			
	}
}
