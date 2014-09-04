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
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.EventFileWriter;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.StringUtilities;

/**
 * Creates a random sample of events from the event file, typically used for manual validation.
 * @author schuemie
 *
 */
public class RandomEventSampler extends JerboaModule {

	public JerboaModule events;
	
	public JerboaModule patients;

	/**
	 * Maximum number of events that will be sampled per event type. You can either specify the number
	 * per event type, (e.g. 'UGIB;100'), or one number of all event types using ALL(e.g. 'ALL;100). 
	 */
	public List<String> sampleSizePerEventType = new ArrayList<String>();

	/**
	 * Start date of the study. If set, only events are sampled that are at or after this date. Format:
	 * YYYYMMDD.<BR>
	 * default = not set (dates are not limited)
	 */
	public String studyStart = null;

	/**
	 * End date of the study. If set, only events are sampled that are at or before this date. Format:
	 * YYYYMMDD.<BR>
	 * default = not set (dates are not limited)
	 */
	public String studyEnd = null;
	
	/**
	 * Only sample from events that occur within the cohort time.<BR>
	 * default = true
	 */
	public boolean insideCohortTimeOnly = true;
	
	/**
	 * Only sample from the first event of a type for a patient.<BR>
	 * default = true
	 */
	public boolean firstEventOnly = true;
	

	private static final long serialVersionUID = 3258681490725368766L;
	private static String ALL = "ALL";
	private Map<String,Integer> eventType2SampleSize;
	private Random random = new Random();

	public static void main(String[] args){
		String folder = "x:/Study5/";
		FileSorter.sort(folder+"Events.txt", new String[]{"EventType"});
		RandomEventSampler module = new RandomEventSampler();
		module.sampleSizePerEventType.add("ALL;10");
		module.studyStart = "20000101";
		module.studyEnd = "20021231";
		module.process(folder+"Events.txt",null,folder+"RandomEventsSample.csv");
	}

	protected void runModule(String outputFilename){
		FileSorter.sort(events.getResultFilename(), new String[]{"PatientID", "Date"});
		if (patients != null)
			FileSorter.sort(patients.getResultFilename(), new String[]{"PatientID"});
		process(events.getResultFilename(),(patients == null)?null:patients.getResultFilename(),outputFilename);
	}

	private void process(String sourceEvents,String sourcePatients, String target) {
		if (insideCohortTimeOnly && sourcePatients == null)
			throw new RuntimeException("Event sampling requires patient cohort times, but no patient file specified");
		
		parseSampleSizes();
		
		OneToManyList<String, Event> eventType2Events = loadAllEligibleEvents(sourceEvents, sourcePatients);

		sampleAndWriteToFile(eventType2Events, target);
	}

	private void sampleAndWriteToFile(OneToManyList<String, Event> eventType2Events, String target) {
		EventFileWriter out = new EventFileWriter(target);
		for (String eventType : eventType2Events.keySet()){
			List<Event> events = eventType2Events.get(eventType);
			int maxSampleSize = getMaxSampleSize(eventType);
			if (maxSampleSize != 0){
				int sampleSize = Math.min(maxSampleSize, events.size());
				for (int i = 0; i < sampleSize; i++)
					out.write(events.remove(random.nextInt(events.size())));
				System.out.println("- sampled " + sampleSize + " events of type " + eventType);
			}
		}
		out.close();
	}

	private OneToManyList<String, Event> loadAllEligibleEvents(String sourceEvents, String sourcePatients) {
		long studyStartDate = parseDate(studyStart);
		long studyEndDate = parseDate(studyEnd)+1;

		Iterator<Event> eventIterator = new EventFileReader(sourceEvents).iterator();
		Iterator<Patient> patientIterator = null;
		Patient patient = null;
		if (insideCohortTimeOnly){
			patientIterator = new PatientFileReader(sourcePatients).iterator();
		  if (patientIterator.hasNext())
		  	patient = patientIterator.next();
		}
		OneToManyList<String, Event> eventType2Events = new OneToManyList<String, Event>();		
		Set<String> previousEventTypes = new HashSet<String>();
		String oldPatientID = "";
		while (eventIterator.hasNext()){
			ProgressHandler.reportProgress();
			Event event = eventIterator.next();

      //Reset previous event types if new patient: 
      if (!event.patientID.equals(oldPatientID)){
      	oldPatientID = event.patientID;
      	previousEventTypes.clear();
      }
			
      //Skip to patient belonging to this event:
      while (patient != null && patient.patientID.compareTo(event.patientID)<0)
      	if  (patientIterator.hasNext())
      		patient = patientIterator.next();  
      	else
      		patient = null;     
			
      if (patient != null && !patient.patientID.equals(event.patientID))
      	continue; //Patient not in patients file: skip this event
      
      //Calculate minimum start date:
      long startDate = 0;
      if (studyStartDate != -1)
      	startDate = studyStartDate;
      if (patient != null && patient.startdate > startDate)
      	startDate = patient.startdate;
      
      //Calculate maximum end date:
      long endDate = Long.MAX_VALUE;
      if (studyEndDate != -1)
      	endDate = studyEndDate;
      if (patient != null && patient.enddate < endDate)
      	endDate = patient.enddate;

      if (!firstEventOnly || previousEventTypes.add(event.eventType)) //Check if its the first event (or not limiting to first events)
        if (event.date >= startDate && event.date < endDate) //Check if event within allowed dates
      		eventType2Events.put(event.eventType, event); 
		}
		return eventType2Events;
	}

	private int getMaxSampleSize(String eventType) {
		Integer sampleSize = eventType2SampleSize.get(eventType);
		if (sampleSize == null)
			sampleSize = eventType2SampleSize.get(ALL);
		if (sampleSize == null)
			return 0;
		return sampleSize;
	}

	private void parseSampleSizes() {
		eventType2SampleSize = new HashMap<String, Integer>();
		for (String sampleSize : sampleSizePerEventType){
			String[] parts = sampleSize.split(";");
			eventType2SampleSize.put(parts[0], Integer.parseInt(parts[1]));
		}
	}

	private long parseDate(String date) {
		if (date == null)
			return -1;
		try {
			return StringUtilities.sortableTimeStringToDays(date);
		} catch (DataFormatException e) {
			throw new RuntimeException("Illegal date in settings for "+this.getClass().getName()+": " + date);
		}
	}
}