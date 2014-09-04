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

import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;

public class PrescriptionFileReader implements Iterable<Prescription> {
  private String filename;

  public PrescriptionFileReader(String filename) {
    this.filename = filename;
  }
  
  public Iterator<Prescription> iterator() {
    return new PrescriptionIterator(filename);
  }

  public static class PrescriptionIterator extends InputFileIterator<Prescription> {
    private int date;
    private int patientID;
    private int duration;
    private int atc;
    private int dose;
    
    public PrescriptionIterator(String filename) {
      super(filename);
    }
    
    protected void processHeader(List<String> row){
      patientID = findIndex("patientID", row);
      date = findIndex("Date", row);
      duration = findIndex("Duration", row);
      atc = findIndex("ATC", row);
      dose = findIndexOptional("Dose", row);
    }

    protected Prescription row2object(List<String> columns) throws Exception{
      Prescription prescription = new Prescription();
      prescription.start = InputFileUtilities.convertToDate(columns.get(date),true);
      prescription.patientID = columns.get(patientID);
      if (columns.get(duration).length() == 0)
      	prescription.duration = ConstantValues.UNKNOWN_DURATION;
      else{
      	prescription.duration = Math.round(Float.parseFloat(columns.get(duration)));
        if (prescription.duration < 0)
      	  throw new RuntimeException("Negative prescription duration: " + columns.get(duration));
      }
      prescription.setATCCodes(columns.get(atc));
      if (dose != -1) {
      	String doseString = columns.get(dose);
      	checkDoseString(doseString);
      	for (ATCCode atcCode : prescription.atcCodes)
      		atcCode.dose = doseString;
      }
      return prescription;
    }

		private void checkDoseString(String doseString) {
			for (int i = 0; i < doseString.length(); i++){
				char ch = doseString.charAt(i);
				if (ch == '_' || ch == ':' || ch == '+')
					throw new RuntimeException("Illegal characters in dose: " + doseString);
			}
		}
  }
}
