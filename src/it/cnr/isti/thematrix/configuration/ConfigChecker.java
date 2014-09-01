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

import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;

import java.io.File;
import java.util.MissingResourceException;

import javax.xml.bind.JAXBException;

public class ConfigChecker {

	/****************************
	 * Method to perform some sanity checks about the program configuration.
	 * Handle I/O exceptions and in case throws Error or returns false.
	 * Whenever false is returned, program execution must halt.
	 * 
	 * @return true if all tests are passed
	 */
	public static boolean checkDirs () {
		/* check directory existence and write-ability */
		String mydirs[]= {"iad", "results", "mapping", "scripts",  "queries", "lib" }; //should libs really be there?
		boolean writable[]={ true, true, false, false, true, false};
		int i;
		File temp=null;

		// deal with special cases explicitly contained in Dynamic 
		if (Dynamic.getIadPath()!=null)
			mydirs[0]=Dynamic.getIadPath();
		if (Dynamic.getScriptPath()!=null)
			mydirs[3]=Dynamic.getScriptPath();
		
		try {
			LogST.logP(0,"Directory check");

			for(i=0; i< mydirs.length; i++){
				System.out.println(mydirs[i]);
				temp = new File(mydirs[i]); 
				// temp.exists(); implied by isDirectory()
				if ( !temp.isDirectory() || (writable[i] && !temp.canWrite() )) {
					LogST.logP(0,"Checking installation directories: check for "+mydirs[i]+" directory failed");
					//FIXME should log this to the data file errors too
					return false;
				}
				//				LogST.logP(0,"Matrix Test - "+mydirs[i]+" dir check passed");
				LogST.logP(0,mydirs[i]+" is OK ");
			}
			LogST.logP(0,"Dir Check passed");
			return true;
		} catch (SecurityException e) {
			e.printStackTrace();
//			throw new Error ("I/O error or Security Exception during sanityCheckConfiguration:\n"+e.toString());
			return false; // in case we remove the throw Error
		}
	}
	
	/**
	 * Perform minimal sanity checks on the data structures produced while parsing the xml mapping configuration file.
	 * @return true if checks passed
	 */
	public static boolean checkMapping()
	{
		MappingSingleton mapping = null;
		ConfigurationMappingContext context = new ConfigurationMappingContext();
		context.file = ConfigSingleton.getInstance().theMatrix.getPath().getMapping();
		
		try 
		{
			mapping = MappingSingleton.getInstance();
		} 
		catch (JAXBException e) 
		{
			LogST.errorCustom("ERROR in checkMapping() : JAXBE exception while parsing the mapping file "+context.file+" ; Exception: "+e.toString());
			return false;
		} 
		catch (MissingResourceException e) 
		{
			LogST.errorConfigurationFile(ConfigSingleton.getInstance().theMatrix.getPath().getMapping());
			return false;
		}
	
		// do presence check
		mapping.mapping.doCheck(context);
		
		if ((context.errors > 0) || context.warnings > 0)
			LogST.errorCustom("Found "+context.errors+" errors and "+context.warnings+ " warnings in file "+context.file);
		
		if (context.errors > 0)
		{
			LogST.logGoodbye();
			throw new Error("Configuration file "+context.file+" is not valid.");
		}
			
		return true;
	}
}	

