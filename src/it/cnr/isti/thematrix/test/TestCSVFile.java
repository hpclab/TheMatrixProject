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
