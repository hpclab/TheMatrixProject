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
package it.cnr.isti.thematrix.mapping.utils;

import it.cnr.isti.thematrix.common.Enums;
import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.filefilter.NameFileFilter;

/**
 * Singleton class to generate temporary file names and register them in a list so that they can be automatically removed at end
 * of execution. As of now, clas supports<br>
 * creating new names for temp files in iad directory <br>
 * adding names of independently created files to the list<br>
 * retrieving the list of active temp files<br>
 * deleting all registered temporary files.<br>
 * Little error checking is done in this class. 
 * A cleaner way could be using File.deleteOnExit(), but we might need to reclaim the disc space before the JVM closes.
 * 
 * TODO Should implement identities to delete only a sublist of the files.
 * 
 * singleton code from http://stackoverflow.com/questions/70689/what-is-an-efficient-way-to-implement-a-singleton-pattern-in-java
 * 
 * FIXME singleton code here is redundant; move to a class with a static initializer block, static methods and no constructor
 * 
 * @author massimo
 * 
 */

public final class TempFileManager {
	private static final TempFileManager INSTANCE= new TempFileManager();
	
	private static ArrayList<String> filenameList= null;
	
	private TempFileManager() {
		if (INSTANCE!= null)
			 throw new IllegalStateException("TempFileManager already instantiated");
	};
	
	static {
		if (filenameList == null ) filenameList = new ArrayList<String>();
	}
	
	public static TempFileManager getInstance() {return INSTANCE;} //used?
	
	/**************************** THE METHODS **************************/
	
	/**
	 * get a File pointing to a fresh new temporary file in the iad directory
	 * @return the new temporary file
	 */
	public static File newTempFile() {
		File tempFile;
		File dir = new File(Dynamic.getResultsPath());
		String suffix = Enums.getFileExtension(Dynamic.bufferCompression);
		try {
			tempFile = java.io.File.createTempFile("test",suffix, dir); 
		} catch (IOException e) { 
			throw new Error ("TempFileManager - Can't open temp file "+dir.toString() +" exception "+e.toString());
		}
		filenameList.add(tempFile.getAbsolutePath());
		return tempFile;
	}
	
	/**
	 * add a file name to the list of temporaries
	 * 
	 * FIXME should distinguish between relative paths and absolute ones
	 * 
	 * @param fullpathname
	 */
	public static void addTempFileName(String fullpathname) {
		filenameList.add(new File(fullpathname).getAbsolutePath());
	}
	
	/**
	 * get the list of temporary files
	 */
	public static ArrayList<String> getTempFilenames() {
		return filenameList;
	}
		
	/**
	 * check if a filename (full path name) is a temporary
	 * @param fullName
	 * @return true if the parameter is in the list of temporary files
	 */
	public static boolean isTemporary(String fullName) {
//		LogST.logP(3, fullName + " is contained in the temporary list?" + filenameList.contains(fullName) );
//		LogST.logP(0, "Temp Files are:"+ getTempFilenames().toString());
		
		return filenameList.contains(fullName);
	}
	
	/**
	 * Check if (path,name) is a temporary file.
	 * @param path
	 * @param name
	 * @return true if the parameters match an element in the list of temporary files
	 */
	public static boolean isTemporary(String path, String name) {
		String fullName = path + (path.endsWith(File.separator)?"":File.separator) + name;
		File f = new File(fullName);
		return isTemporary(f.getAbsolutePath());
	}
	
	/**
	 * Delete all temporary files registered so far and forget them. In case an error is returned, processing is
	 * interrupted; the file triggering the error is the first of the list; it should be safe to call the method can be
	 * called again.
	 * 
	 * @throws SecurityException
	 */
	public static void deleteAllFiles() throws SecurityException, FileNotFoundException
	{
		for (String name : filenameList)
			{
			   File f = new File(name);
			   if (!f.exists()) throw new FileNotFoundException("Temporary file "+name+" not found for deletion");
			   else { 
				   LogST.logP(2, "Deleting Temporary File "+name);
				   f.delete();
//				   filenameList.remove(name); error: we don't have the reference to the iterator here...
			   }
			}
		filenameList = 	new ArrayList<String>();   
	}
	
	
	/**
	 * Remove a temp file from the list, actually deleting it if it exists. Return value is true if the file existed,
	 * false if it was not found. If the file was NOT in the temp file list, raise an error.
	 * 
	 * @param fullname
	 * @return true if the file was found and removed, false if it was not found on disk.
	 */
	public static boolean deleteTempFileQuietly(String fullname)
	{
		boolean found= false;
		if (isTemporary(fullname)) {
			   File f = new File(fullname);
			   if (f.exists())  {
				   found = true;
				   LogST.logP(2, "Deleting Temporary File "+fullname);
				   f.delete();
			   }
			   filenameList.remove(fullname);
			   LogST.logP(2, "Removing Temporary File "+fullname+" from list");
		} else {
			throw new Error ("TempFileManager:deleteTempFileQuietly() - file not in list for file "+fullname);
		}
		return found;
	}
	
	

	/**
	 * Return the path of a file. 
	 * First checks presence in IAD directory, if not return results as path for the file.
	 * It expects a file in the format of filename.extension.
	 * @param filename
	 * @return the path of a file
	 */
	// FIXME: this method should be moved elsewhere
	public static File getPathForFile(String filename)
	{
		File iad = new File(Dynamic.getIadPath());
		File result = new File(Dynamic.getResultsPath());
		
		String[] iad_list = iad.list(new NameFileFilter(filename));
		String[] result_list = result.list(new NameFileFilter(filename));
		
		
		if (iad_list.length > 0 && result_list.length > 0)
			LogST.logP(0, "*** WARNING: found the file "+filename+" both in IAD and RESULTs. Now reading from RESULTS.");
		
		if (result_list.length > 0)
			return result;
		else if (iad_list.length > 0)
			return iad;
		else
			return result;
	}
}
