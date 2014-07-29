package org.erasmusmc.jerboa.tests;

import java.util.Iterator;
import java.util.List;

import org.erasmusmc.utilities.ReadCSVFile;

public class CountATCinAggregatedData {

	public static String targetATC = null;
	public static String targetEvent = "BE";
	public static void main(String[] args) {
		process("/home/data/IPCI/AggregatebyATC.txt");

	}

	private static void process(String filename) {
		Iterator<List<String>> iterator = new ReadCSVFile(filename).iterator();
		List<String> header = iterator.next();
		int atcCol = header.indexOf("ATC");
		int daysCol = header.indexOf("Days");
		int peCol = header.indexOf("PrecedingEventTypes");
		//CountingSet<String> expCounts = new CountingSet<String>();
		double exposureCount = 0;
		int lineCount = 0;
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			for (String atc : cells.get(atcCol).split("\\+")){
				if (targetATC == null || atc.split(":")[0].equals(targetATC)){
					lineCount++;
					if (!cells.get(peCol).contains(targetEvent)){
						int days = Integer.parseInt(cells.get(daysCol));
						//expCounts.add(atc.split(":")[1],days);
					  exposureCount += days;
					}
				}	
			}
		}
		System.out.println("Lines: " + lineCount+", days: " +exposureCount);
		//expCounts.printCounts();
		
	}

}
