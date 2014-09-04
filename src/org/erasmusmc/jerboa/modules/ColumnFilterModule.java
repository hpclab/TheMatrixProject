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
package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Module for filtering data.
 * @author schuemie
 */
public class ColumnFilterModule extends JerboaModule{

	public JerboaModule input;
	
	/**
	 * The names of the columns that should be kept in the output file.
	 */
	public List<String> allowedColumn = new ArrayList<String>();
	
  /**
   * When true, the dates in the events column are removed. Assumes that the data contains a column 
   * names 'Events'.<BR>
   * default = false
   */
	public boolean stripDateFromEvents = false;
	
	/**
	 * When true, all data outside cohort time is removed. This assumes that the data contains a 
	 * column named 'OutsideCohortTime' with a 1 or 0 value.<BR>
	 * default = false
	 */
	public boolean removeOutsideCohortTime = false;
	
	/**
	 * Name of the output file (relative to the working folder).<BR>
	 * default = ColumnFiltered.txt
	 */
	public String outputFilename = "ColumnsFiltered.txt";
	
	private static final long serialVersionUID = 2546297479346673837L;
	
	protected void runModule(String outputFilename){
		//outputFilename = JerboaObjectExchange.workingFolder + "/" + this.outputFilename; //Override given outputfilename
		process(input.getResultFilename(), outputFilename);
	}

	public void process(String source, String target) {
		List<Integer> columns = new ArrayList<Integer>(allowedColumn.size());
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		WriteCSVFile out = new WriteCSVFile(target);
		List<String> header = iterator.next();
		for (int i = 0; i < header.size(); i++)
			if (allowedColumn.contains(header.get(i)))
				columns.add(i);
		int eventsCol = header.indexOf("Events");
		int outsideCohortTimeCol = header.indexOf("OutsideCohortTime");
		out.write(filter(header,columns));
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			if (stripDateFromEvents)
				cells.set(eventsCol, strip(cells.get(eventsCol)));
			if (!removeOutsideCohortTime || cells.get(outsideCohortTimeCol).length() == 0)
			  out.write(filter(cells,columns));
			ProgressHandler.reportProgress();
		}
		out.close();
	}

	private String strip(String eventsString) {
		if (eventsString.length() == 0)
			return eventsString;
		List<String> strippedEvents = new ArrayList<String>();
		for (String event : eventsString.split("\\+"))
			strippedEvents.add(event.split(":")[0]);
		
		return StringUtilities.join(strippedEvents, "+");
	}

	private List<String> filter(List<String> cells, List<Integer> columns) {
		List<String> result = new ArrayList<String>(columns.size());
		for (Integer column : columns)
			result.add(cells.get(column));
		return result;
	}

}
