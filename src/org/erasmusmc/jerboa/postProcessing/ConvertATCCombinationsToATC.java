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
package org.erasmusmc.jerboa.postProcessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.utilities.ReadCSVFileWithHeader;
import org.erasmusmc.utilities.Row;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class ConvertATCCombinationsToATC {

	//public static String folder = "C:/home/data/Simulated/";
	public static String folder = "X:/";
	public static String sourceFile = folder + "AggregatebyATCcombination.txt";
	public static String targetFile = folder + "AggregatebyATCnew.txt";
	public static boolean level7Only = true;
	public static boolean addCombinations = false; //Adds combinations of two ATCS as separate ATC codes
	public static boolean singleATCsOnly = true; //Only output lines when patient is using 1 drug
	
	
	public static void main(String[] args) {
		WriteCSVFile out = new WriteCSVFile(targetFile);
		out.write(generateHeader());
		Map<String, Data> key2data = new HashMap<String, Data>();
		for (Row row : new ReadCSVFileWithHeader(sourceFile)){
			//Store for background data:
			String key = row.get("AgeRange")+"\t"+row.get("Gender")+"\t"+row.get("PrecedingEventTypes");
			Data data = key2data.get(key);
			if (data == null){
				data = new Data();
				key2data.put(key,data);
			}
			data.days += Long.parseLong(row.get("Days"));
			String events = row.get("Events");
			if (events.length() != 0)
			  for (String event : events.split("\\+")){
			    String[] eventParts = event.split(":");	
			  	data.events.add(eventParts[0],Integer.parseInt(eventParts[1]));
			  }
			//Write exposures:
			String atcString = row.get("ATC");
			if (atcString.length() != 0){
				if (!singleATCsOnly || atcString.split("\\+").length == 1)
					for (String atc : atcString.split("\\+"))
						if (!level7Only || atc.length() == 7){
							List<String> cells = new ArrayList<String>();
							cells.add(atc);
							cells.add(row.get("AgeRange"));
							cells.add(row.get("Gender"));
							cells.add(row.get("Days"));
							cells.add(row.get("Events"));
							cells.add(row.get("PrecedingEventTypes"));
							out.write(cells);
						}
			}
			if (addCombinations){
				List<String> atcs = new ArrayList<String>();
				for (String atc : atcString.split("\\+"))
					if (!level7Only || atc.length() == 7)
						atcs.add(atc);
				if (atcs.size() != 1){
					Collections.sort(atcs);
					for (int i = 0; i < atcs.size()-1; i++)
						for (int j=i+1; j < atcs.size(); j++){
							String atcCombination = atcs.get(i)+"&"+atcs.get(j);
							List<String> cells = new ArrayList<String>();
							cells.add(atcCombination);
							cells.add(row.get("AgeRange"));
							cells.add(row.get("Gender"));
							cells.add(row.get("Days"));
							cells.add(row.get("Events"));
							cells.add(row.get("PrecedingEventTypes"));
							out.write(cells);
						}
				}
			}
		}
		
		//Write background:
		for (String key : key2data.keySet()){
			Data data = key2data.get(key);
			String[] keyParts = key.split("\t");
			List<String> cells = new ArrayList<String>();
			cells.add("");
			cells.add(keyParts[0]);//Agerange
			cells.add(keyParts[1]);//Gender
			cells.add(Long.toString(data.days));
			List<String> events = new ArrayList<String>();
			for (String eventType : data.events)
				events.add(eventType + ":" + data.events.getCount(eventType));
			cells.add(StringUtilities.join(events, "+"));
			if (keyParts.length > 2)
			  cells.add(keyParts[2]);//Preceding event types
			else
				cells.add("");
			out.write(cells);
		}
		out.close();
	}

	private static List<String> generateHeader() {
		List<String> header = new ArrayList<String>();
		header.add("ATC");
		header.add("AgeRange");
		header.add("Gender");
		header.add("Days");
		header.add("Events");
		header.add("PrecedingEventTypes");
		return header;
	}

	private static class Data{
		long days = 0;
		CountingSet<String> events = new CountingSet<String>();
	}

}
