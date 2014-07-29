package org.erasmusmc.jerboa.dataClasses;

import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.utilities.WriteCSVFile;

public class PatientFuzzyOffsetFileWriter {
	private WriteCSVFile file;
	private boolean headerWritten = false;

	public PatientFuzzyOffsetFileWriter(String filename){
		file = new WriteCSVFile(filename);
	}
	  
	public void write(PatientFuzzyOffset patientFuzzyOffset){
		if (!headerWritten)
			writeHeader();
		List<String> columns = new ArrayList<String>();
	    columns.add(patientFuzzyOffset.patientID);
	    columns.add(Long.toString(patientFuzzyOffset.fuzzyOffset));
	    file.write(columns);
	  }
	  
	  private void writeHeader() {
	    List<String> headers = new ArrayList<String>();
	    headers.add("PatientID");
	    headers.add("FuzzyOffset");
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
