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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.erasmusmc.jerboa.modules.RelativeRiskModule;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

public class TimeSeriesRRAnalysis {

	/**
	 * Period range. The Relative risk is calculated using data up to the selected year.
	 */
	public static int startYear = 2001;
	public static int endYear = 2007; 
	
	public static boolean inventoryOnly = false;
	
	private int colCount;
	
	
	public static void main(String[] args) {
		TimeSeriesRRAnalysis analysis = new TimeSeriesRRAnalysis();
		analysis.process("/home/data/IPCI/AggregatebyATC.txt", "/home/data/IPCI/RRbyYear.txt");
	}
	
	public void process(String source, String target){
		createYearInventory(source);
		if (!inventoryOnly){
			Map<Integer, String> year2filename = createRelativeRiskFiles(source,target);
			merge(year2filename, target);
		}
	}
	
	private void merge(Map<Integer, String> year2filename, String target) {
		System.out.println("Merging into " + target);
		Map<String, List<String>> drugNameEvent2Line = new HashMap<String, List<String>>();
		colCount = 0;
		List<Integer> years = new ArrayList<Integer>(year2filename.keySet());
		Collections.sort(years);
		for (Integer year : years)
			colCount += addFile(year2filename.get(year), year, drugNameEvent2Line);
		
		WriteCSVFile out = new WriteCSVFile(target);
		List<String> header = new ArrayList<String>();
		header.add("ATC");
		header.add("Name");
		header.add("EventType");
		List<String> colNames = drugNameEvent2Line.get("header");
		List<String> colNamesSorted = new ArrayList<String>(colNames);
		Collections.sort(colNamesSorted);
		List<Integer> colIndices = new ArrayList<Integer>();
		for (String colName : colNamesSorted)
			colIndices.add(colNames.indexOf(colName));
		header.addAll(colNamesSorted);
		out.write(header);
		for (Map.Entry<String, List<String>> entry : drugNameEvent2Line.entrySet())
			if (!entry.getKey().equals("header")){
				List<String> cells = new ArrayList<String>();
				String[] drugNameEvent = entry.getKey().split("\t");
				cells.add(drugNameEvent[0]);
				cells.add(drugNameEvent[1]);
				cells.add(drugNameEvent[2]);
				List<String> moreCells = entry.getValue();
				for (Integer colIndex : colIndices)
					if (colIndex >= moreCells.size())
						cells.add("");
					else
					cells.add(moreCells.get(colIndex));
				out.write(cells);
			}
		out.close();		
	}

	private int addFile(String filename, Integer year,	Map<String, List<String>> drugNameEvent2Line) {
		Iterator<List<String>> iterator = new ReadCSVFile(filename).iterator();
		List<String> header = iterator.next();
		int atcCol = header.indexOf("ATC");
		int eventTypeCol = header.indexOf("EventType");
		int nameCol = header.indexOf("Name");
		header.remove("ATC");
		header.remove("EventType");
		header.remove("Name");
		for (int i = 0; i < header.size(); i++)
			header.set(i, header.get(i)+"("+year+")");
		add("header",header,drugNameEvent2Line);
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			String atc = cells.get(atcCol);
			String eventType = cells.get(eventTypeCol);
			String name = cells.get(nameCol);
			String drugNameEvent = atc + "\t" + name + "\t" + eventType;
			cells.remove(atc);
			cells.remove(eventType);
			cells.remove(name);
			add(drugNameEvent,cells,drugNameEvent2Line);
		}
		return header.size();
	}

	private void add(String drugNameEvent, List<String> cells, Map<String, List<String>> drugNameEvent2Line) {
		List<String> line = drugNameEvent2Line.get(drugNameEvent);
		if (line == null){
			line = new ArrayList<String>();
			for (int i = 0; i < colCount; i++)
				line.add("");
			drugNameEvent2Line.put(drugNameEvent, line);
		}
		line.addAll(cells);
	}

	private Map<Integer, String> createRelativeRiskFiles(String source, String target){
		String folder = new File(target).getParent() + "/";
		Map<Integer, String> year2filename = new HashMap<Integer, String>();
		for (int year = startYear; year <= endYear; year++){
			String tempFilename = folder + "AggregatedbyATC_"+year+".csv";
			filterAfterYear(source,tempFilename, year);
			String filename = folder + "RelativeRisk" + year + ".csv";
			RelativeRiskModule module = new RelativeRiskModule();
			module.minEvents = 5;
			System.out.println("Generating " + filename);
			module.process(tempFilename, filename);
		  year2filename.put(year, filename);
		}
		
		return year2filename;
	}

	private void filterAfterYear(String source, String target,	int targetYear) {
		System.out.println("Generating " + target);
		WriteCSVFile out = new WriteCSVFile(target);
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next();
		out.write(header);
		int yearCol = header.indexOf("Year");
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			int year = Integer.parseInt(cells.get(yearCol));
			if (year < targetYear)
				out.write(cells);
		}
		out.close();
	}

	private void createYearInventory(String source) {
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next();
		int yearCol = header.indexOf("Year");
		int daysCol = header.indexOf("Days");
		int atcCol = header.indexOf("ATC");
		Map<String, LongCount> year2count = new HashMap<String, LongCount>();
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			String atc = cells.get(atcCol);
			if (atc.length() == 0){
			  long days = Long.parseLong(cells.get(daysCol));
			  String year = cells.get(yearCol);
			  LongCount count = year2count.get(year);
			  if (count == null){
			  	count = new LongCount();
			  	year2count.put(year, count);
			  }
			  count.count += days;
			}
		}
		List<String> years = new ArrayList<String>(year2count.keySet());
		Collections.sort(years);
		System.out.println("Available data:");
		for (String year : years)
			System.out.println(year + "," + year2count.get(year).count);
	}

	private static class LongCount {
		long count = 0;
	}
}
