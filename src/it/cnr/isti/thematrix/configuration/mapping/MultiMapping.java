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

import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;

/**
 * Class for a mapping descriptor of a multiple mapping, handles the multiMapping XML tag.
 * Extends AbstractMapping.  
 * TODO: document better this class
 * This class interfaces with XML parsing via annotations.
 */
public class MultiMapping extends AbstractMapping
{
	@XmlElement
	private String function;

	@XmlElement
	private Collection<Mapping> mapping;

	@Override
	public String getLIADAttribute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLIADTable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doCheck(ConfigurationMappingContext context)
	{
		// TODO Auto-generated method stub	
	}
}
