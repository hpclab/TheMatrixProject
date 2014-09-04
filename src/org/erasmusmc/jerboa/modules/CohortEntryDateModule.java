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
package org.erasmusmc.jerboa.modules;

import java.util.zip.DataFormatException;

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.jerboa.dataClasses.PatientFileWriter;
import org.erasmusmc.utilities.StringUtilities;

/**
 * Calculates, based on the system entry and exit dates in the input patients file, the cohort entry and 
 * exit dates.
 * @author schuemie
 *
 */
public class CohortEntryDateModule extends JerboaModule{
	
	public JerboaModule patients;
	
	/**
	 * The default amount of follow-up time (i.e. number of days between system and cohort entry date).<BR>
	 * default = 365
	 */
  public int followUpTime = 365;
  
  /**
   * If the system entry date is less than this number of days from the birthdate of the patient, the cohort 
   * entry date is set to the birthdate.<BR>
   * default = 265
   */
  public int maxDaysFromBirth = 365;
  
  /**
   * Start date of the study. If set, all patient start dates are limited to this date. Format:
   * YYYYMMDD.<BR>
   * default = not set (dates are not limited)
   */
  public String studyStart = null;
  
  /**
   * End date of the study. If set, all patient end dates are limited to this date. Format:
   * YYYYMMDD.<BR>
   * default = not set (dates are not limited)
   */
  public String studyEnd = null;
	
  private static final long serialVersionUID = -4388441531980370642L;
  
  public static void main(String[] args){
  	String folder = "C:/home/data/Simulated/AgeRange/";
    CohortEntryDateModule mod = new CohortEntryDateModule();
    mod.process(folder+"patients.txt" , folder+"Cohortentrydate.txt");
  }
  
  protected void runModule(String outputFilename){
    process(patients.getResultFilename(), outputFilename);
  }

  public void process(String source, String target) {
    PatientFileReader in = new PatientFileReader(source);
    PatientFileWriter out = new PatientFileWriter(target);
    int count = 0;
    int countBirthdate = 0;
    int countRemoved = 0;
    long studyStartDate = parseDate(studyStart);
    long studyEndDate = parseDate(studyEnd);
    
    for (Patient patient : in){
      ProgressHandler.reportProgress();
      count++;
      if (patient.startdate - patient.birthdate < maxDaysFromBirth) {
        patient.startdate = patient.birthdate;
        countBirthdate++;
      } else {
        patient.startdate = patient.startdate + followUpTime;
      }
      if (studyStartDate != -1 && patient.startdate < studyStartDate)
      	patient.startdate = studyStartDate;
      
      if (studyEndDate != -1 && patient.enddate > studyEndDate)
      	patient.enddate = studyEndDate;
      
      if (patient.startdate >= patient.enddate)
        countRemoved++;
      else 
        out.write(patient);
    }
    out.close();
    System.out.println("Of the " + count + " patients, " + countBirthdate + " have their date of birth as cohort entry date");
    System.out.println(countRemoved + " patients did not have enough data and were removed");
  }

	private long parseDate(String date) {
		if (date == null)
			return -1;
		try {
			return StringUtilities.sortableTimeStringToDays(date);
		} catch (DataFormatException e) {
			throw new RuntimeException("Illegal date in settings for "+this.getClass().getName()+": " + date);
		}
	}
}
