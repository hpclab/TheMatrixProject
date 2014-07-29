package org.erasmusmc.jerboa.postProcessing;

import java.util.List;

import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

public class RemoveLinesWithNegativeNumbers {

  public static String folder = "/home/schuemie/Research/Jerboa/";
  public static String source = folder + "Prescriptions.txt";
  public static String target = folder + "Prescriptions_fixed.txt";
	public static void main(String[] args) {
		WriteCSVFile out = new WriteCSVFile(target);
		int keepCount = 0;
		int removeCount = 0;
		for (List<String> row : new ReadCSVFile(source)){
			boolean negative = false;
			for (String cell : row)
				if (cell.startsWith("-"))
					negative = true;
			if (negative)
				removeCount++;
			else {
				keepCount++;
				out.write(row);
			}
		}
		out.close();
		
		System.out.println("Keep: " + keepCount + ", Removed: " + removeCount);
	}

}
