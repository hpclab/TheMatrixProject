package org.erasmusmc.jerboa.dataClasses;

import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class EventFileWriter {
  private WriteCSVFile file;
  private boolean headerWritten = false;
  private boolean writeCode = false;
  
  public EventFileWriter(String filename){
    file = new WriteCSVFile(filename);
  }
  
  public void write(Event event){
    if (!headerWritten)
      writeHeader(event);
    List<String> columns = new ArrayList<String>();
    columns.add(event.patientID);
    columns.add(StringUtilities.daysToSortableDateString(event.date));
    columns.add(event.eventType);
    if (writeCode)
    	if (event.code == null)
    		columns.add("");
    	else
    	  columns.add(event.code);
    file.write(columns);
  }
  
  private void writeHeader(Event event) {
    List<String> headers = new ArrayList<String>();
    headers.add("PatientID");
    headers.add("Date");
    headers.add("EventType");
    if (event.code != null){
    	writeCode = true;
    	headers.add("Code");
    }
    file.write(headers);
    headerWritten = true;
  }

  public void flush(){
    file.flush();
  }
  
  public void close(){
    file.close();
  }
}