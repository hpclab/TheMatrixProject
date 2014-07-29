package org.erasmusmc.jerboa.dataClasses;

import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class PatientFileWriter {
  private WriteCSVFile file;
  private boolean headerWritten = false;
  public PatientFileWriter(String filename){
    file = new WriteCSVFile(filename);
  }
  
  public void write(Patient patient){
    if (!headerWritten)
      writeHeader();
    List<String> columns = new ArrayList<String>();
    columns.add(patient.patientID);
    if (patient.birthdate == ConstantValues.UNKNOWN_DATE)
    	columns.add("");
    else
      columns.add(StringUtilities.daysToSortableDateString(patient.birthdate));
    columns.add((patient.gender == Patient.MALE)?"M":(patient.gender == Patient.FEMALE)?"F":"");
    if (patient.startdate == ConstantValues.UNKNOWN_DATE) 
    	columns.add("");
    else
      columns.add(StringUtilities.daysToSortableDateString(patient.startdate));
    if (patient.enddate == ConstantValues.UNKNOWN_DATE)
    	columns.add("");
    else
      columns.add(StringUtilities.daysToSortableDateString(patient.enddate-1));
    file.write(columns);
  }
  
  private void writeHeader() {
    List<String> headers = new ArrayList<String>();
    headers.add("PatientID");
    headers.add("Birthdate");
    headers.add("Gender");
    headers.add("Startdate");
    headers.add("Enddate");
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
