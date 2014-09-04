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