package org.erasmusmc.jerboa.dataClasses;

import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class MergedDataFileWriter {
  private WriteCSVFile file;
  private boolean headerWritten = false;
  private boolean writeMonths = false;
  private boolean writeYears = false;
  private boolean writeWeeks = false;
  public MergedDataFileWriter(String filename){
    file = new WriteCSVFile(filename);
  }
  
  public void write(MergedData mergedData){
    if (!headerWritten)
      writeHeader(mergedData);
    List<String> columns = new ArrayList<String>();
    columns.add(StringUtilities.daysToSortableDateString(mergedData.start));
    if (writeYears)
    	columns.add(Integer.toString(mergedData.year));
    if (writeMonths)
    	columns.add(Integer.toString(mergedData.month));
    if (writeWeeks)
    	columns.add(Integer.toString(mergedData.week));
    columns.add(mergedData.patientID);
    columns.add(Long.toString(mergedData.duration));
    columns.add(mergedData.getATCCodesAsString());
    columns.add(toString(mergedData.events));
    columns.add(mergedData.ageRange);
    columns.add((mergedData.gender == Patient.MALE)?"M":"F");
    columns.add(StringUtilities.joinSorted(mergedData.precedingEventTypes, "+"));
    columns.add(mergedData.outsideCohortTime?"1":"");
    file.write(columns);
  }
  
  private String toString(List<Event> events) {
    StringBuilder sb = new StringBuilder();
    for (Event event : events){
      if (sb.length() != 0)
        sb.append("+");
      sb.append(event.eventType);
      sb.append(':');
      sb.append(StringUtilities.daysToSortableDateString(event.date));
    }
    return sb.toString();
  }

  private void writeHeader(MergedData mergedData) {
    List<String> headers = new ArrayList<String>();
    headers.add("Date");
    if (mergedData.year != -1){
    	headers.add("Year");
    	writeYears = true;
    }
    if (mergedData.month != -1){
    	headers.add("Month");
    	writeMonths = true;
    }
    if (mergedData.week != -1){
    	headers.add("Week");
    	writeWeeks = true;
    }
    headers.add("PatientID");
    headers.add("Duration");
    headers.add("ATC");
    headers.add("Events");
    headers.add("AgeRange");
    headers.add("Gender");
    headers.add("PrecedingEventTypes");
    headers.add("OutsideCohortTime");
    
    file.write(headers);
    headerWritten = true;
  }

  public void flush(){
    file.flush();
  }
  
  public void close(){
    if (!headerWritten){
      writeHeader(new MergedData()); //Create default header
      System.err.println("Warning: Empty merged data file created");
    }
    file.close();
  }
}