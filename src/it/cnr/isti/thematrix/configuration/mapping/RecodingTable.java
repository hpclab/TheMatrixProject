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

//import java.util.Collection;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ICheckable;

import java.util.Collection;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;

/**
 * Class for a recoding table inside a a mapping descriptor, handles the recodingTable XML tag. This class interfaces
 * with XML parsing via annotations to get most of its fields initialized. The recoding from src to dest values will be
 * used more often, so such an hashmap can be asked to this class. HashMap will be null if no value tags are found,
 * which is an error anyway.
 */
public class RecodingTable implements ICheckable
{
	@XmlElement(name="value")
	private Collection<Value> values;

	// we define the direct HashMap [src-> dest] as it will be used the most often
	private HashMap<String,String> recodingMap = null;
	
	public Collection<Value> getValues()
	{
		return values;
	}
	
	/**
	 * Get the HashMap with src values as Keys and dest values as values. It initializes the hashmap on first call, and
	 * will return a null reference if no values were defined for this recoding table. Note that this is in fact an
	 * error: we should warn the user here.
	 * 
	 * @return
	 */
	public HashMap<String,String> getAsHashMap()
	{
		if (recodingMap == null && values.size()>0)
		{	recodingMap = new HashMap <String, String>(values.size());
			for (Value v: values)  recodingMap.put (v.src,v.dest);
		}
		return recodingMap;
	}

	@Override
	public void doCheck(ConfigurationMappingContext context) 
	{
		String parent = "<recodingTable>";
		
		if (values != null)
		{
			if (values.isEmpty())
			{
				LogST.errorCustom("WARNING: "+parent+" is empty. File: "+context.file);
				context.warnings ++;
			}
			else
			{	
				for (Value v: values)
					v.doCheck(context);
			}
		}
	}
}
