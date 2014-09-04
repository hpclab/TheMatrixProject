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
import java.util.Set;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Counts the codes associated with event occurrences. Assumes that the codes are mentioned in the
 * events file in a column labeled 'Code'.
 * @author schuemie
 *
 */
public class EventCodeCountingModule extends JerboaModule 
{
	public JerboaModule events;
	public JerboaModule patients;
	
	private Map<String, CountingSet<String>> type2codeCount;
	private Map<String, CountingSet<String>> type2codeCountFirstOnly;
	
	private static final long serialVersionUID = 5937240605892905093L;
	
	public static void main(String[] args)
	{
		String folder = "/home/data/Simulated/";
		
		FileSorter.sort(folder + "Events.txt", new String[] { "PatientID", "Date"} );
		FileSorter.sort(folder + "Patients.txt", new String[] { "PatientID"} );
		
		EventCodeCountingModule module = new EventCodeCountingModule();
		module.process(folder + "Events.txt", folder + "Patients.txt", folder + "Counts.txt");
	}
	
	protected void runModule(String outputFilename)
	{
		FileSorter.sort(events.getResultFilename(), new String[] { "PatientID", "Date" } );
		FileSorter.sort(patients.getResultFilename(), new String[] { "PatientID" } );
	
		process(events.getResultFilename(), patients.getResultFilename(), outputFilename);
	}

	private void process(String sourceEvents, String sourcePatients, String target) 
	{
		//Initialize iterator:
		Iterator<Patient> patientIterator = new PatientFileReader(sourcePatients).iterator();
		Iterator<Event> eventIterator = new EventFileReader(sourceEvents).iterator();
		Iterator<PatientEvent> iterator = new PatientEventIterator(patientIterator, eventIterator);
	  
		//Initialize counts:
		type2codeCount = new HashMap<String, CountingSet<String>>();
		type2codeCountFirstOnly = new HashMap<String, CountingSet<String>>();
	  
		while (iterator.hasNext())
		{
			ProgressHandler.reportProgress();
	  		processPatient(iterator.next());
		}
	  
		writeOutput(target);
	}
	
	private void processPatient(PatientEvent patientEvent) 
	{
		Set<String> seenTypes = new HashSet<String>();
		long startDate = patientEvent.patient.startdate;
		long endDate = patientEvent.patient.enddate;
		
		for (Event event : patientEvent.events)
		{
			boolean first = seenTypes.add(event.eventType);
		  
			if (event.date >= startDate && event.date < endDate)
			{
				if (event.code == null)
					throw new RuntimeException("There are no codes specified for the events.");
		  	
				addCount(event.eventType, event.code, type2codeCount);
		  
				if (first)
					addCount(event.eventType, event.code, type2codeCountFirstOnly);
			}
		}
	}
	
	private void addCount(String eventType, String code, Map<String, CountingSet<String>> type2count)
	{
		CountingSet<String> counts = type2count.get(eventType);
	
		if (counts == null)
		{
			counts = new CountingSet<String>();
			type2count.put(eventType,counts);
		}
		
		counts.add(code);		
	}
	
	private void writeOutput(String target) 
	{
		WriteCSVFile out = new WriteCSVFile(target);
		List<String> header = new ArrayList<String>();
		header.add("EventType");
		header.add("Code");
		header.add("Count");
		header.add("FirstCount");
		out.write(header);
		
		for (String type : type2codeCount.keySet())
		{
			CountingSet<String> codeCount = type2codeCount.get(type);	
			CountingSet<String> codeCountfirstOnly = type2codeCountFirstOnly.get(type);
			
			for (String code : codeCount)
			{
				List<String> cells = new ArrayList<String>();
				cells.add(type);
				cells.add(code);
				cells.add(Integer.toString(codeCount.getCount(code)));
				if (codeCountfirstOnly == null)
					cells.add("0");
				else
					cells.add(Integer.toString(codeCountfirstOnly.getCount(code)));
				out.write(cells);
			}
		}
		out.close();
	}

	private class PatientEvent 
	{
		Patient patient;
		List<Event> events = new ArrayList<Event>();
	}
	
	private class PatientEventIterator implements Iterator<PatientEvent>
	{
		private Iterator<Patient> patientIterator;
		private Iterator<Event> eventIterator;
		private PatientEvent buffer;
		private Event event;
		
		public PatientEventIterator(Iterator<Patient> patientIterator, Iterator<Event> eventIterator)
		{
			this.patientIterator = patientIterator;
			this.eventIterator = eventIterator;
	    
			if (eventIterator.hasNext())
				event = eventIterator.next();
			
			readNext();
		}
		
		@Override
		public boolean hasNext() 
		{
			return (buffer != null);
		}

		@Override
		public PatientEvent next() 
		{
			PatientEvent result = buffer;
			readNext();
			return result;
		}

		private void readNext() 
		{
			if (patientIterator.hasNext())
			{
				buffer = new PatientEvent();
				buffer.patient = patientIterator.next();
				String patientID = buffer.patient.patientID;

				//Skip to event belonging to this patient:
				while (event != null && event.patientID.compareTo(patientID)<0)
				{
					if  (eventIterator.hasNext())
						event = eventIterator.next();  
					else
						event = null;
				}
				
				//Iterate through events until we hit the next patient:
				while (event != null && event.patientID.equals(patientID))
				{
					buffer.events.add(event);
	        
					if (eventIterator.hasNext())
						event = eventIterator.next();
					else  
						break; //out of events 
				}
			} 
			else 
			{
				buffer = null;
			}
		}

		@Override
		public void remove() 
		{
			System.err.println("Calling unimplemented remove method in class " + this.getClass().getName());
		}
	}
}
