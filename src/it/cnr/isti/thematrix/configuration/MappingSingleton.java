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
