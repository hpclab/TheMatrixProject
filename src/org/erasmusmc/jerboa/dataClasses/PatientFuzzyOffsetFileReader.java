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
