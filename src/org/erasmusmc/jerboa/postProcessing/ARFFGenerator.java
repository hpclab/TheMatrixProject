package org.erasmusmc.jerboa.postProcessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteTextFile;

public class ARFFGenerator {

	private static String eventTypeHeader = "EventType";
	private static String ageRangeHeader = "AgeRange";
	private static String drugHeader = "current_DaysSinceUse";
	private static String genderHeader = "Gender";
	private static String isCaseHeader = "IsCase";
	
	private Map<String, Integer> drugsUsed;
	private List<String> drugsSorted;
	private List<String> eventsUsed;
	private Map<String, Integer> ageRangesUsed;
	private List<String> ageRangesSorted;
	
	public static void main(String[] args) {
		new ARFFGenerator().process("/home/data/OSIM/CaseControlForJan/CaseSetsSampled.txt", "/home/data/OSIM/CaseControlForJan/CaseSetsSampled.ARFF");

	}

	public void process(String source, String target) {
		identifyUsedDrugsEventsAgeRanges(source);
		WriteTextFile out = new WriteTextFile(target);
		out.writeln("% age ranges:");
		for (String ageRange : ageRangesSorted)
			out.writeln("% " + ageRangesUsed.get(ageRange) + " = " + ageRange);
		out.writeln("");
		out.writeln("@RELATION drug-event");
		out.writeln("");
		out.writeln("@ATTRIBUTE AgeRange	NUMERIC");
		out.writeln("@ATTRIBUTE Gender	{M,F}");
		for (String drug : drugsSorted)
			out.writeln("@ATTRIBUTE Drug_"+drug+"	NUMERIC");
		out.writeln("@ATTRIBUTE class	{NONE,"+StringUtilities.join(eventsUsed, ",")+"}");
		out.writeln("");
		out.writeln("@DATA");
		addDataLines(out, source);
		out.close();
		
	}

	private void addDataLines(WriteTextFile out, String source) {
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next();
		int drugCol = header.indexOf(drugHeader);
		int eventCol = header.indexOf(eventTypeHeader);
		int ageRangeCol = header.indexOf(ageRangeHeader);
		int genderCol = header.indexOf(genderHeader);
		int isCaseCol = header.indexOf(isCaseHeader);
		while (iterator.hasNext()){
			List<String> cells= iterator.next();
			List<String> newCells = new ArrayList<String>(3+drugsUsed.size());
			newCells.add(ageRangesUsed.get(cells.get(ageRangeCol)).toString());
			newCells.add(cells.get(genderCol));
			for (int i = 2; i < 2+drugsUsed.size(); i++)
				newCells.add("99");
		  for (DrugDays drugDays : parseDrugsString(cells.get(drugCol)))
		  	newCells.set(2+drugsUsed.get(drugDays.drug), drugDays.days);
		  if (cells.get(isCaseCol).equals("0"))
		    newCells.add("NONE");
		  else
		  	newCells.add(cells.get(eventCol));
		  out.writeln(StringUtilities.join(newCells, ","));
		}
	}

	private void identifyUsedDrugsEventsAgeRanges(String source) {
		Set<String> drugs = new HashSet<String>();
		Set<String> events = new HashSet<String>();
		Set<String> ageRanges = new HashSet<String>();
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next();
		int drugCol = header.indexOf(drugHeader);
		int eventCol = header.indexOf(eventTypeHeader);
		int ageRangeCol = header.indexOf(ageRangeHeader);
		while (iterator.hasNext()){
			List<String> cells= iterator.next();
			events.add(cells.get(eventCol));
			ageRanges.add(cells.get(ageRangeCol));
			for (DrugDays drugDays : parseDrugsString(cells.get(drugCol)))
				drugs.add(drugDays.drug);
		}
		System.out.println("Found: " + drugs.size() + " drugs, " + events.size() + " events, and " + ageRanges.size() + " ageranges");
		drugsUsed = new HashMap<String, Integer>();
	  drugsSorted = new ArrayList<String>(drugs);
	  Collections.sort(drugsSorted);
	  for (String drug : drugsSorted)
	  	drugsUsed.put(drug, drugsUsed.size());
	  
	  eventsUsed = new ArrayList<String>(events);
	  Collections.sort(eventsUsed);

	  ageRangesUsed = new HashMap<String, Integer>();
	  ageRangesSorted = new ArrayList<String>(ageRanges);
	  Collections.sort(ageRangesSorted, new Comparator<String>(){

			@Override
			public int compare(String arg0, String arg1) {
				String[] parts0 = arg0.split("-");
				String[] parts1 = arg1.split("-");
				return Double.compare(Double.parseDouble(parts0[0]), Double.parseDouble(parts1[0]));
			}});
	  for (String ageRange : ageRangesSorted)
	  	ageRangesUsed.put(ageRange, ageRangesUsed.size());
	}
	
	private List<DrugDays> parseDrugsString(String string){
		if (string.length() == 0)
			return Collections.emptyList();
		List<DrugDays> drugDays = new ArrayList<DrugDays>();
		String[] parts = string.split("\\+");
		for (String part : parts){
			String[] dd = part.split(":");
			drugDays.add(new DrugDays(dd[0],dd[1]));
		}
		return drugDays;
	}
	
	private class DrugDays {
		String drug;
		String days;
		public DrugDays(String drug, String days){
			this.days = days;
			this.drug = drug;
		}
	}
}
