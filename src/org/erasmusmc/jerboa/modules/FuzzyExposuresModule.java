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

import org.erasmusmc.jerboa.dataClasses.Exposure;
import org.erasmusmc.jerboa.dataClasses.ExposureFileReader;
import org.erasmusmc.jerboa.dataClasses.ExposureFileWriter;
import org.erasmusmc.jerboa.dataClasses.PatientFuzzyOffset;
import org.erasmusmc.jerboa.dataClasses.PatientFuzzyOffsetFileReader;
import org.erasmusmc.utilities.FileSorter;

public class FuzzyExposuresModule extends JerboaModule {
	
	private static final long serialVersionUID = -7975356797283647528L;
	
	public JerboaModule exposures;
	public JerboaModule fuzzydata;
	
	public static void main(String[] args) {
		FuzzyExposuresModule module = new FuzzyExposuresModule();
		String path = "D:\\Documents\\IPCI\\Vaccinaties\\Vaesco\\Jerboa Test\\Jerboa\\SCCS\\";
		FileSorter.sort(path + "Exposures.txt", new String[]{"PatientID", "Date", "Type"});
		FileSorter.sort(path + "FuzzyOffSets.txt", new String[]{"PatientID"});
		module.process(path + "Exposures.txt", path + "FuzzyOffSets.txt", path + "FuzzyExposures.txt");
	}
	
	protected void runModule(String outputFilename) {
		FileSorter.sort(exposures.getResultFilename(), new String[]{"PatientID", "Date", "Type"});
		FileSorter.sort(fuzzydata.getResultFilename(), new String[]{"PatientID"});
		process(exposures.getResultFilename(), fuzzydata.getResultFilename(), outputFilename);
	}

	public void process(String sourceExposures, String sourceFuzzyData, String targetFuzzyExposures) {
		Exposure currentExposure;
		PatientFuzzyOffset currentFuzzyData;		
		long fuzzyOffset;
		
		ExposureFileReader exposures = new ExposureFileReader(sourceExposures);
		Iterator<Exposure> exposureIterator = exposures.iterator();
		
		PatientFuzzyOffsetFileReader fuzzyData = new PatientFuzzyOffsetFileReader(sourceFuzzyData);
		Iterator<PatientFuzzyOffset> fuzzyDataIterator = fuzzyData.iterator();

		ExposureFileWriter fuzzyExposures = new ExposureFileWriter(targetFuzzyExposures);
		
		if (exposureIterator.hasNext()) 
			currentExposure = exposureIterator.next();
		else
			currentExposure = null;
		
		if (fuzzyDataIterator.hasNext()) 
			currentFuzzyData = fuzzyDataIterator.next();
		else
			currentFuzzyData = null;
		
		while (currentExposure != null) {
			fuzzyOffset = 0;
			while ((currentFuzzyData != null) && (currentFuzzyData.patientID.compareTo(currentExposure.patientID) < 0)) {
				if (fuzzyDataIterator.hasNext()) 
					currentFuzzyData = fuzzyDataIterator.next();
				else
					currentFuzzyData = null;
			}
			if ((currentFuzzyData != null) && (currentFuzzyData.patientID.compareTo(currentExposure.patientID) == 0)) {
				fuzzyOffset = currentFuzzyData.fuzzyOffset;
			}
			
			currentExposure.start = currentExposure.start + fuzzyOffset;
			
			fuzzyExposures.write(currentExposure);
			//fuzzyExposures.flush();
			
			if (exposureIterator.hasNext()) 
				currentExposure = exposureIterator.next();
			else
				currentExposure = null;
		}
		
		fuzzyExposures.close();
	}

}
