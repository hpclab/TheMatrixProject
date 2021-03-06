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
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

import org.erasmusmc.jerboa.dataClasses.ConstantValues;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.InputFileIterator;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.Patient;

import org.erasmusmc.utilities.StringUtilities;

public class SCCSDataFileReader  implements Iterable<MergedData> {
	private String filename;

	public SCCSDataFileReader(String filename) {
		this.filename = filename;
	}

	public Iterator<MergedData> iterator() {
		return new MergedDataIterator(filename);
	}

	private class MergedDataIterator extends InputFileIterator<MergedData> {
		private int year;
		private int month; 
		private int patientID;
		private int duration;
		private int atc;
		private int events;
		private int ageRange;
		private int gender;
		private int precedingEventTypes;
		private int outsideCohortTime;

		public MergedDataIterator(String filename) {
			super(filename);
		}

		public void processHeader(List<String> row){
			year = findIndexOptional("Year", row);
			month = findIndexOptional("Month", row);
			patientID = findIndex("PatientID", row);
			duration = findIndex("Duration", row);
			atc = findIndex("ATC", row);
			events = findIndex("Events", row);
			ageRange = findIndex("AgeRange", row);
			gender = findIndex("Gender", row);
			precedingEventTypes = findIndexOptional("PrecedingEventTypes", row);
			outsideCohortTime = findIndexOptional("OutsideCohortTime", row);
		}

		public MergedData row2object(List<String> columns) throws Exception{
			MergedData mergedData = new MergedData();
			if (year != -1)
				mergedData.year = Integer.parseInt(columns.get(year));
			if (month != -1)
				mergedData.month = Integer.parseInt(columns.get(month));
			mergedData.patientID = columns.get(patientID);
			mergedData.duration = Math.round(Float.parseFloat(columns.get(duration)));
			mergedData.setATCCodes(columns.get(atc));
			if (precedingEventTypes != -1)
			  mergedData.precedingEventTypes.addAll(StringUtilities.safeSplit(columns.get(precedingEventTypes), '+'));
			if (mergedData.atcCodes.size() == 1 && mergedData.atcCodes.iterator().next().atc.equals(""))
				mergedData.atcCodes.clear();
			mergedData.events = parseEvents(columns.get(events), mergedData.patientID);
			mergedData.ageRange = columns.get(ageRange);
			String genderString = columns.get(gender);
			if (genderString.toLowerCase().charAt(0) == 'm')
				mergedData.gender = Patient.MALE;
			else
				mergedData.gender = Patient.FEMALE;
			if (outsideCohortTime != -1)
			  mergedData.outsideCohortTime = (columns.get(outsideCohortTime).equals("1"));
			return mergedData;
		}

		private List<Event> parseEvents(String string, String patientID) throws DataFormatException {
			if (string.length() == 0)
				return Collections.emptyList();
			String[] events = string.split("\\+");
			List<Event> result = new ArrayList<Event>(events.length);
			for (String eventString : events){
				String[] parts = eventString.split(":");
				Event event = new Event();
				event.patientID = patientID;
				event.eventType = parts[0];
				if (parts.length > 1)
				  event.date = StringUtilities.sortableTimeStringToDays(parts[1]);
				else
					event.date = ConstantValues.UNKNOWN_DATE;
				result.add(event);
			}
			return result;
		}
	}
}