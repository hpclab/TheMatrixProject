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

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Elementary class for an XML ground pair, named after the entries in a recoding table. Protected fields as to allow
 * other classes in the package to access them.
 * 
 * @author massimo
 */
public class Value implements ICheckable
{
	@XmlAttribute
	protected String src;

	@XmlAttribute
	protected String dest;

	@Override
	public void doCheck(ConfigurationMappingContext context) 
	{
		String parent = "<value>";
		
		if ((src == null) || src.trim().isEmpty())
		{
			LogST.errorMappingAttribute(context.file, "src", parent);
			context.errors ++;
		}
			
		if ((dest == null) || dest.trim().isEmpty())
		{
			LogST.errorMappingAttribute(context.file, "dest", parent);
			context.errors ++;
		}
	}

}
