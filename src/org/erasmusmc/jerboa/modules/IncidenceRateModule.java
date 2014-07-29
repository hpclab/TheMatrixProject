package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.PopulationDistribution;
import org.erasmusmc.jerboa.userInterface.PickButton;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Generates incidence rate tables from the aggregated input data.
 * @author schuemie
 *
 */
public class IncidenceRateModule extends JerboaModule {

	public JerboaModule aggregatedData;

	/**
	 * Holds the data on the reference population.When no data is provided, the standardized error rate 
	 * is not computed. The format should be:<br>
	 * ageRange;percentage (e.g.: '60-64;3.72')<br>
	 */
	public List<String> referencePopulation = new ArrayList<String>();

	/**
	 * Specifies any subgroups to be identified for calculation additional standardized rates.
	 * Each code should be represented as two semicolon separated values:
	 * <OL>
	 * <LI>The label of the subgroup (the same label can be used in multiple rows)</LI>
	 * <LI>The age range</LI>
	 * </OL>
	 * For example: children;0-4
	 */
	public List<String> populationSubgroups = new ArrayList<String>();

	/**
	 * Specifies the string that will be added to the age ranges in the output table so Excel does not 
	 * mistake age-ranges for dates.<BR>
	 * default =  years
	 */
	public String postFix = " years";

	/**
	 * Incidence rates are divided by this number of years.<BR>
	 * default = 100000
	 */
	public int perNYears = 100000;

	/**
	 * When true, only totals per event type are generated.<BR>
	 * default = false 
	 */
	public boolean compactOutput = false;
	
	/**
	 * The minimum number of subjects in a row of the resulting aggregated table. Rows with fewer subjects
	 * are deleted. Only works in combination with the compact output.<BR>
	 * default = 0
	 */
	public int minSubjectsPerRow = 0;

	private Map<String, Set<String>> subgroups = new HashMap<String, Set<String>>();
	private Set<String> ageRanges = new HashSet<String>();
	private Set<String> years = new HashSet<String>();
	private List<String> sortedAgeRanges;
	private static final long serialVersionUID = 1957736003895120569L;
	private static final float daysPerYear = 365.25f;
	private static enum TableType {INCIDENCE, SUBJECTS, DAYS, EVENTS};
	private DataPool dataPool;
	private Data overall;
	private PopulationDistribution referencePopulationDistribution = null;

	public static void main(String[] args){
		IncidenceRateModule module = new IncidenceRateModule();
		module.referencePopulation.add("0-4;8.86");
		module.referencePopulation.add("5-9;8.69");
		module.referencePopulation.add("10-14;8.6");
		module.referencePopulation.add("15-19;8.47");
		module.referencePopulation.add("20-24;8.22");
		module.referencePopulation.add("25-29;7.93");
		module.referencePopulation.add("30-34;7.61");
		module.referencePopulation.add("35-39;7.15");
		module.referencePopulation.add("40-44;6.59");
		module.referencePopulation.add("45-49;6.04");
		module.referencePopulation.add("50-54;5.37");
		module.referencePopulation.add("55-59;4.55");
		module.referencePopulation.add("60-64;3.72");
		module.referencePopulation.add("65-69;2.96");
		module.referencePopulation.add("70-74;2.21");
		module.referencePopulation.add("75-79;1.52");
		module.referencePopulation.add("80-84;0.91");
		module.referencePopulation.add("85-;0.63");
		module.compactOutput = true;
		module.process("/home/data/IPCI/study4/Aggregate.txt", "/home/data/IPCI/study4/myIR.txt");
	}

	protected JPanel createParameterPanel() {
		JPanel panel = super.createParameterPanel();
		PickButton pickButton = new PickButton(PickButton.OPEN, this, "outputFilename");
		panel.add(pickButton);
		return panel;
	}

	@SuppressWarnings("serial")
	private class PopulationData extends HashMap<String, Data>{}

	@SuppressWarnings("serial")
	private class YearData extends HashMap<String, PopulationData>{}

	private class EventData {
		PopulationData populationData = new PopulationData();
		YearData yearData = new YearData();
	}

	private class DataPool {
		private Map<String, EventData> event2data = new HashMap<String, EventData>();
		public void add(String eventType, String ageRange, String gender, String year, long days, int events, int subjects){
			EventData eventData = event2data.get(eventType);
			if (eventData == null){
				eventData = new EventData();
				event2data.put(eventType, eventData);
			}
			String key = gender+":"+ageRange;
			addToData(eventData.populationData, key, days, events, subjects);
			if (year != null){
				PopulationData populationData = eventData.yearData.get(year);
				if (populationData == null){
					populationData = new PopulationData();
					eventData.yearData.put(year, populationData);
				}
				addToData(populationData, key, days, events, subjects);
			}
		}

		public List<String> getEventTypes(){
			List<String> sorted = new ArrayList<String>(event2data.keySet());
			Collections.sort(sorted);
			return sorted;
		}

		public EventData getEventData(String eventType){
			return event2data.get(eventType);
		}

		private void addToData(PopulationData populationData, String key, long days, int events, int subjects) {
			Data data = populationData.get(key);
			if (data == null){
				data = new Data();
				populationData.put(key, data);
			}
			data.days += days;
			data.subjects += subjects;
			data.events += events;
		}
	}

	protected void runModule(String outputFilename){
		process(aggregatedData.getResultFilename(), outputFilename);
	}

	private Map<String, Set<String>> parseSubGroups(List<String> groups) {
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		for (String row : groups){
			String[] cols = row.split(";");
			String label = cols[0];
			String range = cols[1];
			Set<String> ranges = result.get(label);
			if (ranges == null){
				ranges = new HashSet<String>();
				result.put(label, ranges);
			}
			ranges.add(range);
		}
		return result;
	}

	public void process(String source, String target){
		subgroups = parseSubGroups(populationSubgroups);
		if (referencePopulation.size() != 0)
		  referencePopulationDistribution = new PopulationDistribution(referencePopulation);
		readDataFromAggregate(source);

		List<List<String>> lines;
		if (compactOutput)
			lines = generateCompactLines();
		else
			lines = generateLines();
		writeToFile(lines, target);
	}

	private void readDataFromAggregate(String source) {  
		ProgressHandler.reportProgress();
		dataPool = new DataPool();
		overall = new Data();
		ageRanges.clear();

		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next();
		int eventTypeCol = header.indexOf("EventType");
		int ageRangecol = header.indexOf("AgeRange");
		int genderCol = header.indexOf("Gender");
		int daysCol = header.indexOf("Days");
		int subjectsCol = header.indexOf("Subjects");
		int eventsCol = header.indexOf("Events");
		int yearCol = header.indexOf("Year");
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			String eventType;
			if (eventTypeCol == -1)
				eventType = "UGIB";
			else
				eventType = cells.get(eventTypeCol);
			long days = Long.parseLong(cells.get(daysCol));
			int events = Integer.parseInt(cells.get(eventsCol));
			int subjects = Integer.parseInt(cells.get(subjectsCol));
			if (eventType.equals("All")){
				overall.days = days;
				overall.events = events;
				overall.subjects = subjects;
			} else {
				String ageRange = cells.get(ageRangecol);
				if (ageRange.equals(""))
					System.err.println("No agerange found in: " + cells);
				else {
					ageRanges.add(ageRange);
					String gender = cells.get(genderCol);
					String year = null;
					if (yearCol != -1){
						year = cells.get(yearCol);
						years.add(year);
					}
					dataPool.add(eventType, ageRange, gender, year, days, events, subjects);    
				}
			}
		}
	}

	private List<List<String>> generateLines(){
		sortedAgeRanges = new ArrayList<String>(ageRanges);
		Collections.sort(sortedAgeRanges, new AgeRangeComparator());
		List<String> sortedYears = new ArrayList<String>(years);
		Collections.sort(sortedYears);

		List<List<String>> lines = new ArrayList<List<String>>();
		lines.add(overallHeader());
		lines.add(new ArrayList<String>());
		for (String eventType : dataPool.getEventTypes()){
			lines.add(eventTypeHeader(eventType));
			EventData eventData = dataPool.getEventData(eventType);
			List<List<String>> dataLines = new ArrayList<List<String>>();
			dataLines = appendTable(eventData.populationData, dataLines, TableType.INCIDENCE);
			dataLines = appendTable(eventData.populationData, dataLines, TableType.SUBJECTS);
			dataLines = appendTable(eventData.populationData, dataLines, TableType.DAYS);
			dataLines = appendTable(eventData.populationData, dataLines, TableType.EVENTS);
			lines.addAll(dataLines);
			addStandardizedRate(lines, eventData.populationData);


			for (String year : sortedYears){
				lines.add(new ArrayList<String>());
				lines.add(yearHeader(year));
				PopulationData yearData = eventData.yearData.get(year);
				List<List<String>> yearLines = new ArrayList<List<String>>();
				yearLines = appendTable(yearData, yearLines, TableType.INCIDENCE);
				yearLines = appendTable(yearData, yearLines, TableType.SUBJECTS);
				yearLines = appendTable(yearData, yearLines, TableType.DAYS);
				yearLines = appendTable(yearData, yearLines, TableType.EVENTS);
				lines.addAll(yearLines);
				addStandardizedRate(lines, yearData);
			}
			lines.add(new ArrayList<String>());
			lines.add(new ArrayList<String>());
		}

		return lines;
	}

	private List<List<String>> generateCompactLines() {
		List<List<String>> lines = new ArrayList<List<String>>();
		lines.add(generateCompactHeader());
		for (String eventType : dataPool.getEventTypes()){
			EventData eventData = dataPool.getEventData(eventType);
			//Calulate total stats:
			Data total = new Data();
			for (Data data : eventData.populationData.values())
				total.add(data);

			if (total.events < minSubjectsPerRow || total.subjects < minSubjectsPerRow){
				System.out.println("Not enough subjects for " + eventType + ", deleting row.");
				continue;
			}
			List<String> cells = new ArrayList<String>();
			cells.add(eventType);
			cells.add(compute(total, TableType.DAYS));
			cells.add(Integer.toString(overall.subjects));
			cells.add(compute(total, TableType.EVENTS));
			cells.add(compute(total, TableType.INCIDENCE));
			for (Set<String> subgroupAgeRanges : subgroups.values()){
				Data subGroupTotal = new Data();
				for (String subgroupAgeRange : subgroupAgeRanges){
					for (byte gender = 1; gender <= 2; gender++){
						Data data = eventData.populationData.get((gender==1?"M":"F")+":"+subgroupAgeRange);
						if (data != null){
					    subGroupTotal.add(data);
						}
					}
				}
				cells.add(compute(subGroupTotal, TableType.INCIDENCE));
			}
			
			if (referencePopulationDistribution != null){
			  cells.add(computeStdIncidenceRate(eventData.populationData, ageRanges));		
			  for (Set<String> subgroupAgeRanges : subgroups.values())
			  	cells.add(computeStdIncidenceRate(eventData.populationData, subgroupAgeRanges));	
			}
			lines.add(cells);
		}
		return lines;
	}

	private List<String> generateCompactHeader() {
		List<String> header = new ArrayList<String>();
		header.add("EventType");
		header.add("Person Time (years)");
		header.add("Subjects");
		header.add("Events");
		header.add("Incidence (/ "+perNYears+" person years)");
	  for (String subgroup : subgroups.keySet())
	  	header.add("Incidence " + subgroup + " (/ "+perNYears+" person years)");
		
		if (referencePopulationDistribution != null){
		  header.add("Standardized Incidence (/ "+perNYears+" person years)");
		  for (String subgroup : subgroups.keySet())
		  	header.add("Standardized Incidence " + subgroup + " (/ "+perNYears+" person years)");
		}
		return header;
	}

	private void addStandardizedRate(List<List<String>> lines, PopulationData populationData) {
		if (referencePopulationDistribution == null)
			return;
		lines.add(new ArrayList<String>());
		lines.add(addStandardizedIRPerAge(populationData, "", ageRanges));
		for (String subGroup : subgroups.keySet())
			lines.add(addStandardizedIRPerAge(populationData, subGroup, subgroups.get(subGroup)));
	}

	private List<String> overallHeader() {
		List<String> cells = new ArrayList<String>();
		cells.add("Overall");
		cells.add("Subjects:");
		cells.add(Integer.toString(overall.subjects));
		cells.add("Patient time:");
		cells.add(Float.toString(overall.days/daysPerYear));
		cells.add("Events:");
		cells.add(Integer.toString(overall.events));
		return cells;
	}

	private List<String> eventTypeHeader(String eventType) {
		List<String> cells = new ArrayList<String>();
		cells.add("*** For event type " + eventType + " ***");
		return cells;
	}

	private List<List<String>> appendTable(Map<String, Data> key2Data, List<List<String>> lines, TableType tableType) {
		int lineNr = 0;
		List<String> cells = new ArrayList<String>();
		switch (tableType)  {
		case INCIDENCE : cells.add("Incidence (/ "+perNYears+" person years)"); break;
		case SUBJECTS : cells.add("Subjects"); break;
		case DAYS : cells.add("Patient time (years)"); break;
		case EVENTS : cells.add("Events"); break;
		}
		cells.add("Male");
		cells.add("Female");
		cells.add("Total");
		addToLine(lines, lineNr++, cells);
		Data maleTotal = new Data();
		Data femaleTotal = new Data();
		for (String ageRange : sortedAgeRanges){
			Data rowTotal = new Data();
			Data cellMale = get(key2Data, 'M', ageRange);
			Data cellFemale = get(key2Data, 'F', ageRange);
			rowTotal.add(cellMale);
			rowTotal.add(cellFemale);
			maleTotal.add(cellMale);
			femaleTotal.add(cellFemale);

			cells = new ArrayList<String>();
			cells.add(ageRange + postFix);
			cells.add(compute(cellMale, tableType));
			cells.add(compute(cellFemale, tableType));
			cells.add(compute(rowTotal, tableType));
			addToLine(lines, lineNr++, cells);
		}
		if (tableType == TableType.SUBJECTS){
			cells = new ArrayList<String>();
			cells.add("");
			cells.add("");
			cells.add("");
			cells.add("");
			addToLine(lines, lineNr++, cells);
		} else {
			Data rowTotal = new Data();
			rowTotal.add(maleTotal);
			rowTotal.add(femaleTotal);   
			cells = new ArrayList<String>();
			cells.add("Total");
			cells.add(compute(maleTotal, tableType));
			cells.add(compute(femaleTotal, tableType));
			cells.add(compute(rowTotal, tableType));
			addToLine(lines, lineNr++, cells);
		}
		return lines;
	}

	private String compute(Data data, TableType tableType) {
		switch (tableType)  {
		case INCIDENCE : return Float.toString(data.events / (data.days / (perNYears * daysPerYear)));
		case SUBJECTS : return Integer.toString(data.subjects);
		case DAYS : return Float.toString(data.days / daysPerYear);
		case EVENTS : return Integer.toString(data.events);
		default : return null;
		}
	}

	private String computeStdIncidenceRate(PopulationData populationData, Collection<String> selectedAgeRanges){
		double rate = 0;
		double populationPercentage = 0;
		boolean illegal = false;

		for (String ageRange : selectedAgeRanges){
			int events = 0;
			long days = 0;
			for (byte gender = 1; gender <= 2; gender++){
				Data data = populationData.get((gender==1?"M":"F")+":"+ageRange);
				if (data == null){
					illegal = true;
				} else {	
					events += data.events;
					days += data.days;
				} 
			}
			double localRate = perNYears * daysPerYear * events / (double)days;  
			double normalisedCount = referencePopulationDistribution.getNormalisedCount(ageRange);
			rate += localRate * normalisedCount;
			populationPercentage += normalisedCount;
		}

		if (illegal) 
			return "incomparable population";
		else
			return Double.toString(rate / populationPercentage);
	}

	private Data get(Map<String, Data> key2Data, char gender, String ageRange) {
		Data data = key2Data.get(gender+":"+ageRange);
		if (data == null)
			return new Data();
		else
			return data;
	}

	private void addToLine(List<List<String>> lines, int pos, List<String> cells) {
		for (int i = lines.size(); i < pos+1; i++)
			lines.add(new ArrayList<String>());
		List<String> line = lines.get(pos);
		if (line.size() != 0)
			line.add("");
		line.addAll(cells);
	}

	private List<String> yearHeader(String year) {
		List<String> cells = new ArrayList<String>();
		cells.add("For year " + year);
		return cells;
	}

	private void writeToFile(List<List<String>> lines, String target) {
		WriteCSVFile out = new WriteCSVFile(target);
		for (List<String> line : lines)
			out.write(line);
		out.close();
	}

	private List<String> addStandardizedIRPerAge(PopulationData populationData, String label, Collection<String> selectedAgeRanges) {
		List<String> cells = new ArrayList<String>();
		cells.add("Standardized: " + label);
		cells.add("");
		cells.add("");
		cells.add(computeStdIncidenceRate(populationData, selectedAgeRanges));
		return cells;
	}

	private class Data {
		public long days;
		public int events;
		public int subjects;
		public void add(Data data){
			days += data.days;
			events += data.events;
			subjects += data.subjects;
		}
	}

	private class AgeRangeComparator implements Comparator<String>{

		public int compare(String arg0, String arg1) {
			int a0 = getNumber(arg0);
			int a1 = getNumber(arg1);
			if (a0 != -1 && a1 != -1)
				return a0-a1;
			else
				return arg0.compareTo(arg1);
		}

		private int getNumber(String string){
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < string.length(); i++){
				char ch = string.charAt(i);
				if (Character.isDigit(ch))
					sb.append(ch);
				else
					break;
			}
			if (sb.length() > 0)
				return Integer.parseInt(sb.toString());
			else 
				return -1;
		} 
	}
}

