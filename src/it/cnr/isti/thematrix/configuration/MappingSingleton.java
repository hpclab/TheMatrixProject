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
package it.cnr.isti.thematrix.configuration;

import it.cnr.isti.thematrix.configuration.mapping.IadMapping;

import java.io.File;
import java.util.MissingResourceException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * The TheMatrix LIAD to IAD mapping file.
 * 
 * It is a singleton.
 */
public class MappingSingleton
{
	/**
	 * IadMapping's global information.
	 */
	public IadMapping mapping;

	private static MappingSingleton instance;

	/**
	 * Gets the only instance of this class.
	 * 
	 * @throws JAXBException
	 */
	public static MappingSingleton getInstance() throws JAXBException, MissingResourceException
	{
		if (instance == null)
		{
			instance = new MappingSingleton();
		}

		return instance;
	}

	// Private constructor, pattern Singleton.
	private MappingSingleton() throws JAXBException, MissingResourceException
	{
		// Read the settings file using JAXB.
		JAXBContext context = JAXBContext.newInstance(IadMapping.class);
		Unmarshaller m = context.createUnmarshaller();

		String mappingPath = ConfigSingleton.getInstance().theMatrix.getPath().getMapping();
		if (new File(mappingPath).exists())
		{
			mapping = ((IadMapping) m.unmarshal(new File(mappingPath)));
		}
		else
		{
			// TODO: launch error from the new log file
			throw new MissingResourceException("MappingSingleton() - missing mapping file, ", "file", mappingPath);
		}
	}
}
