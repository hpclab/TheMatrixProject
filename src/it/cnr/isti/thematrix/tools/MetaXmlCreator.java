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
