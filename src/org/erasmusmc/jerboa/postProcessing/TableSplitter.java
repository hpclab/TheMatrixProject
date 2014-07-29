package org.erasmusmc.jerboa.postProcessing;

import java.io.File;

import org.erasmusmc.utilities.ReadTextFile;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteTextFile;

public class TableSplitter {

  /**
   * Reverses the work of the table merger module
   * @param args
   */
	public static void main(String[] args) {
		split("/home/schuemie/Desktop/test/Mergeaggregatedtables.txt");
	}
	
	public static void split(String sourceFile){
		String targetFolder = new File(sourceFile).getParent() + "/";
		split(sourceFile, targetFolder);
	}
	
	public static void split(String sourceFile, String targetFolder){
		WriteTextFile out = null;
		for (String line : new ReadTextFile(sourceFile)){
			if (line.startsWith("*** Start of file ")){
				if (out != null)
					out.close();
				String filename = targetFolder + StringUtilities.findBetween(line, "*** Start of file ", " ***");
				System.out.println("Creating " + filename);
				out = new WriteTextFile(filename);
			} else {
				if (out != null)
					out.writeln(line);
			}
		}
		out.close();
	}

}
