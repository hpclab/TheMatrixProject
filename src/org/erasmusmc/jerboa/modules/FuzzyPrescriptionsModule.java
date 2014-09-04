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

import java.util.Iterator;

import org.erasmusmc.jerboa.dataClasses.PatientFuzzyOffset;
import org.erasmusmc.jerboa.dataClasses.PatientFuzzyOffsetFileReader;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileWriter;
import org.erasmusmc.utilities.FileSorter;

public class FuzzyPrescriptionsModule extends JerboaModule {
	
	private static final long serialVersionUID = 7795397461463805390L;
	
	public JerboaModule prescriptions;
	public JerboaModule fuzzydata;

	public static void main(String[] args) {
		FuzzyPrescriptionsModule module = new FuzzyPrescriptionsModule();
		String path = "D:\\Documents\\IPCI\\Vaccinaties\\Vaesco\\Jerboa Test\\Jerboa\\SCCS\\";
		FileSorter.sort(path + "Prescriptions.txt", new String[]{"PatientID", "Date"});
		FileSorter.sort(path + "FuzzyOffSets.txt", new String[]{"PatientID"});
		module.process(path + "Prescriptions.txt", path + "FuzzyOffSets.txt", path + "FuzzyPrescriptions.txt");
	}
	
	protected void runModule(String outputFilename) {
		FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID", "Date"});
		FileSorter.sort(fuzzydata.getResultFilename(), new String[]{"PatientID"});
		process(prescriptions.getResultFilename(), fuzzydata.getResultFilename(), outputFilename);
	}

	public void process(String sourcePrescriptions, String sourceFuzzyData, String targetFuzzyPrescriptions) {
		Prescription currentPrescription;
		PatientFuzzyOffset currentFuzzyData;		
		long fuzzyOffset;
		
		PrescriptionFileReader prescriptions = new PrescriptionFileReader(sourcePrescriptions);
		Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
		
		PatientFuzzyOffsetFileReader fuzzyData = new PatientFuzzyOffsetFileReader(sourceFuzzyData);
		Iterator<PatientFuzzyOffset> fuzzyDataIterator = fuzzyData.iterator();

		PrescriptionFileWriter fuzzyPrescriptions = new PrescriptionFileWriter(targetFuzzyPrescriptions);
		
		if (prescriptionIterator.hasNext()) 
			currentPrescription = prescriptionIterator.next();
		else
			currentPrescription = null;
		
		if (fuzzyDataIterator.hasNext()) 
			currentFuzzyData = fuzzyDataIterator.next();
		else
			currentFuzzyData = null;
		
		while (currentPrescription != null) {
			fuzzyOffset = 0;
			while ((currentFuzzyData != null) && (currentFuzzyData.patientID.compareTo(currentPrescription.patientID) < 0)) {
				if (fuzzyDataIterator.hasNext()) 
					currentFuzzyData = fuzzyDataIterator.next();
				else
					currentFuzzyData = null;
			}
			if ((currentFuzzyData != null) && (currentFuzzyData.patientID.compareTo(currentPrescription.patientID) == 0)) {
				fuzzyOffset = currentFuzzyData.fuzzyOffset;
			}
			
			currentPrescription.start = currentPrescription.start + fuzzyOffset;
			
			fuzzyPrescriptions.write(currentPrescription);
			//fuzzyPrescriptions.flush();
			
			if (prescriptionIterator.hasNext()) 
				currentPrescription = prescriptionIterator.next();
			else
				currentPrescription = null;
		}
		
		fuzzyPrescriptions.close();
	}

}
