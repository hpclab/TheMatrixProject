/*
 * Copyright (c) Erasmus MC
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
