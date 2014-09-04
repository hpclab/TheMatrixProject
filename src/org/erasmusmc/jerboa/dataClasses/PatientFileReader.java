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

public class PatientFileReader implements Iterable<Patient> {
  private String filename;
  public static boolean allowUnknownDates = false;
  public static boolean allowUnknownGenders = false;

  public PatientFileReader(String filename) {
    this.filename = filename;
  }

  public Iterator<Patient> iterator() {
    return new PatientIterator(filename);
  }

	private class PatientIterator extends InputFileIterator<Patient> {
    private int patientID;
    private int birthdate;
    private int gender;
    private int startdate;
    private int enddate;
    
    public PatientIterator(String filename) {
      super(filename);
    }
    
    public void processHeader(List<String> row){
      patientID = findIndex("patientID", row);
      birthdate = findIndex("Birthdate", row);
      gender = findIndex("Gender", row);
      startdate = findIndex("Startdate", row);
      enddate = findIndex("Enddate", row);
    }

    public Patient row2object(List<String> columns) throws Exception{
      Patient patient = new Patient();
      patient.patientID = columns.get(patientID);
      patient.birthdate = InputFileUtilities.convertToDate(columns.get(birthdate),allowUnknownDates);
      
      patient.gender = InputFileUtilities.convertToGender(columns.get(gender),allowUnknownGenders);

      
      patient.startdate = InputFileUtilities.convertToDate(columns.get(startdate),allowUnknownDates);
      patient.enddate = InputFileUtilities.convertToDate(columns.get(enddate),allowUnknownDates)+1;
      if (patient.enddate < patient.startdate)
      	throw new IllegalArgumentException("Patient end date before start date");
      return patient;
    }
  }
}