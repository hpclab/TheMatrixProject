package org.erasmusmc.jerboa.dataClasses;

import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class ExposureFileWriter {
  private WriteCSVFile file;
  private boolean headerWritten = false;
  public ExposureFileWriter(String filename){
    file = new WriteCSVFile(filename);
  }
  
  public void write(Exposure exposure){
    if (!headerWritten)
      writeHeader();
    List<String> columns = new ArrayList<String>();
    columns.add(exposure.patientID);
    columns.add(exposure.caseset);
    columns.add(StringUtilities.daysToSortableDateString(exposure.start));
    columns.add(exposure.type);
    file.write(columns);
  }
  
  private void writeHeader() {
    List<String> headers = new ArrayList<String>();
    headers.add("PatientID");
    headers.add("CaseSet");
    headers.add("Date");
    headers.add("Type");
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