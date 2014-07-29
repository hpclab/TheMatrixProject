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
