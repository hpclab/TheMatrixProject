package org.erasmusmc.jerboa.postProcessing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.ReadCSVFileWithHeader;
import org.erasmusmc.utilities.Row;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class DrugUtilizationPopulationSplitter {

	private static AggregationKey fullKey = getFullKey();
	public static String agePrefix = "age ";
	public static boolean addDerivedMetrics = true;
	
	public static void main(String[] args) {

		DrugUtilizationPopulationSplitter splitter = new DrugUtilizationPopulationSplitter();
		//splitter.process("X:/SOS - IPCI 2 -complete/DUS/DrugUtilizationPopulation180Duration0.txt", "X:/SOS - IPCI 2 -complete/DUS/DUSpopulation/");
		/*
		splitter.process("X:/SOS - IPCI 2/DrugUtilizationPopulation365Duration0.txt", "X:/SOS - IPCI 2/DUS");
		splitter.process("X:/SOS - IPCI 2/DrugUtilizationPopulation180Duration1.txt", "X:/SOS - IPCI 2/DUS");
		splitter.process("X:/SOS - IPCI 2/DrugUtilizationPopulation365Duration1.txt", "X:/SOS - IPCI 2/DUS");
		splitter.process("X:/SOS - IPCI 2/DrugUtilizationPopulation180Duration2.txt", "X:/SOS - IPCI 2/DUS");
		splitter.process("X:/SOS - IPCI 2/DrugUtilizationPopulation365Duration2.txt", "X:/SOS - IPCI 2/DUS");
		splitter.process("X:/SOS - IPCI 2/DrugUtilizationPopulation180Duration3.txt", "X:/SOS - IPCI 2/DUS");
		splitter.process("X:/SOS - IPCI 2/DrugUtilizationPopulation365Duration3.txt", "X:/SOS - IPCI 2/DUS");
		*/
		
		splitter.process("x:/sos/DrugUtilizationPopulation180.txt", "x:/sos/DUS");
		splitter.process("x:/sos/DrugUtilizationPopulation365.txt", "x:/sos/DUS");
	}

	private static AggregationKey getFullKey() {
		AggregationKey key = new AggregationKey();
		key.add("AgeRange");
		key.add("Gender");
		key.add("Dose");
		key.add("Year");
		key.add("Month");
		return key;
	}

	public void process(String sourceFile, String targetFolder) {
		List<AggregationKey> aggregationKeys = identifyAggregationKeys(sourceFile);
		String baseFilename = new File(sourceFile).getName().replace(".txt", "");
		for (AggregationKey key : aggregationKeys){
			String targetFile = targetFolder + "/" + baseFilename + "_" + StringUtilities.join(key, "_")+".csv";
			createTable(sourceFile, targetFile, key);
		}
	}

	private void createTable(String sourceFile, String targetFile, AggregationKey keyTemplate) {
		System.out.println("Creating file " + targetFile);
		Map<AggregationKey, Data> backgroundData = getBackgroundData(sourceFile, keyTemplate);
		int atcLevel = Integer.parseInt(keyTemplate.get(0).substring(3));
		WriteCSVFile out = new WriteCSVFile(targetFile);
		out.write(generateHeader(keyTemplate));
		for (Row row : new ReadCSVFileWithHeader(sourceFile)){
			String atc = row.get("ATC");
			if (atc.length() == atcLevel){
				List<String> cells = new ArrayList<String>();
				cells.add(atc);
				AggregationKey key = new AggregationKey();
				boolean isCorrectAggregationLevel = true;
				for (String field : fullKey){
					String value = row.get(field);
					boolean isAll = value.equals("ALL");
					boolean inKeyTemplate = keyTemplate.contains(field);
					if ((isAll && inKeyTemplate) || (!isAll && !inKeyTemplate)){
						isCorrectAggregationLevel = false;
						break;
					}
					key.add(value);
					if (inKeyTemplate)
						if (field.equals("AgeRange"))
					    cells.add(agePrefix + value);	
						else
							cells.add(value);
				}
				if (isCorrectAggregationLevel){
					Data data = backgroundData.get(key);
					cells.add(row.get("PersonDays"));
					cells.add(data.personDays);
					long uncensoredBackgroundDays = Long.parseLong(data.personDays) - Long.parseLong(row.get("CensoredDays"));
					cells.add(Long.toString(uncensoredBackgroundDays));
					cells.add(row.get("ExposedIndividuals"));
					cells.add(data.exposedIndividuals);
					cells.add(row.get("NewUsers"));
					cells.add(row.get("PrescriptionStarts"));
					if (addDerivedMetrics){
						int exposedIndividuals = Integer.parseInt(row.get("ExposedIndividuals"));
						long personDays = Long.parseLong(row.get("PersonDays"));
						int exposedIndividuals_background = Integer.parseInt(data.exposedIndividuals);
						long personDays_background = Long.parseLong(data.personDays);
						int newUsers = Integer.parseInt(row.get("NewUsers"));
						int prescriptionStarts = Integer.parseInt(row.get("PrescriptionStarts"));
						cells.add(Double.toString(exposedIndividuals * 1000l / (personDays_background/365.25d)));
						cells.add(Double.toString(newUsers * 1000l / (uncensoredBackgroundDays/365.25d)));
						cells.add(Double.toString(personDays_background/(double)exposedIndividuals_background));
						if (prescriptionStarts == 0)
							cells.add("");
						else
						  cells.add(Double.toString(personDays/(double)prescriptionStarts));
						if (exposedIndividuals == 0)
							cells.add("");
						else
						  cells.add(Double.toString(personDays/(double)exposedIndividuals));
						cells.add(Double.toString(prescriptionStarts * 1000l / (personDays_background/365.25d)));
						cells.add(Double.toString(personDays * 1000l / (double)personDays_background));
					}
					out.write(cells);
				}
			}
		}
		out.close();
		FileSorter.sort(targetFile, (String[])keyTemplate.toArray(new String[keyTemplate.size()]));
	}

	private List<String> generateHeader(AggregationKey key) {
		List<String> header = new ArrayList<String>();
		header.addAll(key);
		header.add("PersonDays");
		header.add("PersonDays_Background");
		header.add("PersonDays_Background_Uncensored");
		header.add("ExposedIndividuals");
		header.add("ExposedIndividuals_Background");
		header.add("NewUsers");
		header.add("PrescriptionStarts");
		if (addDerivedMetrics){
			header.add("Prevalence (per 1000 PT years)");
			header.add("Incidence (per 1000 PT years)");
			header.add("Mean duration follow-up");
			header.add("Mean duration per prescription");
			header.add("Mean personDays per exposed individual");
			header.add("Rx/PT (*1000)");
			header.add("Exposure Rate (*1000)");
		}
		return header;
	}

	private Map<AggregationKey, Data> getBackgroundData(String sourceFile, AggregationKey keyTemplate) {
		Map<AggregationKey, Data> backgroundData = new HashMap<AggregationKey, Data>();
		for (Row row : new ReadCSVFileWithHeader(sourceFile)){
			String atc = row.get("ATC");
			if (atc.length() == 0){
				boolean isCorrectAggregationLevel = true;
				AggregationKey key = new AggregationKey();
				for (String field : fullKey){
					String value = row.get(field);
					boolean isAll = value.equals("ALL");
					boolean inKeyTemplate = keyTemplate.contains(field);
					if ((isAll && inKeyTemplate) || (!isAll && !inKeyTemplate)){
						isCorrectAggregationLevel = false;
						break;
					}
					key.add(value);
				}
				if (isCorrectAggregationLevel){
					Data data = new Data();
					data.exposedIndividuals = row.get("ExposedIndividuals");
					data.personDays = row.get("PersonDays");
					backgroundData.put(key, data);
				}
			}
		}
		return backgroundData;
	}

	private List<AggregationKey> identifyAggregationKeys(String sourceFile) {
		Set<AggregationKey> keys = new HashSet<AggregationKey>();
		for (Row row : new ReadCSVFileWithHeader(sourceFile)){
			String atc = row.get("ATC");
			if (atc.length() != 0){
				AggregationKey key = new AggregationKey();
				key.add("ATC"+atc.length());
				if (!row.get("AgeRange").equals("ALL"))
					key.add("AgeRange");
				if (!row.get("Gender").equals("ALL"))
					key.add("Gender");
				if (!row.get("Dose").equals("ALL"))
					key.add("Dose");
				if (!row.get("Year").equals("ALL"))
					key.add("Year");
				if (!row.get("Month").equals("ALL"))
					key.add("Month");
				keys.add(key);
			}
		}

		return new ArrayList<AggregationKey>(keys);
	}

	private static class AggregationKey extends ArrayList<String>{
		private static final long serialVersionUID = -213469061680946767L;	
	}

	private class Data {
		public String personDays;
		public String exposedIndividuals;
	}


}
