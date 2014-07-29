package org.erasmusmc.jerboa.dataClasses;

import java.util.Iterator;
import java.util.List;

import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader.PrescriptionIterator;

public class ExtendedPrescriptionFileReader implements Iterable<ExtendedPrescription>{
  private String filename;
  
	public ExtendedPrescriptionFileReader(String filename){
    this.filename = filename;
	}
	
	@Override
	public Iterator<ExtendedPrescription> iterator() {
		return new DummyIterator(filename);
	}
	
	//Dummy structure for casting Prescription to ExtendedPrescription
	public static class DummyIterator implements Iterator<ExtendedPrescription>{

		private ExtendedPrescriptionIterator iterator;
		
		public DummyIterator(String filename){
			iterator = new ExtendedPrescriptionIterator(filename);
		}
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public ExtendedPrescription next() {
			return (ExtendedPrescription)iterator.next();
		}

		@Override
		public void remove() {
			iterator.remove();
		}
		
	}
	
	public static class ExtendedPrescriptionIterator extends PrescriptionIterator {
		private int dddTotal;
		private int totalUnits;
		private int unitsPerDay;
    
    public ExtendedPrescriptionIterator(String filename) {
      super(filename);
    }
    
    protected void processHeader(List<String> row){
    	super.processHeader(row);
    	dddTotal = findIndex("DDDTotal", row);
    	totalUnits = findIndex("TotalUnits", row);
    	unitsPerDay = findIndex("UnitsPerDay", row);
    }

    protected Prescription row2object(List<String> columns) throws Exception{
      ExtendedPrescription prescription = new ExtendedPrescription(super.row2object(columns));
      prescription.dddTotal = parseDouble(columns.get(dddTotal));
      prescription.totalUnits = parseDouble(columns.get(totalUnits));
      prescription.unitsPerDay = parseDouble(columns.get(unitsPerDay));
      return prescription;
    }

		private double parseDouble(String string) {
			if (string.length() == 0)
				return -1;
			else {
			  double number = Double.parseDouble(string);
			  if (number < 0)
			  	throw new RuntimeException("Found negative number: " + string);
			  return number;
			}
		}
  }
}
