package org.erasmusmc.jerboa.dataClasses;

import java.util.Iterator;
import java.util.List;

public class ExposureFileReader implements Iterable<Exposure> 
{
	private String filename;

	public ExposureFileReader(String filename) 
	{
		this.filename = filename;
	}

	public Iterator<Exposure> iterator() 
	{
		return new ExposureIterator(filename);
	}

	private class ExposureIterator extends InputFileIterator<Exposure> 
	{
		private int patientID;
		private int caseset;
		private int startdate;
		private int type;
    
		public ExposureIterator(String filename) 
		{
			super(filename);
		}
    
		public void processHeader(List<String> row)
		{
			patientID = findIndex("patientID", row);
			caseset = findIndex("CaseSet", row);
			startdate = findIndex("Date", row);
			type = findIndex("Type", row);
		}

		public Exposure row2object(List<String> columns) throws Exception
		{
			Exposure exposure = new Exposure();
			exposure.patientID = columns.get(patientID);
			exposure.start = InputFileUtilities.convertToDate(columns.get(startdate),false);
			exposure.caseset = columns.get(caseset);
			exposure.type = columns.get(type);
			return exposure;
		}
	}
}