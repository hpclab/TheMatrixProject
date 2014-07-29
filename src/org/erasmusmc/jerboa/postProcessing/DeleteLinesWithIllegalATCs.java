package org.erasmusmc.jerboa.postProcessing;

import java.util.Iterator;
import java.util.List;

import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class DeleteLinesWithIllegalATCs {
	
	public static boolean verbose = false;
	public static String deletedLinesFile;
	
	public static void main(String[] args){
		//String folder = "/home/data/Pooled/Version 3.0 (Silver)/NoRemap/";
		//process(folder+"PooledStartProfiles.csv", folder+"PooledStartProfilesLegalATCs.csv");
		//process(folder+"PooledRRLevel7.csv", folder+"PooledRRLevel7LegalATCs.csv");
		
		//String folder = "x:/";
		//process(folder+"analysisLevel7PharmoMinEvents0.csv", folder+"RelativeRisks.csv");
		
		String folder = "/home/data/Pooled/Gold/";
		//deletedLinesFile = folder + "deleted.csv";
		process(folder+"PooledStartProfilesLevel4.csv", folder+"FixLEOPARD/PooledStartProfilesLevel4.csv");
	}
	
	
  public static void process(String source, String target){
  	WriteCSVFile outDeleted = null;
  	if (deletedLinesFile != null)
  		outDeleted = new WriteCSVFile(deletedLinesFile);
  	WriteCSVFile out = new WriteCSVFile(target);
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next();
		int atcCol = StringUtilities.caseInsensitiveIndexOf("ATC", header);
		out.write(header);
		if (outDeleted != null)
			outDeleted.write(header);
		int startCount = 0;
		int endCount = 0;
		while (iterator.hasNext()){
			startCount++;
			List<String> cells = iterator.next();
			String atcString = cells.get(atcCol);
			boolean legal = true;
	  	for (String atcCode : atcString.split("\\+"))
	  		if ((atcCode.length() != 0) && !isLegalATC(new ATCCode(atcCode).atc)){
	  			legal = false;
	  			if (verbose)
	  			  System.out.println("Illegal: " + atcCode);
	  			if (outDeleted != null)
	  				outDeleted.write(cells);
	  			break;
	  		}
	  	if (legal){
	  		out.write(cells);
	  		endCount++;
	  	}
		}		
		out.close();
		if (outDeleted != null)
			outDeleted.close();
		System.out.println("Before filtering: " + startCount + " rows, after filtering: " + endCount + " rows.");
  }

	public static boolean isLegalATC(String atc) {
		if (atc.length() == 7)
		  if (Character.isLetter(atc.charAt(0)) && legalStart(atc.charAt(0)))
		  	if (Character.isDigit(atc.charAt(1)))
		  		if (Character.isDigit(atc.charAt(2)))
		  			if (Character.isLetter(atc.charAt(3)))
		  				if (Character.isLetter(atc.charAt(4)))
		  					if (Character.isDigit(atc.charAt(5)))
		  						if (Character.isDigit(atc.charAt(6)))
		  							return true;
		return false;
	}


	private static boolean legalStart(char ch) {
		return (ch == 'A' || ch == 'B' || ch == 'C' || ch == 'D' || ch == 'G' || ch == 'H' || ch == 'J' || ch == 'L' || ch == 'M' || ch == 'N' || ch == 'P' || ch == 'R' || ch == 'S' || ch == 'V');
	}
}
