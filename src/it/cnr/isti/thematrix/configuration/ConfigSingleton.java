package it.cnr.isti.thematrix.configuration;

import it.cnr.isti.thematrix.configuration.setting.TheMatrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.MissingResourceException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * The TheMatrix configuration file (singleton), supports reading in the
 * file-based configuration and act upon it. Any variable value here must be
 * ignored whenever a Dynamic method/var is defined with the same meaning.
 * 
 * Class is a singleton with two public methods for getting the instance. On
 * first init, the ordinary one without parameters will just init everything and
 * look for files stemming from the current directory. The parameterized one one
 * will set instead the base path for configuration files from its argument. <br>
 * Once the singleton is created, the no-argument constructor will just return
 * it; the one argument shall report an error; for now, trying to set a
 * different path will just write warning to console. <br>
 * 
 * FIXME this behavior is not really needed any longer: just break havoc is the
 * path is provided after first initialization.
 * 
 * Rationale: the base directory should be possible to set from command line, I
 * need the functionality for merging with Neverlang code, but I don't want to
 * access the Dynamic singleton from here now.
 */
public class ConfigSingleton {
	/**
	 * TheMatrix's global information.
	 */
	public TheMatrix theMatrix;

	private static ConfigSingleton instance;
	// this is the same as Dynamic.basePath;
	private static String baseConfigPath = null;

	/**
	 * Method checking whether we already parsed the main configuration file.
	 * 
	 * @return true if singleton configured, i.e. settings.xml has been read.
	 */
	public static boolean configured() { return (instance != null);}
	
	/**
	 * Gets the only instance of this class.
	 * 
	 * @return the instance
	 */
	public static ConfigSingleton getInstance() {
		if (instance == null) {
			instance = new ConfigSingleton();
		}
		return instance;
	}

	/**
	 * Create the only instance of this class; trigger a warning on second attempt and return the existing one.
	 * 
	 * @param path
	 *            the base directory for config and data files; if null, all operations go to the current dir
	 * @return the instance
	 */
	public static ConfigSingleton getInstanceFromPath(String path) {
		if (instance == null) {
			baseConfigPath = path;
			instance = new ConfigSingleton();
		}
		else {
			/** add better error handling code **/
			LogST.logP(0, "WARNING : Trying to reinitialize the TheMatrix ConfigSingleton");

			// " \nfrom path: "
			// + baseConfigPath + "\nto path: " + path
		}
		return instance;
	}

	/**
	 * Private constructor for the singleton pattern.
	 * 
	 * Does not throw JAXBException, turns it into an Error: if an exception is found, we will never get past the first
	 * instancing attempt.
	 * 
	 * TODO: in building the entity it reads in the XML; should perform sanity checks and data transformations
	 * immediately after.
	 * 
	 * @throws MissingResourceException
	 */
	private ConfigSingleton() // throws JAXBException
	{
		String printablePath = baseConfigPath == null ? " <currentdir> " : baseConfigPath;

		if (!(new File(baseConfigPath, "settings.xml")).exists())
			throw new MissingResourceException("ConfigSingleton - manca il file " + printablePath + "settings.xml",
					"File", printablePath + "settings.xml");

		// Read the settings file using JAXB.
		try {
			JAXBContext context = JAXBContext.newInstance(TheMatrix.class);
			Unmarshaller m = context.createUnmarshaller();
			theMatrix = ((TheMatrix) m.unmarshal(new File(baseConfigPath, "settings.xml")));
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new MissingResourceException("ConfigSingleton - errore nel file settings.xml", "File", printablePath
					+ "settings.xml");
		}
		// here sanity checks

	}

}
