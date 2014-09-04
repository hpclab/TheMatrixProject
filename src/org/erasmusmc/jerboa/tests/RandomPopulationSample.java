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
package org.erasmusmc.jerboa.tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;


public class RandomPopulationSample {
  public static void main(String[] args){
  	int sampleSize = 10000;
  	String folder = "x:/Gold/";
  	
    String standardPrescriptionsFile = folder+"Prescriptions.txt";
    String standardPatientsFile = folder+"Patients.txt";
    String standardEventsFile = folder+"Events.txt"; 
    String samplePrescriptionsFile = folder+"PrescriptionsSample.txt";
    String samplePatientsFile = folder+"PatientsSample.txt";
    String sampleEventsFile = folder+"EventsSample.txt";
    
    Set<String> patientsSample = samplePatients(standardPatientsFile, sampleSize);
    filter(standardPrescriptionsFile, samplePrescriptionsFile, patientsSample);
    filter(standardPatientsFile, samplePatientsFile, patientsSample);
    filter(standardEventsFile, sampleEventsFile, patientsSample);
  }
  
  private static void filter(String source,String target, Set<String> patientIDs) {
		WriteCSVFile out = new WriteCSVFile(target);
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next();
		int patientIDCol = StringUtilities.caseInsensitiveIndexOf("patientid", header);
		out.write(header);
		int count = 0;
		while(iterator.hasNext()){
			List<String> cells = iterator.next();
			String patientID = cells.get(patientIDCol);
			if (patientIDs.contains(patientID)){
				out.write(cells);
				count++;
			}
		}
		System.out.println("Sampled " + count + " from " + source);
		out.close();		
	}

	private static Set<String> samplePatients(String source, int sampleSize){
    PatientFileReader in = new PatientFileReader(source);
    List<Patient> patients = new ArrayList<Patient>();
    for (Patient patient : in)
      patients.add(patient);
    //random sample
    Random random = new Random(0);
    Set<String> patientsSample = new HashSet<String>(sampleSize);
    for (int i = 0; i < sampleSize; i++)
      patientsSample.add(patients.remove(random.nextInt(patients.size())).patientID);

    return patientsSample;
  }
}
