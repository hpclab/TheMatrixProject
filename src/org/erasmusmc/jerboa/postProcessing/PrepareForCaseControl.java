package org.erasmusmc.jerboa.postProcessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.collections.OneToManySet;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.ReadCSVFileWithHeader;
import org.erasmusmc.utilities.Row;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class  PrepareForCaseControl {

	public int maxDaysSinceUse = 0;

	public String exposureColumn = "current";//_DaysSinceUse";

	public int minEventsDuringExposure = 1;

	public String limitSetFile = null;

	public boolean perEventAnalysis = false;

	public boolean includeAge = false;

	public boolean includeMonth = false;

	public boolean includeYear = false;

	public boolean includeGender = false;

	public boolean includeChronicDiseaseScore = false;

	public boolean includeDrugCount = true;

	public static int batchSize = 1000;

	/**
	 * If true, cases and controls not exposed to the same class are removed
	 */
	public boolean matchOnSubATC = false;
	
	public int subATCLevel = 4;

	public static void main(String[] args){
		PrepareForCaseControl prepareForCaseControl = new PrepareForCaseControl();
		String folder = "x:";
		prepareForCaseControl.limitSetFile = "C:/home/schuemie/Research/SignalGenerationCompare/Pharmo - Gold/WP2ValidationSetUpdated.csv";

		//Case control
		prepareForCaseControl.maxDaysSinceUse = -1;
		prepareForCaseControl.perEventAnalysis = true;
		prepareForCaseControl.includeAge = true;
		prepareForCaseControl.includeGender = true;
		prepareForCaseControl.process(folder+"CaseControl.txt", folder+"CCforR", folder+"CCAnalysisdata.txt");

		//Case crossover
		//prepareForCaseControl.exposureColumn = "current";
		//prepareForCaseControl.maxDaysSinceUse = -1;
		//prepareForCaseControl.includeChronicDiseaseScore = false;
		//prepareForCaseControl.includeDrugCount = false;
		//prepareForCaseControl.process(folder+"CaseCrossover.txt", folder+"CCRforR", folder+"CCRAnalysisdata.txt");
	}


	public void process(String source, String targetPatientDataFolder, String targetAnalysisData){
		FileSorter.sort(source, "CaseSetID");
		Set<String> eventAtcs = findEventATCCombinations(source);
		if (perEventAnalysis){
			Map<String, String> event2Filename = generatePerEventFilenames(eventAtcs,targetPatientDataFolder);
			saveAnalysisData(event2Filename, targetAnalysisData);
			generatePerEventPatientDataFiles(source, event2Filename, eventAtcs);
		} else {
			Map<String, String> eventATC2Filename = generateFilenames(eventAtcs,targetPatientDataFolder);
			saveAnalysisData(eventATC2Filename,targetAnalysisData);
			generatePatientDataFiles(source, eventATC2Filename);
		}
	}


	private void generatePerEventPatientDataFiles(String source,Map<String, String> event2Filename, Set<String> eventAtcs) {
		for (String event : event2Filename.keySet()){

			Set<String> atcs = new HashSet<String>();
			for (String eventATC : eventAtcs){
				String[] parts = eventATC.split("\t");
				if (parts[0].equals(event))
					atcs.add(parts[1]);
			}
			System.out.println("Creating ouput for " + event + "(" + atcs.size() + " drugs)");
			processEvent(source, event2Filename.get(event), event, atcs);
		}
	}


	private void processEvent(String source, String target, String event,	Set<String> atcs) {
		WriteCSVFile out = new WriteCSVFile(target);
		List<String> sortedATCs = new ArrayList<String>(atcs);
		Collections.sort(sortedATCs);
		out.write(generateHeader(sortedATCs));
		for (Row data : new ReadCSVFileWithHeader(source))
			if (data.get("EventType").equals(event)){
				List<String> cells = new ArrayList<String>();
				cells.add(data.get("CaseSetID"));
				if (includeAge)
					cells.add(data.get("AgeRange"));
				if (includeMonth)
					cells.add(data.get("Month"));
				if (includeYear)
					cells.add(data.get("Year"));
				if (includeGender)
					cells.add(data.get("Gender"));
				if (includeChronicDiseaseScore)
					cells.add(data.get("ChronicDiseaseScore"));
				if (includeDrugCount)
					cells.add(data.get("DrugCount"));
				List<String> exposedAtcs = extractATCs(data.get(exposureColumn));
				for (String atc: sortedATCs)
					cells.add(exposedAtcs.contains(atc)?"1":"0");
				cells.add(data.get("IsCase"));
				out.write(cells);
			}
		out.close();
	}


	private void generatePatientDataFiles(String source, Map<String, String> eventATC2Filename) {
		int offset = 0;
		List<String> list = new ArrayList<String>(eventATC2Filename.keySet());
		List<String> header = generateHeader(null);
		while (offset < list.size()){
			List<String> batch = list.subList(offset, Math.min(list.size(), offset+batchSize));
			offset += batch.size();
			OneToManySet<String, String> event2atcs = new OneToManySet<String, String>();
			Map<String,WriteCSVFile> eventATC2File = new HashMap<String, WriteCSVFile>();
			for (String eventATC : batch){
				String[] parts = eventATC.split("\t");	  
				event2atcs.put(parts[0],parts[1]);
				WriteCSVFile out = new WriteCSVFile(eventATC2Filename.get(eventATC));
				out.write(header);
				eventATC2File.put(eventATC, out);
			}
			String oldCaseSetID = "";
			List<Row> caseSetData = new ArrayList<Row>();
			System.out.println("Processing " + batch.size() + " signals");
			for (Row row : new ReadCSVFileWithHeader(source)){
				if (!row.get("CaseSetID").equals(oldCaseSetID)){
					processCaseSet(caseSetData,event2atcs,eventATC2File);
					caseSetData.clear();
					oldCaseSetID = row.get("CaseSetID");
				}
				caseSetData.add(row);
			}
			processCaseSet(caseSetData,event2atcs,eventATC2File);

			for (WriteCSVFile out : eventATC2File.values())
				out.close();
		}
		StringUtilities.outputWithTime("Done");
	}

	private void processCaseSet(List<Row> caseSetData, OneToManySet<String, String> event2atcs,Map<String, WriteCSVFile> eventATC2File) {
		if (caseSetData.size() == 0)
			return;
		String eventType = caseSetData.get(0).get("EventType");
		for (String atc : event2atcs.get(eventType)){
			WriteCSVFile out = eventATC2File.get(eventType+"\t"+atc);
			processCaseSet(out, caseSetData, eventType, atc);
		}
	}

	private void processCaseSet(WriteCSVFile out, List<Row> caseSetData,	String eventType, String atc) {
		for (Row data : caseSetData){
			if (!matchOnSubATC || matchesATC4(data.get(exposureColumn),atc)){
				List<String> cells = new ArrayList<String>();
				cells.add(data.get("CaseSetID"));
				if (includeAge)
					cells.add(data.get("AgeRange"));
				if (includeMonth)
					cells.add(data.get("Month"));
				if (includeYear)
					cells.add(data.get("Year"));
				if (includeGender)
					cells.add(data.get("Gender"));
				if (includeChronicDiseaseScore)
					cells.add(data.get("ChronicDiseaseScore"));
				if (includeDrugCount)
					cells.add(data.get("DrugCount"));
				cells.add(contains(data.get(exposureColumn),atc)?"1":"0");
				cells.add(data.get("IsCase"));
				out.write(cells);
			}
		}
	}

	private boolean matchesATC4(String exposureVar, String atc) {
		String subATC = atc.substring(0,Math.min(subATCLevel, atc.length()));
		for (String exposedATC : extractATCs(exposureVar))
			if (exposedATC.length() >= subATCLevel && exposedATC.substring(0,subATCLevel).equals(subATC))
				return true;
		return false;
	}


	private boolean contains(String exposureVar, String atc){
		return extractATCs(exposureVar).contains(atc);
	}

	private void saveAnalysisData(Map<String, String> eventATC2Filename,	String target) {
		WriteCSVFile out = new WriteCSVFile(target);
		List<String> header = new ArrayList<String>();
		header.add("EventType");
		if (!perEventAnalysis)
			header.add("ATC");
		header.add("Filename");
		out.write(header);
		for (String eventATC :  eventATC2Filename.keySet()){
			List<String> cells = new ArrayList<String>();
			String event = eventATC.split("\t")[0];
			cells.add(event);
			if (!perEventAnalysis){
				String atc = eventATC.split("\t")[1];
				cells.add(atc);
			}

			cells.add(eventATC2Filename.get(eventATC));
			out.write(cells);
		}
		out.close();
	}

	private Map<String, String> generatePerEventFilenames(Set<String> eventAtcs,String targetFolder) {
		Map<String, String> event2Filename = new HashMap<String, String>();
		for (String eventATC : eventAtcs){
			String[] parts = eventATC.split("\t");	  
			if (!event2Filename.containsKey(parts[0]))
				event2Filename.put(parts[0],targetFolder+"/"+parts[0]+".txt");
		}
		return event2Filename;
	}

	private Map<String, String> generateFilenames(Set<String> eventAtcs,String targetFolder) {
		Map<String, String> eventATC2Filename = new HashMap<String, String>();
		for (String eventATC : eventAtcs){
			String[] parts = eventATC.split("\t");	  
			eventATC2Filename.put(eventATC,targetFolder+"/"+parts[0]+"-"+parts[1]+".txt");
		}
		return eventATC2Filename;
	}


	private Set<String> findEventATCCombinations(String source) {
		StringUtilities.outputWithTime("Starting");
		Map<String, CountingSet<String>> event2atcCounts = new HashMap<String, CountingSet<String>>();
		OneToManySet<String, String> event2atcControls = new OneToManySet<String, String>();
		for (Row row : new ReadCSVFileWithHeader(source)){
			String eventType = row.get("EventType");
			if (row.get("IsCase").equals("1") ){
				for (String atc : extractATCs(row.get(exposureColumn))){
					CountingSet<String> atcCounts = event2atcCounts.get(eventType);
					if (atcCounts == null){
						atcCounts = new CountingSet<String>();
						event2atcCounts.put(eventType, atcCounts);
					}
					atcCounts.add(atc);	
				}
			} else 
				for (String atc : extractATCs(row.get(exposureColumn)))
					event2atcControls.put(eventType, atc);

		}

		Set<String> limitSet = loadLimitSet();
		Set<String> event2atcs = new HashSet<String>();
		int countBefore = 0;
		int countAfter = 0;
		for (String event : event2atcCounts.keySet()){
			CountingSet<String> atcCounts = event2atcCounts.get(event);
			for (String atc : atcCounts){
				countBefore++;
				if (atcCounts.getCount(atc) >= minEventsDuringExposure && inLimitSet(limitSet, event,atc) && event2atcControls.get(event).contains(atc)){
					event2atcs.add(event+"\t"+atc);
					countAfter++;
				}
			}
		}
		System.out.println("Found " + countBefore + " drug-event combinations, " + countAfter + " after filtering");
		return event2atcs;
	}

	private boolean inLimitSet(Set<String> limitSet, String event, String atc) {
		if (limitSet == null)
			return true;
		if (perEventAnalysis)
			return limitSet.contains(event);
		else
			return limitSet.contains(event+"\t"+atc);
	}


	private List<String> extractATCs(String exposureVar){
		if (exposureVar.length() == 0)
			return Collections.emptyList();
		List<String> atcs = new ArrayList<String>();
		for (String drugDaysSinceUse : exposureVar.split("\\+")){
			String[] parts = drugDaysSinceUse.split(":");
			if (maxDaysSinceUse == -1 || Integer.parseInt(parts[1]) <= maxDaysSinceUse){
				String atc = parts[0];
				if (DeleteLinesWithIllegalATCs.isLegalATC(atc))
					atcs.add(atc);
			}
		}
		return atcs;
	}

	private Set<String> loadLimitSet() {
		if (limitSetFile == null)
			return null;
		else {
			Set<String> limitSet = new HashSet<String>();
			for (Row row : new ReadCSVFileWithHeader(limitSetFile)){
				if (perEventAnalysis)
					limitSet.add(row.get("EventType"));
				else
					limitSet.add(row.get("EventType")+"\t"+row.get("ATC"));
			}
			return limitSet;
		}
	}

	private List<String> generateHeader(List<String> atcs) {
		List<String> header = new ArrayList<String>();
		header.add("CaseSetID");
		if (includeAge)
			header.add("AgeRange");
		if (includeMonth)
			header.add("Month");
		if (includeYear)
			header.add("Year");
		if (includeGender)
			header.add("Gender");
		if (includeChronicDiseaseScore)
			header.add("ChronicDiseaseScore");
		if (includeDrugCount)
			header.add("DrugCount");
		if (atcs == null)
			header.add("Exposed");
		else
			header.addAll(atcs);
		header.add("Event");
		return header;
	}
}