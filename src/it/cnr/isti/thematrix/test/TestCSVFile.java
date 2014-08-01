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
package it.cnr.isti.thematrix.test;

import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.setting.TheMatrix;
import it.cnr.isti.thematrix.mapping.creator.ValueRemapper;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBException;

/**
 * Tests the usage of a <code>CSVFile</code>.
 */
public class TestCSVFile
{
	public static void main(String[] args) throws JAXBException, IOException, NoSuchAlgorithmException
	{
		/*********************************************************
		 * Argument processing
		 */
		Dynamic.getDynamicInfo(); // we need to initialize our singleton
		
		System.out.println("Test letture file csv");
		TheMatrix matrix = ConfigSingleton.getInstance().theMatrix;
		String TheFile = "DRUG";
		
		String path = matrix.getPath().getIad();
		String version = matrix.getVersion();
		
		System.out.println("File esistente: " + CSVFile.checkExistence(path, TheFile) + " - File valido: "
				+ CSVFile.validateCheckSum(path, TheFile));
		
		CSVFile csv = new CSVFile(path, TheFile, version);
		while (csv.hasNext())
		{
			csv.loadBatch(20000);
			csv.saveTo(path, "prova", true);
		}
		CSVFile.createMetaXml(path, "prova", matrix.getVersion());
		
		System.out.println("Finito");
	}
}
