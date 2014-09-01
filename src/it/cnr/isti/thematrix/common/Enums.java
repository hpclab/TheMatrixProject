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
package it.cnr.isti.thematrix.common;

import it.cnr.isti.thematrix.configuration.LogST;

import java.io.File;
import java.util.ArrayList;

public class Enums {

	public static enum ChecksumType{
		NONE, MD5;
	}
	
	public static enum CompressionType{
		NONE, ZIP, GZIP;
	}
	
	/**
	 * Method for get the proper extension according to compression type.
	 * 
	 * @param compression if null, it will cause an empty extension.
	 * 
	 * @return extension as a String, if recognized, empty otherwise.
	 */
	public static String getFileExtension(CompressionType compression){
		String fileExtension = "";
		if (compression != null) {
			switch (compression) {
				case NONE :
					fileExtension = ".csv";
					break;
				case GZIP :
					fileExtension = ".csv.gzip";
					break;
				case ZIP :
					fileExtension = ".csv.zip";
					break;
			}
		}
		return fileExtension;
	}
	
	/**
	 * Find a file on disk, and parse its extension to detect its compression type. If the file is not found, or the
	 * extension is not recognized, return null instead. 
	 * 
	 * @param path
	 * @param filename
	 * @return the type of compression
	 */
	public static CompressionType parseCompressionExtension(String path, String filename) {
		String baseName = getBaseNameFile(filename);
		String extension = extensionOfFileExists(path,baseName);
		for (CompressionType c : CompressionType.values()) {
			if (extension.equals(getFileExtension(c))) return c;
		}
		return null;
	}

	/**
	 * Parse the extension of a filename to detect its compression type. NO access to disk is performed.
	 * 
	 * @param filename
	 * @return null if no extension is recognized
	 */
	public static CompressionType parseCompressionExtension(String filename) {
		for (CompressionType c : CompressionType.values()) {
			if (filename.endsWith(getFileExtension(c))) return c;
		}
		return null;
	}

	
	/**
	 * Find a file on disk, and check that it exists with a given compression type, or find it if no type is given. If
	 * required compression type and suffix do not match, return null. If they match, or only one is found, continue.
	 * Parse file actual extension to detect its compression type.
	 * 
	 * If the file is not found, or the extension is not recognized, or is not the one we were looking for return null
	 * instead. 
	 * 
	 * Can throw RuntimeException if more files are found for the same basename.
	 * 
	 * @param path
	 * @param filename
	 * @param wanted
	 *            the compression we want, or null if unknown
	 * @return the compression type found, or null if file not found or not recognized.
	 */
	public static CompressionType checkFileExistence (String path, String filename, CompressionType wanted) {

		String baseName = getBaseNameFile(filename);
		CompressionType wanted2 = parseCompressionExtension(filename);
		if (wanted != null && wanted2 != null && wanted!=wanted2) {
			LogST.logP(0, "");
			return null; // conflicting requirements!
		} 
		if (wanted==null) wanted = wanted2; // collapse both specs into one

		// we do this anyway, to check for name clashes
		CompressionType extension = cExtensionOfFileExists(path,baseName); 

		if (wanted==null) {		// cercare una copia qualsiasi
			if (extension!=null) return extension;
		} else {
			// cercarne una copia specifica
			if (extension==wanted) return extension;
		}
		// no match !!
		LogST.logP(0, "e' diverso");
		return null;
	}
	
	/**
	 * Method for retrieve only file name without the extension
	 * @param completeFileName
	 * 
	 * @return the basename of the file. 
	 */
	public static String getBaseNameFile(String completeFileName){
		String baseName=completeFileName;
		if (!completeFileName.endsWith(".csv")){
			if(completeFileName.endsWith(".csv.gzip")){
				baseName = completeFileName.substring(0, completeFileName.length() - 9);
			}else if (completeFileName.endsWith(".csv.zip")){
				baseName = completeFileName.substring(0, completeFileName.length() - 8);
			}
		}else{
			baseName= completeFileName.substring(0, completeFileName.length() - 4);
		}
		return baseName;
	}
	
	/**
	 * Method for get an ArrayList with the supported extension of a file.
	 * 
	 * TODO when no longer used, may be deprecated
	 * 
	 * @return ArrayList with the supported extension. 
	 */
	public static ArrayList<String> getExtensions(){
		CompressionType[] compressions = Enums.CompressionType.values();
		ArrayList<String> extensions = new ArrayList<String>();
		for(int i=0; i<compressions.length; i++){
			extensions.add(getFileExtension(compressions[i]));
		}
		return extensions;
	}
	
	/**
	 * FIXME correct docs
	 * Method for retrieving the correct extension of a file on disk, depending on the compression methods;returns empty
	 * string if file is not found.
	 * 
	 * Throws an Error() if several copies of the file exist with different extensions.
	 * 
	 * TODO rewrite a bit to use the Enum.values() and a for on those
	 * TODO launch a subclass of RuntTime Exception instead
	 * 
	 * @param path
	 * @param filename 
	 * 
	 * @return the extension of the file on disk, if it exists.
	 */
	public static CompressionType cExtensionOfFileExists(String path, String filename){
		CompressionType exten=null;
		int howMany = 0;
		for(CompressionType c : CompressionType.values()){
			if (new File(path + filename + getFileExtension(c)).exists()){
				howMany++;
				exten=c;
			}
		}
		if (howMany == 0) return null;
		if (howMany != 1) throw new Error("ERROR Enums.extensionOfFileExists(): base filename "+filename+" duplicated");

		return exten;
	}

	/**
	 * Method for retrieving the correct extension of a file on disk, depending on the compression methods;returns empty
	 * string if file is not found.
	 * 
	 * Throws an Error() if several copies of the file exist with different extensions.
	 * 
	 * TODO rewrite a bit to use the Enum.values() and a for on those TODO launch a subclass of RuntTime Exception
	 * instead
	 * 
	 * @param path
	 * @param filename 
	 * 
	 * @return the String extension of the file on disk, if it exists, or null.
	 */
	public static String extensionOfFileExists(String path, String filename) {
		return getFileExtension(cExtensionOfFileExists(path, filename));
	}
}