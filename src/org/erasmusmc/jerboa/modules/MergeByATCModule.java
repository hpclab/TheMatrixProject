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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.jerboa.userInterface.VirtualTable;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.SequenceProcessing;
import org.erasmusmc.utilities.StringUtilities;

/**
 * Similar to the MergeDataModule, but also splits up by ATC code, so each row will refer to at most 1 ATC code. The meaning of the ATC column therefore has 
 * a much different meaning: No ATC indicates all patient time, irrespective of whether the patient was using drug. An ATC code indicates the time a patient was
 * on that drug. Time in the output table is NON-EXCLUSIVE: a patient day could be counted several times. 
 * @author schuemie
 *
 */
public class MergeByATCModule extends JerboaModule implements VirtualTable<MergedData> {


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
	 * The number of days the patient time should be censored after an event, if censoring is enabled.<BR>
	 * default = 999999
	 */
	public int censorPeriod = 999999;

	/**
	 * Specifies whether patient time should be split by calendar year. If set to true, no episode will span
	 * two years, and the calendar year is output as a separate variable.<BR>
	 * default = false
	 */
	public boolean splitByYear = false;

	private static final long serialVersionUID = 1561967305951500543L;
	private List<Code> codes;

	public static void main(String[] args){
		String folder = "x:/EUADR Study5/";
		MergeByATCModule module = new MergeByATCModule();
		module.removeAfterEvent = true;
		module.censorPeriod = 999999;
		module.splitByYear = true;
		module.ageCode.add("0;5;0-4");
		module.ageCode.add("5;10;5-9");
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
		module.process(folder+"Mergerepeats.txt", folder+"Cohortentrydate.txt",folder+"Events.txt",folder+"MergebyATC.txt");
	}

	@Override
	protected void runModule(String outputFilename){
		FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID","Date"});
		FileSorter.sort(patients.getResultFilename(), "PatientID");
		FileSorter.sort(events.getResultFilename(), "PatientID");
		if (!isVirtual())
		  process(prescriptions.getResultFilename(), patients.getResultFilename(), events.getResultFilename(), outputFilename);
	}

	public void process(String sourcePrescriptions, String sourcePatients, String sourceEvents, String target) {
		MergedDataFileWriter out = new MergedDataFileWriter(target);
		Iterator<MergedData> iterator = getIterator(sourcePrescriptions, sourcePatients, sourceEvents);
		while (iterator.hasNext()){
			out.write(iterator.next());
			ProgressHandler.reportProgress();
		}
		out.close();
	}

	private List<MergedData> processPatient(PatientPrescriptionEvent patientPrescriptionEvent) {
		List<MergedData> result = new ArrayList<MergedData>();
		addFullPatientTime(patientPrescriptionEvent.prescriptions, patientPrescriptionEvent.patient);
		removeDuplicatesAndSort(patientPrescriptionEvent.events);
		for (Prescription prescription : patientPrescriptionEvent.prescriptions){
			MergedData mergedData = new MergedData(prescription);
			mergedData.gender = patientPrescriptionEvent.patient.gender;
			for (MergedData choppedByCohortTime : chopDataByCohortTime(mergedData, patientPrescriptionEvent.patient)){
      	long ageAtStart;
      	if (patientPrescriptionEvent.patient.birthdate == ConstantValues.UNKNOWN_DATE)
      		ageAtStart = -1;
      	else
      	  ageAtStart = choppedByCohortTime.start - patientPrescriptionEvent.patient.birthdate;
				for (MergedData choppedByAge : chopDataByAge(choppedByCohortTime, ageAtStart))
					for (MergedData choppedByYear : chopDataByYear(choppedByAge))
					for (MergedData choppedByEvent : chopByEvent(choppedByYear, patientPrescriptionEvent.events)){        				
						result.add(choppedByEvent);
					}
			}
		}
		return result;
	}

	public List<MergedData> chopDataByYear(MergedData mergedData) {
		List<MergedData> result = new ArrayList<MergedData>();
		if (!splitByYear)
			result.add(mergedData);
		else {
			try {
				long startOfNextYear;
				long adjustedEndTime;
				do {
					long adjustedStartTime = mergedData.start;
					adjustedEndTime = mergedData.getEnd();
					String startYear = StringUtilities.daysToSortableDateString(adjustedStartTime).substring(0,4);
					startOfNextYear = StringUtilities.sortableTimeStringToDays(startYear + "1231") + 1;
					if (startOfNextYear < adjustedEndTime){
						long adjustedStartOfNextYear = startOfNextYear;
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
	
	private List<MergedData> chopDataByCohortTime(MergedData mergedData, Patient patient) {
		if (mergedData.start >= patient.startdate && mergedData.getEnd() <= patient.enddate){
			List<MergedData> result = new ArrayList<MergedData>(1);
			result.add(mergedData);
			return result;
		} else 	if (mergedData.getEnd() <= patient.startdate || mergedData.start >= patient.enddate){
			return Collections.emptyList();
		} else {
			long endDate = Math.min(patient.enddate, mergedData.getEnd());
			long startDate = Math.max(patient.startdate, mergedData.start);
			mergedData.start = startDate;
			mergedData.duration = endDate - startDate;
			List<MergedData> result = new ArrayList<MergedData>(1);
			result.add(mergedData);
			return result;
		}
	}

	private void addFullPatientTime(List<Prescription> prescriptions,	Patient patient) {
		Prescription fullPatientTime = new Prescription();
		fullPatientTime.patientID = patient.patientID;
		fullPatientTime.start = patient.startdate;
		fullPatientTime.duration = patient.enddate - fullPatientTime.start;
		fullPatientTime.atcCodes.add(new ATCCode(""));
		prescriptions.add(fullPatientTime);
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

	private MergedData split(MergedData md, Long date) {
		long end = md.getEnd();
		MergedData newMD = new MergedData(md);
		newMD.duration = date - md.start;  
		md.start = date;
		md.duration = end - date;
		return newMD;
	}

	private static class Code{
		long start;
		long end;
		String code;
		public Code(int startday, int endday, String code){
			start = Math.round(startday * 365.25);
			end = Math.round(endday * 365.25);
			this.code = code;
		}
	}  

	public Iterator<MergedData> getIterator() {
		return getIterator(prescriptions.getResultFilename(), patients.getResultFilename(), events.getResultFilename());
	}
	
	public Iterator<MergedData> getIterator(String sourcePrescriptions, String sourcePatients, String sourceEvents) {
		codes = parseCodes(ageCode);
		PatientPrescriptionEventIterator iterator = new PatientPrescriptionEventIterator(sourcePrescriptions, sourcePatients, sourceEvents);
		return new MergedDataIterator(iterator);
	}

	private class MergedDataIterator extends SequenceProcessing<PatientPrescriptionEvent, MergedData>{				
		public MergedDataIterator(Iterator<PatientPrescriptionEvent> inputIterator){
			super(inputIterator); 
		}

		@Override
		public List<MergedData> processInput(PatientPrescriptionEvent input) {
			return processPatient(input);
		}
	}
}