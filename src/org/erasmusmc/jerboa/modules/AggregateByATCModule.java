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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.MergedDataFileReader;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Aggregates a merged data file at the level of unique values in the ATC field.
 * @author schuemie
 *
 */
public class AggregateByATCModule extends JerboaModule{
	
  public JerboaModule mergedData;
  
  /**
   * The minimum number of subjects in a row of the resulting aggregated table. Rows with fewer subjects
   * are deleted, and deletions will be summarized in the last line of the table.<BR>
   * default = 0
   */
  public int minSubjectsPerRow = 0;
  
  /**	
   * Create aggregates for each calendar year.<BR>
   * Warning: the input table should be split by calendar year (i.e it should not contain episodes spanning
   * two years).<BR>
   * default = false 
   */
  public boolean includeYear = false;

  private Data statistics;
  private Data deleted;
  private static final long serialVersionUID = -429537554154644439L;
  
  public static void main(String[] args){
  	String folder = "x:/Study5/";
  	String sourceFile = folder + "Mergedata.txt";
    FileSorter.sort(sourceFile, new String[]{"ATC", "AgeRange", "Gender", "PrecedingEventTypes", "Date", "PatientID"});
    AggregateByATCModule module = new AggregateByATCModule();
    module.includeYear = false;
    module.process(sourceFile, folder+"AggregatebyATCCombination.txt");
  }

  protected void runModule(String outputFilename){
		FileSorter.sort(mergedData.getResultFilename(), new String[]{"ATC", "AgeRange", "Gender", "PrecedingEventTypes", "Date", "PatientID"});
 		process(mergedData.getResultFilename(), outputFilename);
  }

  public void process(String source, String target) {
  	WriteCSVFile out = new WriteCSVFile(target);
  	generateHeader(out);
    MergedDataFileReader in = new MergedDataFileReader(source);
    Set<ATCCode> oldCodes = new HashSet<ATCCode>();
    Set<String> oldPrecedingEventTypes = new HashSet<String>();
    byte oldGender = 0;
    String oldAge = "";
    String oldPatientID = "";
    int oldYear = -1;
    statistics = new Data();
    deleted = new Data();

    long exposure = -1;
    EventCounts eventCounts = new EventCounts();
    int subjects = 0;

    for (MergedData mergedData : in)
    	if (!mergedData.outsideCohortTime){
    		ProgressHandler.reportProgress();
    		int year = -1;
    		if (includeYear)
    			year = mergedData.year;
    		
    		if (!mergedData.atcCodes.equals(oldCodes) || 
    				mergedData.gender != oldGender || 
    				!mergedData.ageRange.equals(oldAge) || 
    				(includeYear && year != oldYear) ||
    				!mergedData.precedingEventTypes.equals(oldPrecedingEventTypes)){
    			if (exposure != -1)
    				writeData(out, oldCodes, oldGender, oldAge, oldYear, oldPrecedingEventTypes, exposure, eventCounts, subjects);
    			oldCodes = mergedData.atcCodes;
    			oldAge = mergedData.ageRange;
    			oldGender = mergedData.gender;
    			oldPatientID = mergedData.patientID;
    			oldYear = year;
    			oldPrecedingEventTypes = mergedData.precedingEventTypes;
    			exposure = 0;
    			eventCounts = new EventCounts();
    			subjects = 1;
    		}

    		exposure += mergedData.duration;
    		eventCounts.add(mergedData.events);
    		if (!mergedData.patientID.equals(oldPatientID)){
    			oldPatientID = mergedData.patientID;
    			subjects++;
    		}
    	}
    writeData(out, oldCodes, oldGender, oldAge, oldYear, oldPrecedingEventTypes, exposure, eventCounts, subjects);
    //out.write(Arrays.asList("* deleted: " + deleted.subjects + " subjects," + deleted.days +" days, and " + deleted.events+ " events."));
    System.out.println("Aggregated table has " + statistics.subjects + " rows, contains " + statistics.days + " days, and " + statistics.events + " events");
    if (deleted.subjects != 0)
      System.out.println("Deleted " + deleted.subjects + " rows with too few subjects, containing " + deleted.days + " days, and " + deleted.events + " events");
    out.close();
    
    //Dereference data objects:
    statistics = null;
    deleted = null;
  }   
  
  private class EventCounts {
  	private CountingSet<String> counts = new CountingSet<String>();
  	public void add(List<Event> events){
  		for (Event event : events)
  			counts.add(event.eventType);
  	}
		public int getNumberOfEvents() {
			int count = 0;
			for (String eventType : counts)
				count += counts.getCount(eventType);
			return count;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (String eventType : counts){
				if (sb.length() != 0)
					sb.append("+");
				sb.append(eventType);
				sb.append(":");
				sb.append(counts.getCount(eventType));
			}
			return sb.toString();	
		}
  }

  private void generateHeader(WriteCSVFile out) {
    List<String> header = new ArrayList<String>();
    header.add("ATC");
    header.add("AgeRange");
    header.add("Gender");
    if (includeYear)
      header.add("Year");
    header.add("Days");
    header.add("Subjects");
    header.add("Events");
    header.add("PrecedingEventTypes");
    out.write(header);
  }

  private void  writeData(WriteCSVFile out, Set<ATCCode> codes, byte gender, String age, int year, Set<String> precedingEventTypes, long exposure, EventCounts eventCounts, int subjects) {
    if (subjects < minSubjectsPerRow){
      deleted.days += exposure;
      deleted.events += eventCounts.getNumberOfEvents();
      deleted.subjects++;
    } else {
      List<String> data = new ArrayList<String>();
      data.add(StringUtilities.joinSorted(codes, "+"));
      data.add(age);
      data.add((gender == Patient.MALE)?"M":(gender == Patient.FEMALE)?"F":"");
      if (includeYear)
        data.add(Integer.toString(year));
      data.add(Long.toString(exposure));
      data.add(Integer.toString(subjects));
      data.add(eventCounts.toString());
      data.add(StringUtilities.joinSorted(precedingEventTypes, "+"));
      out.write(data);
      statistics.subjects++;
      statistics.events += eventCounts.getNumberOfEvents();
      statistics.days += exposure;
    }
  }

  private class Data {
    long days = 0;
    int subjects = 0;
    int events = 0;
  }
}
