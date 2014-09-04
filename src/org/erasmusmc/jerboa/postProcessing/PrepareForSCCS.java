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
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.utilities.CountingSetLong;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.ReadCSVFileWithHeader;
import org.erasmusmc.utilities.Row;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class PrepareForSCCS {

	public int minEventsDuringExposure = 2;
	
	public long minExposure = 1000;

	public String limitSetFile = null;

	public boolean perEventAnalysis = false;
	
	public boolean censorAfterEvent = false;
	
	public boolean splitByAge = true;
	
	public boolean splitByMonth = false;
	
	public boolean splitByYear = false;
	
	public boolean splitByGender = true;

	public static int batchSize = 1000;

	public static void main(String[] args) {
		//String folder = "/home/schuemie/Research/Jerboa/";
		String folder = "x:";
		PrepareForSCCS module = new PrepareForSCCS();
		//module.limitSetFile = "C:/home/schuemie/Research/SignalGenerationCompare/Pharmo/WP2ValidationSetLimitOnPharmo.csv";
		module.limitSetFile = "C:/home/schuemie/Research/SignalGenerationCompare/Pharmo - Gold/WP2ValidationSetUpdated.csv";
		module.process(folder+"SCCS.txt", folder + "SCCSforR", folder + "SCCSAnalysisData.txt");
	}

	public void process(String source, String targetPatientDataFolder, String targetAnalysisData){
		FileSorter.sort(source, "PatientID");
		Set<String> eventAtcs = findEventATCCombinations(source);
		if (perEventAnalysis){
			Map<String, String> event2Filename = generatePerEventFilenames(eventAtcs,targetPatientDataFolder);
			saveAnalysisData(event2Filename, targetAnalysisData);
			generatePerEventPatientDataFiles(source, event2Filename, eventAtcs);
		} else {
			Map<String, String> eventATC2Filename = generateFilenames(eventAtcs,targetPatientDataFolder);
			saveAnalysisData(eventATC2Filename, targetAnalysisData);
			generatePatientDataFiles(source, eventATC2Filename);
		}
	}

	private void generatePerEventPatientDataFiles(String source, Map<String, String> event2Filename, Set<String> eventAtcs) {
		for (String event : event2Filename.keySet()){
			System.out.println("Creating ouput for " + event);
			Set<String> atcs = new HashSet<String>();
			for (String eventATC : eventAtcs){
				String[] parts = eventATC.split("\t");
				if (parts[0].equals(event))
					atcs.add(parts[1]);
			}
			processEvent(source, event2Filename.get(event), event, atcs);
		}
	}

	private void processEvent(String source, String target, String event, Set<String> atcs) {
		WriteCSVFile out = new WriteCSVFile(target);
		List<String> header = new ArrayList<String>();
		header.add("PatientID");
		if (splitByAge)
		  header.add("AgeRange");
		if (splitByGender)
		  header.add("Gender");
		List<String> sortedATCs = new ArrayList<String>(atcs);
		Collections.sort(sortedATCs);
		header.addAll(sortedATCs);
		header.add("Event");
		header.add("Duration");
		header.add("LogDuration");
		out.write(header);
		String oldPatientID = "";
		List<MergedData> patientData = new ArrayList<MergedData>();
		for (MergedData data : new SCCSDataFileReader(source)){
			if (!data.patientID.equals(oldPatientID)){
				processPatient(out,patientData, event, sortedATCs);
				patientData.clear();
				oldPatientID = data.patientID;
			}
			patientData.add(data);
		}
		processPatient(out,patientData, event, sortedATCs);
		
		out.close();
	}

	private void processPatient(WriteCSVFile out, List<MergedData> patientData,	String eventType, List<String> atcs) {
		if (!findEventTypes(patientData).contains(eventType))
			return;
		Map<List<String>, Data> key2Data = new HashMap<List<String>, PrepareForSCCS.Data>();
		for (MergedData data : patientData){
			List<String> key = new ArrayList<String>();
			key.add(data.patientID);
			if (splitByAge)
			  key.add(data.ageRange);
			if (splitByMonth)
			  key.add(Integer.toString(data.month));
			if (splitByYear)
			  key.add(Integer.toString(data.year));
			if (splitByGender)
			  key.add(data.gender==Patient.MALE?"M":"F");
			for (String atc: atcs)
			  key.add(contains(data.atcCodes,atc)?"1":"0");
			Data d = key2Data.get(key);
			if (d == null){
				d = new Data();
				key2Data.put(key, d);
			}
			d.duration += data.duration;
			if (contains(data.events,eventType))
				d.event = true;
		}

		for (List<String> key : key2Data.keySet()){
			List<String> cells = new ArrayList<String>(key);
			Data data = key2Data.get(key);
			cells.add(data.event?"1":"0");
			cells.add(Long.toString(data.duration));
			cells.add(Double.toString(Math.log(data.duration)));
			out.write(cells);
		}
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

	private void generatePatientDataFiles(String source, Map<String, String> eventATC2Filename) {
		int offset = 0;
		List<String> list = new ArrayList<String>(eventATC2Filename.keySet());
		List<String> header = generateHeader();
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
			String oldPatientID = "";
			List<MergedData> patientData = new ArrayList<MergedData>();
			System.out.println("Processing " + batch.size() + " signals");
			for (MergedData data : new SCCSDataFileReader(source)){
				if (!data.patientID.equals(oldPatientID)){
					processPatient(patientData,event2atcs,eventATC2File);
					patientData.clear();
					oldPatientID = data.patientID;
				}
				patientData.add(data);
			}
			processPatient(patientData,event2atcs,eventATC2File);

			for (WriteCSVFile out : eventATC2File.values())
				out.close();
		}
		StringUtilities.outputWithTime("Done");
	}

	private List<String> generateHeader() {
		List<String> header = new ArrayList<String>();
		header.add("PatientID");
		if (splitByAge)
		  header.add("AgeRange");
		if (splitByMonth)
			header.add("Month");
		if (splitByYear)
			header.add("Year");
		if (splitByGender)
		  header.add("Gender");
		header.add("Exposed");
		header.add("Event");
		header.add("Duration");
		header.add("LogDuration");
		return header;
	}

	private void processPatient(List<MergedData> patientData, OneToManySet<String, String> event2atcs,Map<String, WriteCSVFile> eventATC2File) {
		for (String eventType : findEventTypes(patientData))
			for (String atc : event2atcs.get(eventType)){
				WriteCSVFile out = eventATC2File.get(eventType+"\t"+atc);
				processPatient(out, patientData, eventType, atc);
			}
	}

	private void processPatient(WriteCSVFile out, List<MergedData> patientData,	String eventType, String atc) {
		Map<List<String>, Data> key2Data = new HashMap<List<String>, PrepareForSCCS.Data>();
		for (MergedData data : patientData){
			if (!censorAfterEvent || !data.precedingEventTypes.contains(eventType)){
				List<String> key = new ArrayList<String>();
				key.add(data.patientID);
				if (splitByAge)
				  key.add(data.ageRange);
				if (splitByMonth)
				  key.add(Integer.toString(data.month));
				if (splitByYear)
				  key.add(Integer.toString(data.year));
				if (splitByGender)
				  key.add(data.gender==Patient.MALE?"M":"F");
				key.add(contains(data.atcCodes,atc)?"1":"0");
				Data d = key2Data.get(key);
				if (d == null){
					d = new Data();
					key2Data.put(key, d);
				}
				d.duration += data.duration;
				if (contains(data.events,eventType)){
					d.event = true;
				}
			}
		}

		for (List<String> key : key2Data.keySet()){
			List<String> cells = new ArrayList<String>(key);
			Data data = key2Data.get(key);
			cells.add(data.event?"1":"0");
			cells.add(Long.toString(data.duration));
			cells.add(Double.toString(Math.log(data.duration)));
			out.write(cells);
		}

	}

	private boolean contains(List<Event> events, String eventType) {
		for (Event event : events)
			if (event.eventType.equals(eventType))
				return true;
		return false;
	}

	private boolean contains(Set<ATCCode> atcCodes, String atc) {
		for (ATCCode code : atcCodes)
			if (code.atc.equals(atc))
				return true;
		return false;
	}

	private class Data {
		long duration;
		boolean event = false;
	}

	private Set<String> findEventTypes(List<MergedData> patientData) {
		Set<String> eventTypes = new HashSet<String>();
		for (MergedData data : patientData)
			for (Event event : data.events)
				eventTypes.add(event.eventType);
		return eventTypes;
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


	private Set<String> findEventATCCombinations(String source) {
		StringUtilities.outputWithTime("Starting");
		Map<String, CountingSet<String>> event2atcCounts = new HashMap<String, CountingSet<String>>();
		CountingSetLong<String> exposureCounts = new CountingSetLong<String>();
		for (MergedData data : new SCCSDataFileReader(source)){
			for (ATCCode atc : data.atcCodes)
				if (DeleteLinesWithIllegalATCs.isLegalATC(atc.atc))
					exposureCounts.add(atc.atc, data.duration);
			for (Event event : data.events)
				for (ATCCode atc : data.atcCodes)
					if (DeleteLinesWithIllegalATCs.isLegalATC(atc.atc)){
						CountingSet<String> atcCounts = event2atcCounts.get(event.eventType);
						if (atcCounts == null){
							atcCounts = new CountingSet<String>();
							event2atcCounts.put(event.eventType, atcCounts);
						}
						atcCounts.add(atc.atc);	
					}

		}
		Set<String> limitSet = loadLimitSet();
		Set<String> event2atcs = new HashSet<String>();
		int countBefore = 0;
		int countAfter = 0;
		for (String event : event2atcCounts.keySet()){
			CountingSet<String> atcCounts = event2atcCounts.get(event);
			for (String atc : atcCounts){
				countBefore++;
				if (atcCounts.getCount(atc) >= minEventsDuringExposure && (limitSet == null || limitSet.contains(event+"\t"+atc))){
					if (exposureCounts.getCount(atc) > minExposure){
						event2atcs.add(event+"\t"+atc);
						countAfter++;
					}
				}
			}
		}
		System.out.println("Found " + countBefore + " drug-event combinations, " + countAfter + " after filtering");
		return event2atcs;
	}

	private Set<String> loadLimitSet() {
		if (limitSetFile == null)
			return null;
		else {
			Set<String> limitSet = new HashSet<String>();
			for (Row row : new ReadCSVFileWithHeader(limitSetFile)){
				limitSet.add(row.get("EventType")+"\t"+row.get("ATC"));
			}
			return limitSet;
		}

	}


}
