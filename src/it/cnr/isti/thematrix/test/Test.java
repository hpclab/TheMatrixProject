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

import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.MappingSingleton;
import it.cnr.isti.thematrix.configuration.mapping.IadMapping;
import it.cnr.isti.thematrix.exception.SyntaxErrorInMappingException;
import it.cnr.isti.thematrix.exception.JDBCConnectionException;
import it.cnr.isti.thematrix.exception.UnsupportedDatabaseDriverException;
import it.cnr.isti.thematrix.mapping.MappingManager;

/**
 * @deprecated
 * 
 * OLD Runnable class performing a test on the LIAD to IAD mapping scenario.
 *  
 * This class performs some configuration sanity checks (to be improved):
 * it rebuild the whole set of CSV cache files, either connecting to the DBMS or (if no connection configured) 
 * generating text files with SQL queries.
 * <p>
 * Added command line options: <ul>
 * <li>help on usage, 
 * <li>limit the size of query results, 
 * <li>do not open the DB connection, dump the queries to files straight away.
 * </ul>
 * Diagnostics to System.out, plus messages are appended to the logfile "timeline.txt" 
 * <p>
 * TODO: rename to TestMapping; make the whole test more user friendly, i.e.
 * <ul>
 * <li>
 * 1) download smaller datasets first: EXE, PERSON, HOSP, OUTPAT, DRUG  (now done by reordering the mapping XML config)
 * <li>
 * TODO: 2) download a fragment of the datasets first, then attempt downloading the whole  (do by date? which date?)
 * <li>
 * TODO: 3) attempt downloading specific columns in case of failure on a dataset (check with query creator test)
 * <li>
 * TODO: devise a common mechanism and assumptions for throwing errors
 * </ul>
 * DONE: add true mechanisms for logging
 */
public class Test
{
	/***
	 * @deprecated
	 * 
	 * Outdated - Runnable method to perform the test run (see class documentation). 
	 * 
	 * @param args standards runnable main parameters, currently unused.
	 */
	public static void main(String[] args)
	{
		/*********************************************************
		 * Argument processing
		 */
		Dynamic.getDynamicInfo(); // we need to initialize our singleton

		String[] options = {"--help","--MappingTestRun", "--ignoreDBconnection"}; //same order as entries in the switch below
		int i,j; // i the parameter under check, j the option that it may match
		for (i=0,j=0;i<args.length && i<options.length;j++) {
			if (args[i].equalsIgnoreCase(options[j])) {
				switch (j) {
				case 0:
					usage();
					i++; //not really needed
					System.exit(0);
					break;
				case 1: //this is the model: more options will be like this
					Dynamic.doLimitQueryResults=true;
					i++;
					break;
				case 2: //set a flag that will cause SQL queries always to be saved to a file
					Dynamic.ignoreDBConnection=true;
					i++;
					break;
				default:
					//we didn't find a matching option!
					usage();
					System.exit(1);
					break;
				}
			}
		}
		// at end of for: i is the number of parameters we have matched with options
		if (i>options.length) //we got more parameters than know options...
		{
			usage();
			System.exit(1);
		}
		/*************** Real work  starts here *******************/

		java.util.Date d = new java.util.Date();

		// this enables logging and opens the file
		LogST.getInstance().enable(3);
		BufferedWriter out = LogST.getInstance().getWriter();
		
		System.out.println("Matrix Test - avvio: " + d.toString());

		// LogST.getInstance().startupMessage();
		LogST.logP(0, "-> Configuration check - settings.xml - query size limit is " + 
				ConfigSingleton.getInstance().theMatrix.getTestQuerySizeLimit().toString() +
				"\n");
		// if we got here past ConfigSingleton, then settings.xml has been read
		
		if (!sanityCheckConfiguration(out))
			throw new Error ("Failed initial configuration check");
		
		Dynamic.getDynamicInfo().getTestQuerySizeLimit();
		
		MappingManager mapper = new MappingManager();
		Collection<String> datasetNames = new ArrayList<String>();

		try
		{
			Collection<String> dataName = MappingSingleton.getInstance().mapping.getDatasetNames();
			for (String name : dataName)
			{
				System.out.println("Matrix Test - adding dataset " + name + " to execution");
				datasetNames.add(name);
			}
		}
		catch (JAXBException e1)
		{
			System.out.println("Matrix Test - errore nella lettura del file di mapping");
			e1.printStackTrace();
		}

		try
		{
			mapper.createDataset(datasetNames);
		}
		/*************** Real work  ends here *******************/
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("Matrix Test - algoritmo di codifica MD5 non trovato");
			e.printStackTrace();
		}
		catch (JAXBException e)
		{
			System.out.println("Matrix Test - errore JAXB");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out.println("Matrix Test - file mancante");
			e.printStackTrace();
		}
		catch (SyntaxErrorInMappingException e)
		{
			System.out.println("Matrix Test - file mapping mal formattato");
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			System.out.println("Matrix Test - sql error");
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Matrix Test - driver JDBC non trovato");
			e.printStackTrace();
		}
		catch (JDBCConnectionException e)
		{
			System.out.println("Matrix Test - impossibile creare una connessione JDBC");
			e.printStackTrace();
		}
		catch (UnsupportedDatabaseDriverException e)
		{
			System.out.println("Matrix Test - driver richiesto non ancora supportato");
			e.printStackTrace();
		}

		/*************** Close and cleanup  *******************/

		d = new java.util.Date();
		System.out.println("Matrix test - job done: " + d.toString());

		LogST.logP(0,"-> Test terminato: " + d.toString());
		LogST.getInstance().close();
		
	}
	
	/****************************
	 * Method to perform some sanity checks about the program configuration.
	 * <p>
	 * TODO: refactor to use LogST
	 * 
	 * @param  out BufferedWriter to log messages (can be null, then only print on stdout)
	 * 
	 * @return true if all tests are passed
	 */
	public static boolean sanityCheckConfiguration (BufferedWriter out) {
		/* check directory existence and write-ability */
		String mydirs[]= {"iad", "results", "mapping", "scripts",  "queries", "libs" }; //should libs really be there?
		boolean writable[]={ true, true, false, false, true, false};
		int i;
		File temp=null;

		// deal with special cases explicitly contained in Dynamic 
		if (Dynamic.getIadPath()!=null)
			mydirs[0]=Dynamic.getIadPath();
		if (Dynamic.getScriptPath()!=null)
			mydirs[3]=Dynamic.getScriptPath();
		
		try {
			if (out!= null) {out.write("Dir check ");}

			for(i=0; i< mydirs.length; i++){
				temp = new File(mydirs[i]); 
				// temp.exists(); implied by isDirectory()
				if ( !temp.isDirectory() || (writable[i] && !temp.canWrite() )) {
					System.out.println("Matrix Test - "+mydirs[i]+" directory failed");
					if (out!= null) {out.write(mydirs[i]+" failed\n");}
					return false;
				}
				System.out.println("Matrix Test - "+mydirs[i]+" dir check passed");
				if (out!= null) {out.write(mydirs[i]+" OK ");}
			}
			if (out!= null) {out.write("passed\n");}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error ("I/O error during sanityCheckConfiguration:\n"+e.toString());
		}
		
	}	

	/***
	 * @deprecated
	 * 
	 * Tiny method (for now) to describe command line usage	for the Test class.
	 */
	private static void usage () {
		System.out.println("Command usage:\n\n java <java_options> <classpath_options> <program_main_class> [--help] [--MappingTestRun ]\n" );
		System.out.println("See the TheMatrix and TheMatrixReloaded bash scripts;\nif already using scripts, only pass the optional parameters if allowed.");
	}
}