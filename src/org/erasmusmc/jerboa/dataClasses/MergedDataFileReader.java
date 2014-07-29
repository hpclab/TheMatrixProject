package org.erasmusmc.jerboa.dataClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

import org.erasmusmc.utilities.StringUtilities;

public class MergedDataFileReader implements Iterable<MergedData> {
	private String filename;

	public MergedDataFileReader(String filename) {
		this.filename = filename;
	}

	public Iterator<MergedData> iterator() {
		return new MergedDataIterator(filename);
	}

	private class MergedDataIterator extends InputFileIterator<MergedData> {
		private int date;
		private int year;
		private int month;
		private int week; 
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
			date = findIndex("Date", row);
			year = findIndexOptional("Year", row);
			month = findIndexOptional("Month", row);
			week = findIndexOptional("Week", row);
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
			mergedData.start = StringUtilities.sortableTimeStringToDays(columns.get(date));
			if (year != -1)
				mergedData.year = Integer.parseInt(columns.get(year));
			if (month != -1)
				mergedData.month = Integer.parseInt(columns.get(month));
			if (week != -1)
				mergedData.week = Integer.parseInt(columns.get(week));
			mergedData.patientID = columns.get(patientID);
			mergedData.duration = Math.round(Float.parseFloat(columns.get(duration)));
			mergedData.setATCCodes(columns.get(atc));
			if (precedingEventTypes != -1 && columns.get(precedingEventTypes).length() != 0)
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