package org.erasmusmc.jerboa.modules;

import java.util.Iterator;

import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.jerboa.dataClasses.PatientFileWriter;
import org.erasmusmc.jerboa.dataClasses.PatientFuzzyOffset;
import org.erasmusmc.jerboa.dataClasses.PatientFuzzyOffsetFileReader;
import org.erasmusmc.utilities.FileSorter;

public class FuzzyPatientsModule extends JerboaModule {
	
	private static final long serialVersionUID = 2358082634523269324L;
	
	public JerboaModule patients;
	public JerboaModule fuzzydata;

	public static void main(String[] args) {
		FuzzyPatientsModule module = new FuzzyPatientsModule();
		String path = "D:\\Documents\\IPCI\\Vaccinaties\\Vaesco\\Jerboa Test\\Jerboa\\SCCS\\";
		FileSorter.sort(path + "Patients.txt", new String[]{"PatientID"});
		FileSorter.sort(path + "FuzzyOffSets.txt", new String[]{"PatientID"});
		module.process(path + "Patients.txt", path + "FuzzyOffSets.txt", path + "FuzzyPatients.txt");
	}
	
	protected void runModule(String outputFilename) {
		FileSorter.sort(patients.getResultFilename(), new String[]{"PatientID"});
		FileSorter.sort(fuzzydata.getResultFilename(), new String[]{"PatientID"});
		process(patients.getResultFilename(), fuzzydata.getResultFilename(), outputFilename);
	}

	public void process(String sourcePatients, String sourceFuzzyData, String targetFuzzyPatients) {
		Patient currentPatient;
		PatientFuzzyOffset currentFuzzyData;		
		long fuzzyOffset;
		
		PatientFileReader patients = new PatientFileReader(sourcePatients);
		Iterator<Patient> patientIterator = patients.iterator();
		
		PatientFuzzyOffsetFileReader fuzzyData = new PatientFuzzyOffsetFileReader(sourceFuzzyData);
		Iterator<PatientFuzzyOffset> fuzzyDataIterator = fuzzyData.iterator();

		PatientFileWriter fuzzyPatients = new PatientFileWriter(targetFuzzyPatients);
		
		if (patientIterator.hasNext()) 
			currentPatient = patientIterator.next();
		else
			currentPatient = null;
		
		if (fuzzyDataIterator.hasNext()) 
			currentFuzzyData = fuzzyDataIterator.next();
		else
			currentFuzzyData = null;
		
		while (currentPatient != null) {
			fuzzyOffset = 0;
			while ((currentFuzzyData != null) && (currentFuzzyData.patientID.compareTo(currentPatient.patientID) < 0)) {
				if (fuzzyDataIterator.hasNext()) 
					currentFuzzyData = fuzzyDataIterator.next();
				else
					currentFuzzyData = null;
			}
			if ((currentFuzzyData != null) && (currentFuzzyData.patientID.compareTo(currentPatient.patientID) == 0)) {
				fuzzyOffset = currentFuzzyData.fuzzyOffset;
			}
			
			currentPatient.birthdate = currentPatient.birthdate + fuzzyOffset;
			currentPatient.startdate = currentPatient.startdate + fuzzyOffset;
			currentPatient.enddate = currentPatient.enddate + fuzzyOffset;
			
			fuzzyPatients.write(currentPatient);
			//fuzzyPatients.flush();
			
			if (patientIterator.hasNext()) 
				currentPatient = patientIterator.next();
			else
				currentPatient = null;
		}
		
		fuzzyPatients.close();
	}

}
