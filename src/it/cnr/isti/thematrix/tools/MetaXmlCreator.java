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
package it.cnr.isti.thematrix.tools;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBException;

import it.cnr.isti.thematrix.common.Enums;
import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.setting.TheMatrix;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;

public class MetaXmlCreator
{

	/**
	 * FIXME fill this javadoc
	 * FIXME refactor code as consequence of the change in Enums.extensionOfFileExists()
	 * 
	 * @param args
	 * @throws JAXBException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static void main(String[] args) throws JAXBException, NoSuchAlgorithmException, IOException
	{
		if (args.length <= 0 || args.length >= 2)
		{
			System.out.println("Matrix MetaXmlCreator - it's mandatory to specify a single CSV file on which create the meta xml");
		}
		else
		{
			
			String fileName = Enums.getBaseNameFile(args[0]);
			/*
			String fileName = "";
  		    if (args[0].endsWith(".csv"))
			{
				fileName = args[0].substring(0, args[0].indexOf(".csv"));
			}
			else
			{
				fileName = args[0];
			}
			*/
			System.out.println("Matrix MetaXmlCreator - start procedure on " + fileName + " file");
			TheMatrix matrix = ConfigSingleton.getInstance().theMatrix;
			File f = new File(matrix.getPath().getIad() + fileName + ".xml");
			if (f.exists())
			{
				System.out.println("Matrix MetaXmlCreator - file MetaXml for " + f.getName() + " exists. Delete it before continue");
			}
			else
			{
				String extension = Enums.extensionOfFileExists(matrix.getPath().getIad(), fileName);
				if (extension != "") 
					f = new File(matrix.getPath().getIad() + fileName +/*".csv"+*/extension);
				if ((!extension.equals("")) && f.exists())
				{
					CSVFile.createMetaXml(matrix.getPath().getIad(), fileName, matrix.getVersion());
					System.out.println("Matrix MetaXmlCreator - MetaXml created: " + fileName + ".xml");
					System.out.println("Matrix MetaXmlCreator - procedure done on " + f.getName() + " file");
				}
				else
				{
					System.out.println("Matrix MetaXmlCreator - file " + f.getName() + " doesn't exists. Import it before continue");
				}
			}
		}
	}
}
