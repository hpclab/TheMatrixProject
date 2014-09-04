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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.collections.SparseHighDimensionalMatrix;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.MergedDataFileReader;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.jerboa.userInterface.VirtualTable;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Calculates drug utilization such as indidence and prevalence at the population level. Multiple aggregation levels can be specified. 
 * @author schuemie
 *
 */
public class DrugUtilizationPopulationModule extends JerboaModule {

	private static final long serialVersionUID = -1133812292742544228L;
	
	public JerboaModule mergedData;
	public JerboaModule prescriptions;

  /**
   * The minimum number of subjects in a row of the resulting aggregated table. Rows with fewer subjects
   * are deleted, and deletions will be summarized in the last line of the table.<BR>
   * default = 0
   */
  public int minSubjectsPerRow = 0;
	
	/**
	 * The period (in days) before the first prescription where the drug is not used.<BR>
	 * default = 180
	 */
	public int naivePeriod = 180;

	/**
	 * Specifies the levels of aggregation. For instance: 'ATC;AgeRange;Gender' will generate a table aggregated by ATC, ageRange and gender.<BR>
	 * Each key should be specified using semicolon as seperator, using a selection of these fields:<BR>
	 * ATC<BR>
	 * Dose<BR>
	 * AgeRange<BR>
	 * Gender<BR>
	 * Year<BR>
	 * Month<BR>
	 */
	public List<String> aggregationKey = new ArrayList<String>();

	private SparseHighDimensionalMatrix<Data> dataMatrix;
	private static final int ALL = -1;
	private static final int KEY_ATC = 0;
	private static final int KEY_DOSE = 1;
	private static final int KEY_YEAR = 2;
	private static final int KEY_MONTH = 3;
	private static final int KEY_GENDER = 4;
	private static final int KEY_AGERANGE = 5;
	private static final int KEYSIZE = 6;
	private List<KeyTemplate> keyTemplates;
	private Map<String, Integer> atc2Index;
	private Map<String, Integer> dose2Index;
	private Map<String, Integer> ageRange2Index;
	private Prescription currentPrescription;
	private Iterator<Prescription> prescriptionIterator;
	private Set<Integer> atcLevels;
	private Set<Boolean> withDoses;
	private static final ATCCode nullATCCode = new ATCCode("");

	
	public static void main(String[] args) {
		//String folder = "D:\\Documents\\IPCI\\SOS\\Test Data\\";
		//String folder = "x:/SOS - IPCI 2 - small/";
		//String folder = "x:/SOS/";
		String folder = "C:/home/data/Simulated/DUS/";
		
		DrugUtilizationPopulationModule module = new DrugUtilizationPopulationModule();
		
		//FileSorter.sort(folder+"Mergedata.txt", new String[]{"PatientID", "Date"});
		//FileSorter.sort(folder+"Prescriptions.txt", new String[]{"PatientID", "Date", "ATC"});
		/*
		module.aggregationKey.add("ATC7");
		module.aggregationKey.add("ATC7;AgeRange");
		module.aggregationKey.add("ATC7;AgeRange;Year");
		module.aggregationKey.add("ATC7;AgeRange;Gender");
		module.aggregationKey.add("ATC7;AgeRange;Gender;Year");
		module.aggregationKey.add("ATC7;Gender");
		module.aggregationKey.add("ATC7;Gender;Year");
		module.aggregationKey.add("ATC7;Gender;Year;Month");
		module.aggregationKey.add("ATC7;Year");
		module.aggregationKey.add("ATC7;Year;Month");

		module.aggregationKey.add("ATC5");
		module.aggregationKey.add("ATC5;AgeRange");
		module.aggregationKey.add("ATC5;AgeRange;Year");
		module.aggregationKey.add("ATC5;AgeRange;Gender");
		module.aggregationKey.add("ATC5;AgeRange;Gender;Year");
		module.aggregationKey.add("ATC5;Gender");
		module.aggregationKey.add("ATC5;Gender;Year");
		module.aggregationKey.add("ATC5;Gender;Year;Month");
		module.aggregationKey.add("ATC5;Year");
		module.aggregationKey.add("ATC5;Year;Month");

		module.aggregationKey.add("ATC4");
		module.aggregationKey.add("ATC4;AgeRange");
		module.aggregationKey.add("ATC4;AgeRange;Year");
		module.aggregationKey.add("ATC4;AgeRange;Gender");
		module.aggregationKey.add("ATC4;AgeRange;Gender;Year");
		module.aggregationKey.add("ATC4;Gender");
		module.aggregationKey.add("ATC4;Gender;Year");
		module.aggregationKey.add("ATC4;Gender;Year;Month");
		module.aggregationKey.add("ATC4;Year");
		module.aggregationKey.add("ATC4;Year;Month");
		*/
		
		module.aggregationKey.add("ATC7;Year");
		module.aggregationKey.add("ATC7;AgeRange");
		module.aggregationKey.add("ATC7;AgeRange;Year");
		module.aggregationKey.add("ATC4;Year");
		module.naivePeriod = 365;
		//MergeDataModule mdm = initMDM(folder+"Combineconcomitant0.txt", folder+"Cohortentrydate.txt");
		//long start = System.currentTimeMillis();
		
		//module.process(mdm.getIterator(), folder+"Prescriptionsfiltered0.txt", folder+"DUSTest.csv");
		//System.out.println((System.currentTimeMillis()-start));
		module.process(folder+"Mergedata.txt", folder+"Prescriptionsfiltered.txt", folder+"DUSTest.csv");
	}

	protected void runModule(String outputFilename){
		FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID", "Date", "ATC"});
		if (mergedData.isVirtual()){
			@SuppressWarnings("unchecked")
			VirtualTable<MergedData> virtualTable = (VirtualTable<MergedData>) mergedData;
			process(virtualTable.getIterator(), prescriptions.getResultFilename(), outputFilename);
		} else {
			FileSorter.sort(mergedData.getResultFilename(), new String[]{"PatientID", "Date"});
			process(mergedData.getResultFilename(), prescriptions.getResultFilename(), outputFilename);
		}
	}

	public void process(String sourceMergedDate, String sourcePrescriptions, String target) {
		Iterator<MergedData> mergedDateIterator = new MergedDataFileReader(sourceMergedDate).iterator();
		process(mergedDateIterator, sourcePrescriptions, target);
	}
	
	public void process(Iterator<MergedData> mergedDateIterator, String sourcePrescriptions, String target) {
		dataMatrix = new SparseHighDimensionalMatrix<Data>(KEYSIZE);
		atc2Index = new HashMap<String, Integer>();
		dose2Index = new HashMap<String, Integer>();
		ageRange2Index = new HashMap<String, Integer>();
		parseKeyTemplates();
		
		prescriptionIterator = new PrescriptionFileReader(sourcePrescriptions).iterator();
		currentPrescription = prescriptionIterator.next();
		
		List<MergedData> patientData = new ArrayList<MergedData>();
		String lastPatientID = "";
		while (mergedDateIterator.hasNext()){
	    MergedData mergedData = mergedDateIterator.next();
		  if (!mergedData.patientID.equals(lastPatientID)){
		  	processPatient(patientData);
		  	patientData.clear();
		  	lastPatientID = mergedData.patientID;
				ProgressHandler.reportProgress();
		  }
		  patientData.add(mergedData);
		}
  	processPatient(patientData);
  	
    writeToFile(target);
		
		//Dereference data objects:
		dataMatrix = null;
		keyTemplates = null;
		atc2Index = null;
		dose2Index = null;
		ageRange2Index = null;
		currentPrescription = null;
		prescriptionIterator = null;
	}
	
	private void processPatient(List<MergedData> patientData){		
		SparseHighDimensionalMatrix<Boolean> keysSeenByPatient = new SparseHighDimensionalMatrix<Boolean>(KEYSIZE);
		Map<ATCCode, Long> lastUses  = new HashMap<ATCCode, Long>();
		Set<ATCCode> previousATCCodes = new HashSet<Prescription.ATCCode>();
		for (MergedData mergedData : patientData){
			mergedData.atcCodes.add(nullATCCode); //Add empty atc code for background statistics (non-exposure)
			CountingSet<ATCCode> startCounts = countPrescriptionStarts(mergedData);
			Set<List<Integer>> keysSeen = new HashSet<List<Integer>>();
			Set<String> atcsSeen = new HashSet<String>();
			for (ATCCode atcCode : mergedData.atcCodes){
				if (!mergedData.outsideCohortTime){
					int startsATCDose = startCounts.getCount(atcCode);
					int startsATC = sumAccrossDose(startCounts, atcCode.atc);
					long personDays = mergedData.duration;
					boolean atcNotSeen = atcsSeen.add(atcCode.atc); //Could be multiple prescriptions with different dose but same ATC at the same time
					for (KeyTemplate keyTemplate : keyTemplates){
						int[] key = convertToKey(mergedData, atcCode, keyTemplate);
						Data data = dataMatrix.get(key);
						if (data == null){
							data = new Data();
							dataMatrix.set(key, data);
						}
						data.starts += keyTemplate.dose?startsATCDose:(atcNotSeen?startsATC:0);
						
						if (keysSeen.add(convertToList(key))){
							boolean isNewSubject = (keysSeenByPatient.get(key) == null);
							if (isNewSubject)
								keysSeenByPatient.set(key, true);
							Long lastUsed = lastUses.get(convertToKeyATCCode(atcCode, keyTemplate));
							
							data.exposedIndividuals += (isNewSubject?1:0);
							data.newUsers += (lastUsed == null || mergedData.start - lastUsed > naivePeriod)?1:0;
							data.personDays += personDays;
						}
					}
				}
			}
			
			// Store last drug use dates:
			for (ATCCode atcCode : mergedData.atcCodes) 
				if (atcCode != nullATCCode){
					for (KeyTemplate keyTemplate : keyTemplates)
						lastUses.put(convertToKeyATCCode(atcCode, keyTemplate), mergedData.getEnd());
					previousATCCodes.add(atcCode);
				}
			
			//Store censored time:
			keysSeen = new HashSet<List<Integer>>();
			if (!mergedData.outsideCohortTime)
				for (ATCCode atcCode : previousATCCodes)
					for (KeyTemplate keyTemplate : keyTemplates){
						long lastUsed = lastUses.get(convertToKeyATCCode(atcCode, keyTemplate));
						long endOfCensorPeriod = lastUsed + naivePeriod;
						if (endOfCensorPeriod > mergedData.start){
							int[] key = convertToKey(mergedData, atcCode, keyTemplate);
							if (keysSeen.add(convertToList(key))){
								Data data = dataMatrix.get(key);
								if (data == null){
									data = new Data();
									dataMatrix.set(key, data);
								}
								data.censoredDays += Math.min(mergedData.getEnd(), endOfCensorPeriod) - mergedData.start;
							}
						}
					}
		}
	}
	
	private ATCCode convertToKeyATCCode(ATCCode atcCode, KeyTemplate keyTemplate){
		return new ATCCode(atcCode.atc.substring(0, Math.min(keyTemplate.atc, atcCode.atc.length()))+":"+(keyTemplate.dose?atcCode.dose:""));
	}

	private List<Integer> convertToList(int[] key) {
		List<Integer> list = new ArrayList<Integer>(key.length);
		for (int keyPart : key)
			list.add(keyPart);
		return list;
	}

	private int sumAccrossDose(CountingSet<ATCCode> startCounts, String atc) {
		int sum = 0;
		for (ATCCode atcCode : startCounts)
			if (atcCode.atc.equals(atc))
				sum += startCounts.getCount(atcCode);
		return sum;
	}

	private int[] convertToKey(MergedData mergedData, ATCCode atcCode, KeyTemplate keyTemplate) {
		int[] key = new int[KEYSIZE];
		if (keyTemplate.atc != -1)
			key[KEY_ATC] = stringToIndex(atcCode.atc.substring(0, Math.min(keyTemplate.atc, atcCode.atc.length())), atc2Index);
		else
			key[KEY_ATC] = ALL;
		if (keyTemplate.dose)
			key[KEY_DOSE] = stringToIndex(atcCode.dose, dose2Index);
		else
			key[KEY_DOSE] = ALL;
		if (keyTemplate.year){
			if (mergedData.year == -1)
				throw new RuntimeException("Merged data file not split by year!");
			key[KEY_YEAR] = mergedData.year;
		} else
			key[KEY_YEAR] = ALL;
		if (keyTemplate.month){
			if (mergedData.month == -1)
				throw new RuntimeException("Merged data file not split by month!");
			key[KEY_MONTH] = mergedData.month;
		} else
			key[KEY_MONTH] = ALL;
		if (keyTemplate.gender)
			key[KEY_GENDER] = mergedData.gender;
		else
			key[KEY_GENDER] = ALL;
		if (keyTemplate.ageRange)
			key[KEY_AGERANGE] = stringToIndex(mergedData.ageRange, ageRange2Index);
		else
			key[KEY_AGERANGE] = ALL;
		return key;
	}

	private int stringToIndex(String string, Map<String, Integer> map) {
		Integer result = map.get(string);
		if (result == null){
			result = map.size();
			map.put(string, result);
		}
		return result;
	}

	private String indexToString(int index, Map<String, Integer> map){
		if (index == ALL)
			return "ALL";
		for (Map.Entry<String, Integer> entry : map.entrySet()){
			if (entry.getValue().equals(index))
				if (entry.getKey() == null)
					return "";
				else
					return entry.getKey();
		}
		throw new RuntimeException("Value not found in map");
	}

	private void parseKeyTemplates() {
		keyTemplates = new ArrayList<KeyTemplate>();
		atcLevels = new HashSet<Integer>();
		withDoses = new HashSet<Boolean>();
		for (String keyString : aggregationKey){
			KeyTemplate keyTemplate = new KeyTemplate();
			for (String part : keyString.split(";")){
				if (part.toLowerCase().equals("atc1"))
					keyTemplate.atc = 1;
				else if (part.toLowerCase().equals("atc4"))
					keyTemplate.atc = 4;
				else if (part.toLowerCase().equals("atc5"))
					keyTemplate.atc = 5;
				else if (part.toLowerCase().equals("atc7") || part.toLowerCase().equals("atc"))
					keyTemplate.atc = 7;
				else if (part.toLowerCase().equals("dose"))
					keyTemplate.dose = true;
				else if (part.toLowerCase().equals("year"))
					keyTemplate.year = true;
				else if (part.toLowerCase().equals("month"))
					keyTemplate.month = true;
				else if (part.toLowerCase().equals("gender"))
					keyTemplate.gender = true;
				else if (part.toLowerCase().equals("agerange"))
					keyTemplate.ageRange = true;
			}
			keyTemplates.add(keyTemplate);
			atcLevels.add(keyTemplate.atc);
			withDoses.add(keyTemplate.dose);
		}
		if (keyTemplates.size() == 0) {
			keyTemplates.add(new KeyTemplate());
		}
	}

	private CountingSet<ATCCode> countPrescriptionStarts(MergedData mergedData) {
		CountingSet<ATCCode> startCounts = new CountingSet<ATCCode>();
		// Move to correct patient
		while (currentPrescription != null && currentPrescription.patientID.compareTo(mergedData.patientID) < 0) {
			if (prescriptionIterator.hasNext()) 
				currentPrescription = prescriptionIterator.next();
			else 
				currentPrescription = null;
		}
		// Move to correct prescription
		while (currentPrescription != null && currentPrescription.patientID.equals(mergedData.patientID) && currentPrescription.start < mergedData.start) {
			if (prescriptionIterator.hasNext()) 
				currentPrescription = prescriptionIterator.next();
			else 
				currentPrescription = null;
		}
		while ((currentPrescription != null) && currentPrescription.patientID.equals(mergedData.patientID) && (currentPrescription.start < mergedData.getEnd())) {
			startCounts.addAll(currentPrescription.atcCodes);
			if (prescriptionIterator.hasNext()) 
				currentPrescription = prescriptionIterator.next();
			else 
				currentPrescription = null;
		}

		return startCounts;
	}

	private void writeToFile(String target) {
		// Open the output file
		WriteCSVFile targetFile = new WriteCSVFile(target);

		// Write the header of the output file
		List<String> headers = new ArrayList<String>();
		headers.add("AgeRange");
		headers.add("Gender");
		headers.add("ATC");
		headers.add("Dose");
		headers.add("Year");
		headers.add("Month");
		headers.add("PersonDays");
		headers.add("CensoredDays");
		headers.add("ExposedIndividuals");
		headers.add("NewUsers");
		headers.add("PrescriptionStarts");

		targetFile.write(headers);

		// Write the data to the output file
		int deletedRows = 0;
		long deletedTime = 0;
		Iterator<SparseHighDimensionalMatrix.Entry<Data>> iterator = dataMatrix.iterator();
		while(iterator.hasNext()) {
			SparseHighDimensionalMatrix.Entry<Data> entry = iterator.next();
			int[] key = entry.indices;
			Data data = entry.value;
			if (minSubjectsPerRow == 0 || (data.exposedIndividuals >= minSubjectsPerRow && data.newUsers >= minSubjectsPerRow && data.personDays >= minSubjectsPerRow && data.starts >= minSubjectsPerRow)){
				List<String> line = new ArrayList<String>();
				line.add(indexToString(key[KEY_AGERANGE], ageRange2Index));
				line.add(genderToString(key[KEY_GENDER]));
				line.add(indexToString(key[KEY_ATC], atc2Index));
				line.add(indexToString(key[KEY_DOSE], dose2Index));
				line.add(numberToString(key[KEY_YEAR]));
				line.add(numberToString(key[KEY_MONTH]));

				line.add(Long.toString(data.personDays));
				line.add(Long.toString(data.censoredDays));
				line.add(Integer.toString(data.exposedIndividuals));
				line.add(Integer.toString(data.newUsers));
				line.add(Integer.toString(data.starts));

				targetFile.write(line);
			} else {
				deletedRows++;
				deletedTime += data.personDays;
			}
		}
		// Close the output file
		targetFile.close();
		if (deletedRows != 0)
		  System.out.println("Deleted " + deletedRows + " rows with too few subjects, containing " + deletedTime + " days of patient time");
	}

	private String numberToString(int number) {
		if (number == ALL)
			return "ALL";
		return Integer.toString(number);
	}

	private String genderToString(int number) {
		if (number == ALL)
			return "ALL";
		return (number == Patient.MALE?"M":"F");
	}

	private class Data {
		long personDays = 0;
		int exposedIndividuals = 0;
		int newUsers = 0;
		int starts = 0;
		long censoredDays = 0;
	}

	private class KeyTemplate {
		int atc = -1;
		boolean dose = false;
		boolean year = false;
		boolean month = false;
		boolean gender = false;
		boolean ageRange = false;
	}

}