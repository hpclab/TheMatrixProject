package org.erasmusmc.jerboa.dataClasses;

import java.util.Iterator;
import java.util.List;

public class PatientFuzzyOffsetFileReader implements Iterable<PatientFuzzyOffset> {
	private String filename;

	public PatientFuzzyOffsetFileReader(String filename) {
	    this.filename = filename;
	}

	public Iterator<PatientFuzzyOffset> iterator() {
		return new PatientFuzzyOffsetIterator(filename);
	}

	private class PatientFuzzyOffsetIterator extends InputFileIterator<PatientFuzzyOffset> {
		private int patientID;
	    private int fuzzyOffset;
	    
	    public PatientFuzzyOffsetIterator(String filename) {
			super(filename);
		}
	    
	    public void processHeader(List<String> row){
	    	patientID = findIndex("PatientID", row);
	    	fuzzyOffset = findIndex("FuzzyOffset", row);
	    }

	    public PatientFuzzyOffset row2object(List<String> columns) throws Exception{
	      PatientFuzzyOffset patientFuzzyOffset = new PatientFuzzyOffset();
	      patientFuzzyOffset.patientID = columns.get(patientID);
	      try {
	    	  patientFuzzyOffset.fuzzyOffset = Long.parseLong(columns.get(fuzzyOffset));
	      } catch (NumberFormatException nfe) {
	    	  System.out.println("NumberFormatException: " + nfe.getMessage());
	      }

	      return patientFuzzyOffset;
	    }
	}

}
