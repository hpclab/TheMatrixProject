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

import it.cnr.isti.thematrix.configuration.mapping.reporting.ICheckable;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Class for a mapping descriptor of a IAD attribute, handles the AbstractMapping XML tag contents. 
 * This class interfaces with XML parsing via annotations.
 */
public abstract class AbstractMapping implements ICheckable
{
	@XmlAttribute
	protected String name;

	@XmlAttribute
	protected boolean isSensible;
	
	/**
	 * Gets the name of the IAD attribute.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Gets the privacy value of the IAD attribute.
	 */
	public boolean isSensible()
	{
		return isSensible;
	}

	/**
	 * Check if recoding is needed to map this attribute.
	 */
	public boolean isRecodingNeeded() 
	{
		return false;
	}
	
	/**
	 * Gets the name of the LIAD table.
	 */
	public abstract String getLIADTable();
	
	/**
	 * Gets the name of the LIAD attribute.
	 */
	public abstract String getLIADAttribute();
}
