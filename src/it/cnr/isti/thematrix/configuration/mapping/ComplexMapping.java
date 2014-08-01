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
 * Class for a IAD dataset field mapped on a LIAD field with a complex mapping 
 * (where a lookup operation is needed), handles the ComplexMapping XML tag.
 * This class interfaces with XML parsing via annotations.
 */
public class ComplexMapping extends AbstractMapping
{
	/**
	 * deprecated, we are not supporting any value other than false
	 */
	@XmlAttribute
	private Boolean inverseLookup;

	@XmlElement
	private String function;
	
	@XmlElement
	private LookupOperation lookupOperation;
	
	/**
	 * FIXME
	 * 
	 * quindi l'idea potrebbe essere:
	 * 
	 * definire un elemento che puoi essere { lookupoperation | <smple fielde reference>   }
	 * e poi una collection di questi elementi , che diventano gli argomenti della funzione (unica)
	 * 
	 */
	
	
	/**
	 * Gets whether the inverse lookup operation is permitted.
	 */
	public boolean getInverseLookup()
	{
		return inverseLookup;
	}
	
	/**
	 * Gets the name of the function applied after each lookup to obtain the resulting value.
	 */
	public String getFunction()
	{
		return function;
	}
	
	/**
	 * Gets a description for the lookup operation involved in the mapping.
	 */
	public LookupOperation getLookupOperation()
	{
		return lookupOperation;
	}
	
	/**
	 * Gets the name of the LIAD table.
	 */
	@Override
	public String getLIADTable()
	{
		return lookupOperation.getMapping().getSourceTable();
	}
	
	/**
	 * Gets the name of the LIAD attribute.
	 */
	@Override
	public String getLIADAttribute()
	{
		return lookupOperation.getMapping().getSourceAttribute();
	}

	@Override
	public void doCheck(ConfigurationMappingContext context) 
	{
		String parent = "<complexMapping name=\""+name+"\">";
		
		if ((name == null) || name.trim().isEmpty())
		{
			LogST.errorMappingAttribute(context.file, "name", "<complexMapping>");
			context.errors ++;
		}
		
		if ((function == null) || function.trim().isEmpty())
		{
			LogST.errorMappingElement(context.file, "function", parent);
			context.errors ++;
		}
		
		if (inverseLookup == null)
		{
			LogST.errorMappingElement(context.file, "inverseLookup", parent);
			context.errors ++;
		}
	
		if (lookupOperation == null)
		{
			LogST.errorMappingElement(context.file, "lookupOperation", parent);
			context.errors++;
		}
		else
			lookupOperation.doCheck(context);
	}
}
