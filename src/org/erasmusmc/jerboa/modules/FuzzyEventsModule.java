package org.erasmusmc.jerboa.modules;

import java.util.Iterator;

import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.EventFileWriter;
import org.erasmusmc.jerboa.dataClasses.PatientFuzzyOffset;
import org.erasmusmc.jerboa.dataClasses.PatientFuzzyOffsetFileReader;
import org.erasmusmc.utilities.FileSorter;

public class FuzzyEventsModule extends JerboaModule {

	private static final long serialVersionUID = 3262668432314394778L;
	
	public JerboaModule events;
	public JerboaModule fuzzydata;
	
	public static void main(String[] args) {
		FuzzyEventsModule module = new FuzzyEventsModule();
		String path = "D:\\Documents\\IPCI\\Vaccinaties\\Vaesco\\Jerboa Test\\Jerboa\\SCCS\\";
		FileSorter.sort(path + "Events.txt", new String[]{"PatientID", "Date", "EventType"});
		FileSorter.sort(path + "FuzzyOffSets.txt", new String[]{"PatientID"});
		module.process(path + "Events.txt", path + "FuzzyOffSets.txt", path + "FuzzyEvents.txt");
	}
	
	protected void runModule(String outputFilename) {
		FileSorter.sort(events.getResultFilename(), new String[]{"PatientID", "Date", "EventType"});
		FileSorter.sort(fuzzydata.getResultFilename(), new String[]{"PatientID"});
		process(events.getResultFilename(), fuzzydata.getResultFilename(), outputFilename);
	}

	public void process(String sourceEvents, String sourceFuzzyData, String targetFuzzyEvents) {
		Event currentEvent;
		PatientFuzzyOffset currentFuzzyData;		
		long fuzzyOffset;
		
		EventFileReader events = new EventFileReader(sourceEvents);
		Iterator<Event> eventIterator = events.iterator();
		
		PatientFuzzyOffsetFileReader fuzzyData = new PatientFuzzyOffsetFileReader(sourceFuzzyData);
		Iterator<PatientFuzzyOffset> fuzzyDataIterator = fuzzyData.iterator();

		EventFileWriter fuzzyEvents = new EventFileWriter(targetFuzzyEvents);
		
		if (eventIterator.hasNext()) 
			currentEvent = eventIterator.next();
		else
			currentEvent = null;
		
		if (fuzzyDataIterator.hasNext()) 
			currentFuzzyData = fuzzyDataIterator.next();
		else
			currentFuzzyData = null;
		
		while (currentEvent != null) {
			fuzzyOffset = 0;
			while ((currentFuzzyData != null) && (currentFuzzyData.patientID.compareTo(currentEvent.patientID) < 0)) {
				if (fuzzyDataIterator.hasNext()) 
					currentFuzzyData = fuzzyDataIterator.next();
				else
					currentFuzzyData = null;
			}
			if ((currentFuzzyData != null) && (currentFuzzyData.patientID.compareTo(currentEvent.patientID) == 0)) {
				fuzzyOffset = currentFuzzyData.fuzzyOffset;
			}
			
			currentEvent.date = currentEvent.date + fuzzyOffset;
			
			fuzzyEvents.write(currentEvent);
			//fuzzyEvents.flush();
			
			if (eventIterator.hasNext()) 
				currentEvent = eventIterator.next();
			else
				currentEvent = null;
		}
		
		fuzzyEvents.close();
	}

}
