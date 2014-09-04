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

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.EventFileWriter;
import org.erasmusmc.utilities.FileSorter;

public class ShiftEventDateModule extends JerboaModule {

	public JerboaModule events;
	
	/**
	 * Keep both shifted and original events?<BR>
	 * default = false 
	 */
	public boolean keepOriginal = false;
	
	/**
	 * Number of days to shift the index date. Negative number indicates going back in time. E.g. -7 will shift the dates one week to the past.<BR>
	 * default = -7
	 */
	public int shift = -7;
	
	/**
	 * Added at the end of the eventtype field to mark shifted events.<BR>
	 * default = ""
	 */
	public String shiftPostfix = "";
	
	private static final long serialVersionUID = 6086331385395712581L;

	protected void runModule(String outputFilename){
		process(events.getResultFilename(), outputFilename);
	}

	public void process(String source, String target) {
		ProgressHandler.reportProgress();
		EventFileWriter out = new EventFileWriter(target);
		int count = 0;
		for (Event event : new EventFileReader(source)){
			if (keepOriginal)
				out.write(event);
			event.date += shift;
			event.eventType += shiftPostfix;
			out.write(event);
			count++;
		}
		out.close();
		if (keepOriginal)
		  FileSorter.sort(target, new String[]{"PatientID", "Date"});
		System.out.println("Shifted dates of " + count + " events");
	}
}