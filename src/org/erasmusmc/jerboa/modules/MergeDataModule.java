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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.erasmusmc.collections.OneToManyList;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.ConstantValues;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.MergedDataFileWriter;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEventIterator;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.userInterface.VirtualTable;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.SequenceProcessing;
import org.erasmusmc.utilities.StringUtilities;

/**
 * Merges patient, event, and optionally prescription data into one file. Optionally, the patient time is 
 * split by calendar year and/or month. Optionally, patient data after an event is censored. Patient time 
 * outside the patient cohort time is marked separately. Time in the output table is EXCLUSIVE: each patient day is counted only once.
 * @author schuemie
 *
 */
public class MergeDataModule extends JerboaModule implements VirtualTable<MergedData> {

	public JerboaModule prescriptions;
	public JerboaModule patients;
	public JerboaModule events;

	/**
	 * The list of age ranges used when coding patient age. Each range should be represented as 
	 * three semicolon separated values:
	 * <OL>
	 * <LI>The start year since birth</LI>
	 * <LI>The end year since birth (exclusive)</LI>
	 * <LI>The label of age range</LI>
	 * </OL>
	 * For example: 0;5;0-4
	 */
	public List<String> ageCode = new ArrayList<String>();

	/**
	 * Specifies whether patient time after an event should be censored.<BR>
	 * default = true
	 */
	public boolean removeAfterEvent = true;

	/**
	 * Specifies whether patient time should be split by calendar year. If set to true, no episode will span
	 * two years, and the calendar year is output as a separate variable.<BR>
	 * default = false
	 */
	public boolean splitByYear = false;

	/**
	 * Specifies whether patient time should be split by calendar month. If set to true, no episode will span
	 * two months, and the month is output as a separate variable.<BR>
	 * default = false
	 */
	public boolean splitByMonth = false;
	
	/**
	 * Specifies whether patient time should be split by calendar week. If set to true, no episode will span
	 * two weeks, and the week is output as a separate variable. Week 1 starts on 1st of January, and ends on January 7.<BR>
	 * default = false
	 */
	public boolean splitByWeek = false;

	/**
	 * Specifies the level of noise to be added to the month boundary. The month offset is sampled from a normal distribution with a mean of 0, 
	 * and the standard deviation specified here.<BR>
	 * default = 0
	 */
	public double calenderTimeStandardDeviation = 0d;

	/**
	 * Specifies the level of noise to be added to the patient boundary. The time offset is sampled from a normal distribution with a mean of 0, 
	 * and the standard deviation specified here.<BR>
	 * default = 0
	 */
	public double patientTimeStandardDeviation = 0d;

	/**
	 * The number of days the patient time should be censored after an event, if censoring is enabled.<BR>
	 * default = 999999
	 */
	public int censorPeriod = 999999;

	private static final long serialVersionUID = -4422411384238432005L;
	private List<Code> codes = new ArrayList<Code>();
	private int newPrescriptionCount;
	private int oldEventCount;
	private int newEventCount;
	private int patientCount;
	private int oldPrescriptionCount;
	private Random random = new Random();
	
	public static void main(String[] args){

		String folder = "C:/home/data/Simulated/AgeRange/";
		MergeDataModule module = new MergeDataModule();
		module.removeAfterEvent = true;
		module.censorPeriod = 999999;
		module.splitByYear = true;
		module.splitByMonth = false;
		
		module.ageCode.add("0;5;00-04");
		module.ageCode.add("5;10;05-09");
		module.ageCode.add("10;15;10-14");
		module.ageCode.add("15;20;15-19");
		module.ageCode.add("20;25;20-24");
		module.ageCode.add("25;30;25-29");
		module.ageCode.add("30;35;30-34");
		module.ageCode.add("35;40;35-39");
		module.ageCode.add("40;45;40-44");
		module.ageCode.add("45;50;45-49");
		module.ageCode.add("50;55;50-54");
		module.ageCode.add("55;60;55-59");
		module.ageCode.add("60;65;60-64");
		module.ageCode.add("65;70;65-69");
		module.ageCode.add("70;75;70-74");
		module.ageCode.add("75;80;75-79");
		module.ageCode.add("80;85;80-84");
		module.ageCode.add("85;999;85-");
		
		module.process(folder+"Prescriptions.txt", folder+"Cohortentrydate.txt", folder+"Events.txt", folder+"MergeData.txt");
	}

	protected void runModule(String outputFilename){
		if (prescriptions != null)
		  FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID","Date"});
		FileSorter.sort(patients.getResultFilename(), "PatientID");
		if (events != null)
		  FileSorter.sort(events.getResultFilename(), "PatientID");
		if (!isVirtual())
		  process((prescriptions == null)?null:prescriptions.getResultFilename(), patients.getResultFilename(), (events == null)?null:events.getResultFilename(), outputFilename);
	}

	private List<Code> parseCodes(List<String> exposureCode) {
		List<Code> codes = new ArrayList<Code>();
		for (String row : exposureCode){
			String[] cols = row.split(";");
			int startDay = Integer.parseInt(cols[0]);
			int endDay = Integer.parseInt(cols[1]);
			String label  = cols[2];
			Code code = new Code(startDay, endDay, label);
			codes.add(code);
		}
		return codes;
	}

	public void process(String sourcePrescriptions, String sourcePatients, String sourceEvents, String target) {
		MergedDataFileWriter out = new MergedDataFileWriter(target);
		Iterator<MergedData> iterator = getIterator(sourcePrescriptions, sourcePatients, sourceEvents);
		while (iterator.hasNext()){
			out.write(iterator.next());
			ProgressHandler.reportProgress();
		}
		out.close();
		System.out.println("Processed " + patientCount + " patients");
		System.out.println("Original size: " + oldPrescriptionCount + " prescriptions, new size: " + newPrescriptionCount + " prescriptions");
		System.out.println("Original number of events: " + oldEventCount +", new number of events: " + newEventCount);
	}

	private List<MergedData> processPatient(PatientPrescriptionEvent data){
		List<MergedData> result = new ArrayList<MergedData>();
		patientCount++;
		oldPrescriptionCount += data.prescriptions.size();
		oldEventCount += data.events.size();
		long calenderTimeOffset = samleOffset(calenderTimeStandardDeviation);
		long personTimeOffset = samleOffset(patientTimeStandardDeviation);
		
		removeDuplicatesAndSort(data.events);
		addNonExposure(data);
		for (Prescription prescription : data.prescriptions){
			MergedData mergedData = new MergedData(prescription);
			mergedData.gender = data.patient.gender;
			for (MergedData choppedByCohortTime : chopDataByCohortTime(mergedData, data.patient, personTimeOffset)){
				long ageAtStart;
				if (data.patient.birthdate == ConstantValues.UNKNOWN_DATE)
					ageAtStart = -1;
				else
					ageAtStart = choppedByCohortTime.start - data.patient.birthdate;
				for (MergedData choppedByAge : chopDataByAge(choppedByCohortTime, ageAtStart))
					for (MergedData choppedByYear : chopDataByYear(choppedByAge, calenderTimeOffset))
						for (MergedData choppedByMonth : chopDataByMonth(choppedByYear, calenderTimeOffset))
							for (MergedData choppedByWeek : chopDataByWeek(choppedByMonth, calenderTimeOffset))
								for (MergedData choppedByEvent : chopByEvent(choppedByWeek, data.events)){        				
									result.add(choppedByEvent);
									newPrescriptionCount++;
									newEventCount += choppedByEvent.events.size();
								}
			}
		} 
		return result;
	}

	private long samleOffset(double standardDeviation) {
		if (standardDeviation == 0)
		  return 0;
		else
			return Math.round(random.nextGaussian()*standardDeviation);
	}

	private void addNonExposure(PatientPrescriptionEvent data) {
		long start = data.patient.startdate;
		long end = data.patient.enddate;
		List<Prescription> nonPrescriptions = new ArrayList<Prescription>();
		for (Prescription prescription : data.prescriptions){
			if (prescription.start < end){
				if (prescription.start > start){
					Prescription nonPrescription = new Prescription();
					nonPrescription.patientID = prescription.patientID;
					nonPrescription.start = start;
					long nonPrescriptionEnd = prescription.start;
					nonPrescription.duration = nonPrescriptionEnd - nonPrescription.start;
					nonPrescriptions.add(nonPrescription);
				}
				if (prescription.getEnd() > start)
				  start = prescription.getEnd();
			}
		}
		//Write remaining patient time as non-prescription
		if (start < end){
			Prescription nonPrescription = new Prescription();
			nonPrescription.patientID = data.patient.patientID;
			nonPrescription.start = start;
			nonPrescription.duration = end - start;
			nonPrescriptions.add(nonPrescription);
		}
		data.prescriptions.addAll(nonPrescriptions);
		Collections.sort(data.prescriptions, new Comparator<Prescription>(){

			@Override
			public int compare(Prescription o1, Prescription o2) {
				if (o1.start == o2.start)
					return 0;
				else if (o1.start < o2.start)
					return -1;
				else 
					return 1;
			}});
	}

	private List<MergedData> chopDataByCohortTime(MergedData md, Patient patient, long personTimeOffset) {
		List<MergedData> newMDs = new ArrayList<MergedData>(1);
	  long adjustedStartdate = patient.startdate + personTimeOffset;
	  long adjustedEndData = patient.enddate + personTimeOffset;
	  
		if (md.start >= adjustedStartdate && md.getEnd() <= adjustedEndData)
			newMDs.add(md);
		else if (md.getEnd() <= adjustedStartdate || md.start >= adjustedEndData){
			md.outsideCohortTime = true;
			newMDs.add(md);
		} else {
			if (md.start < adjustedStartdate && md.getEnd() > adjustedStartdate){

				MergedData newMD = new MergedData(md);
				newMD.duration = adjustedStartdate - newMD.start;
				newMD.outsideCohortTime = true;
				newMDs.add(newMD);

				md.duration = md.getEnd() - adjustedStartdate;
				md.start = adjustedStartdate;
			}  
			if (md.start < adjustedEndData && md.getEnd() > adjustedEndData){
				MergedData newMD = new MergedData(md);
				newMD.duration = adjustedEndData - newMD.start;

				newMDs.add(newMD);
				md.duration = md.getEnd() - adjustedEndData;
				md.start = adjustedEndData;
				md.outsideCohortTime = true;
			}
			if (md.duration != 0)
				newMDs.add(md);
		}
		return newMDs;
	}
	
	private List<MergedData> chopByEvent(MergedData md,	List<Event> events) {
		List<MergedData> newMDs = new ArrayList<MergedData>(1);	
		if (events != null){
			if (!removeAfterEvent){ // Just add all events that fall within md:
				for (Event event : events)
					if (event.date >= md.start && event.date < md.getEnd()) 
						md.events.add(event);
				newMDs.add(md);
			} else { // Only add events if past censor date, and split by event and censor date:
				//First, build lists of all events and end of censor dates within period:
				Map<String,Long> precedingEventTypes2dates = new HashMap<String,Long>(0);
				OneToManyList<Long, Event> date2Events = new OneToManyList<Long, Event>();
				OneToManyList<Long, String> date2EndOfCensorPeriod = new OneToManyList<Long, String>();
				for (Event event : events) {
					if (event.date >= md.getEnd())
						break;
					Long censorDate = precedingEventTypes2dates.get(event.eventType);

					if (censorDate == null || event.date > censorDate) {
						if (event.date >= md.start)
							date2Events.put(event.date, event);
						Long newCensorDate = event.date + censorPeriod;
						if (newCensorDate >= md.start && newCensorDate < md.getEnd())
							date2EndOfCensorPeriod.put(newCensorDate, event.eventType);
						precedingEventTypes2dates.put(event.eventType, newCensorDate);
						if (event.date < md.start && newCensorDate >= md.start)
							md.precedingEventTypes.add(event.eventType);
					}
				}
				//Then, go through all relevant dates and chop the merged data accordingly:
				Set<Long> allDates = new HashSet<Long>();
				allDates.addAll(date2Events.keySet());
				allDates.addAll(date2EndOfCensorPeriod.keySet());
				List<Long> sortedDates = new ArrayList<Long>(allDates);
				Collections.sort(sortedDates);
				for (Long date : sortedDates){
					MergedData partMD = split(md, date+1);
					partMD.events.addAll(date2Events.get(date));
					newMDs.add(partMD);
					md.precedingEventTypes.removeAll(date2EndOfCensorPeriod.get(date));
					for (Event event: partMD.events)
						md.precedingEventTypes.add(event.eventType);
				}
				if (md.duration > 0)
					newMDs.add(md);
			}
		}
		return newMDs;
	}

	private MergedData split(MergedData md, Long date) {
		long end = md.getEnd();
		MergedData newMD = new MergedData(md);
		newMD.duration = date - md.start;  
		md.start = date;
		md.duration = end - date;
		return newMD;
	}

	private void removeDuplicatesAndSort(List<Event> events) {   
		Collections.sort(events);
		Event previousEvent = null;
		Iterator<Event> iterator = events.iterator();
		while (iterator.hasNext()){
			Event event = iterator.next();
			if (previousEvent != null && event.eventType.equals(previousEvent.eventType) && event.date == previousEvent.date)
				iterator.remove();
			else
				previousEvent = event;
		}
	}

	private List<MergedData> chopDataByAge(MergedData mergedData, long ageAtStart) {
		List<MergedData> result = new ArrayList<MergedData>();
		if (ageAtStart == -1){
			MergedData codedData = new MergedData(mergedData);
			codedData.ageRange = "";
			result.add(codedData);        
		} else
			for (Code code : codes){
				long codeStart = code.start + mergedData.start - ageAtStart;
				long codeEnd = code.end + mergedData.start - ageAtStart;
				if (codeEnd > mergedData.start && codeStart < mergedData.getEnd()){
					MergedData codedData = new MergedData(mergedData);
					codedData.start = Math.max(codeStart, mergedData.start);
					codedData.duration = Math.min(codeEnd, mergedData.getEnd()) - codedData.start;
					codedData.ageRange = code.code;
					result.add(codedData);        
				}
			}
		return result;
	}

	public List<MergedData> chopDataByYear(MergedData mergedData, long calenderTimeOffset) {
		List<MergedData> result = new ArrayList<MergedData>();
		if (!splitByYear)
			result.add(mergedData);
		else {
			try {
				long startOfNextYear;
				long adjustedEndTime;
				do {
					long adjustedStartTime = mergedData.start - calenderTimeOffset;
					adjustedEndTime = mergedData.getEnd() - calenderTimeOffset;
					String startYear = StringUtilities.daysToSortableDateString(adjustedStartTime).substring(0,4);
					startOfNextYear = StringUtilities.sortableTimeStringToDays(startYear + "1231") + 1;
					if (startOfNextYear < adjustedEndTime){
						long adjustedStartOfNextYear = startOfNextYear + calenderTimeOffset;
						MergedData codedData = new MergedData(mergedData);
						codedData.duration = adjustedStartOfNextYear - codedData.start;
						codedData.year = Integer.parseInt(startYear);
						result.add(codedData);     
						mergedData.duration -= adjustedStartOfNextYear - mergedData.start;
						mergedData.start = adjustedStartOfNextYear;
					} else {
						mergedData.year = Integer.parseInt(startYear);
						result.add(mergedData);
					}
				} while (startOfNextYear < adjustedEndTime);
			} catch (DataFormatException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public List<MergedData> chopDataByMonth(MergedData mergedData, long calenderTimeOffset) {
		List<MergedData> result = new ArrayList<MergedData>();
		if (!splitByMonth)
			result.add(mergedData);
		else {
			try {
				long startOfNextMonth;
				long adjustedEndTime;
				do {
					long adjustedStartTime = mergedData.start - calenderTimeOffset;
					adjustedEndTime = mergedData.getEnd() - calenderTimeOffset;
					String startYearMonth = StringUtilities.daysToSortableDateString(adjustedStartTime).substring(0,6);
					int startYear = Integer.parseInt(startYearMonth.substring(0,4));
					int startMonth = Integer.parseInt(startYearMonth.substring(4));
					int nextYear = startYear;
					int nextMonth = startMonth + 1;
					if (nextMonth == 13){
						nextMonth = 1;
						nextYear++;
					}

					startOfNextMonth = StringUtilities.sortableTimeStringToDays(nextYear + StringUtilities.formatNumber("00", nextMonth) + "01");
					if (startOfNextMonth < adjustedEndTime){
						long adjustedStartOfNextMonth = startOfNextMonth + calenderTimeOffset;
						MergedData codedData = new MergedData(mergedData);
						codedData.duration = adjustedStartOfNextMonth - codedData.start;
						codedData.month = startMonth;
						result.add(codedData);     
						mergedData.duration -= adjustedStartOfNextMonth - mergedData.start;
						mergedData.start = adjustedStartOfNextMonth;
					} else {
						mergedData.month = startMonth;
						result.add(mergedData);
					}
				} while (startOfNextMonth < adjustedEndTime);
			} catch (DataFormatException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private List<MergedData> chopDataByWeek(MergedData mergedData,long calenderTimeOffset) {
		List<MergedData> result = new ArrayList<MergedData>();
		if (!splitByWeek)
			result.add(mergedData);
		else {
			try {
				long startOfNextWeek;
				long adjustedEndTime = mergedData.getEnd() - calenderTimeOffset;
				do {
					long adjustedStartTime = mergedData.start - calenderTimeOffset;
					String startYearString = StringUtilities.daysToSortableDateString(adjustedStartTime).substring(0,4);
					long startOfStartYear = StringUtilities.sortableTimeStringToDays(startYearString+"0101");
					long endOfStartYear =  StringUtilities.sortableTimeStringToDays(startYearString+"1231");
					int startWeek = (int)(1+(adjustedStartTime - startOfStartYear)/7);

					if (startOfStartYear + startWeek * 7 > endOfStartYear)
					  startOfNextWeek = endOfStartYear + 1;
					else
						startOfNextWeek = startOfStartYear + startWeek * 7;
					
					if (startOfNextWeek < adjustedEndTime){
						long readjustedStartOfNextWeek = startOfNextWeek + calenderTimeOffset;
						MergedData codedData = new MergedData(mergedData);
						codedData.duration = readjustedStartOfNextWeek - codedData.start;
						codedData.week = startWeek;
						result.add(codedData);     
						mergedData.duration -= readjustedStartOfNextWeek - mergedData.start;
						mergedData.start = readjustedStartOfNextWeek;
					} else {
						mergedData.week = startWeek;
						result.add(mergedData);
					}
				} while (startOfNextWeek < adjustedEndTime);
			} catch (DataFormatException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private static class Code
	{
		long start;
		long end;
		String code;
	
		public Code(int startday, int endday, String code)
		{
			start = Math.round(startday * 365.25);
			end = Math.round(endday * 365.25);
			this.code = code;
		}
	}  
	
	public Iterator<MergedData> getIterator() 
	{
		return getIterator((prescriptions == null)?null:prescriptions.getResultFilename(), patients.getResultFilename(), (events == null)?null:events.getResultFilename());
	}
	
	public Iterator<MergedData> getIterator(String sourcePrescriptions, String sourcePatients, String sourceEvents) 
	{
		codes = parseCodes(ageCode);
		newPrescriptionCount = 0;
		oldEventCount = 0;
		newEventCount = 0;
		patientCount = 0;
		oldPrescriptionCount = 0;
		PatientPrescriptionEventIterator iterator = new PatientPrescriptionEventIterator(sourcePrescriptions, sourcePatients, sourceEvents);
		return new MergedDataIterator(iterator);
	}

	private class MergedDataIterator extends SequenceProcessing<PatientPrescriptionEvent, MergedData>
	{				
		public MergedDataIterator(Iterator<PatientPrescriptionEvent> inputIterator)
		{
			super(inputIterator); 
		}

		@Override
		public List<MergedData> processInput(PatientPrescriptionEvent input)
		{
			return  processPatient(input);
		}
	}
}
