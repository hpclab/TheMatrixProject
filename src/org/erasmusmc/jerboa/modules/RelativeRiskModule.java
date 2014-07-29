package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.erasmusmc.jerboa.ATC2Name;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.calculations.FastStratifiedExact;
import org.erasmusmc.jerboa.calculations.MantelHaenszel;
import org.erasmusmc.jerboa.calculations.MantelHaenszel.Stats;
import org.erasmusmc.jerboa.calculations.PersonTimeData;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.jerboa.userInterface.PickButton;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Generates relative risks from the aggregated input data.
 * @author schuemie
 *
 */
public class RelativeRiskModule extends JerboaModule {

	public JerboaModule aggregatedData;

	/**
	 * ATC level (number of characters) at which the analysis should be performed.<BR>
	 * default = 7
	 */
	public int atcLevel = 7;

	/**
	 * Minimum number of events to include a signal in the resulting table.<BR>
	 * default = 5
	 */
	public int minEvents = 5; 

	/**
	 * Compare exposure to exposure to other drugs with the same indication.<BR>
	 * default = false
	 */
	public boolean compareToSubATC = false;

	/**
	 * Specifies the ATC level for indications (for compareToSubATC).<BR>
	 * default = 3
	 */
	public int subATCLevel = 3;

	/**
	 * Output the standard error in the results file.<BR>
	 * default = false
	 */
	public boolean outputStandardError = false;

	/**
	 * Sort the output table by RRmh (will take a lot of memory for extremely large numbers signals).<BR>
	 * default = true
	 */
	public boolean outputSorted = true;

	/**
	 * Output the expected number of events based on the background rate.<BR>
	 * default = false
	 */
	public boolean outputExpected = false;

	/**
	 * Output the non-exposed days and events.<BR>
	 * default = false
	 */
	public boolean outputNonExposed = false;


	public boolean outputExactP = false;

	private static String POSTEXPOSUREPREFIX = "P";
	private static int maxLineCountForCodeIdentification = 1000000;
	private static ATC2Name atc2Name = null;
	private boolean verbose = false;
	private EventData background;
	private static final long serialVersionUID = 2061686531686889743L;
	private Set<String> exposureCodes;
	private Set<String> postExposureCodes;
	private int daysCol;
	private int eventsCol;
	private int ageCol;
	private int genderCol;
	private int atcCol;
	private int precedingEventTypesCol;

	protected JPanel createParameterPanel() {
		JPanel panel = super.createParameterPanel();
		PickButton pickButton = new PickButton(PickButton.OPEN, this, "outputFilename");
		panel.add(pickButton);
		return panel;
	}

	public static void main(String[] args){
		String folder = "x:/";
		//String folder = "/home/data/Pooled/Version 3.0 (Silver)/";
		//String folder = "C:/home/data/Simulated/";
		String sourceFile = folder + "AggregatebyATC.txt";
		//FileSorter.sort(sourceFile, "ATC");
		RelativeRiskModule module = new RelativeRiskModule();
		module.outputSorted = false;
		module.verbose = true;
		module.atcLevel = 7;
		module.minEvents = 0;
		module.compareToSubATC = false;
		module.subATCLevel = 3;
		module.outputExpected = true;
		module.outputStandardError = false;
		module.outputNonExposed = false;
		module.outputExactP = false;
		module.process(sourceFile, folder+"RelativeRisks.csv");
		//module.process(folder+"PooledTable2.txt", folder+"RelativeRisks5.csv");
	}

	private class PopulationData {
		private Map<String, PersonTimeData> agegender2data = new HashMap<String, PersonTimeData>();
		public void add(String age, String gender, int events, long days){
			String ageGender= age + ":" + gender;
			PersonTimeData data = agegender2data.get(ageGender);
			if (data == null){
				data = new PersonTimeData();
				agegender2data.put(ageGender, data);
			}
			data.days += days;
			data.events += events;
		}

		public Map<String, PersonTimeData> getMap(){
			return agegender2data;
		}

		public PersonTimeData aggregate(){
			PersonTimeData aggregate = new PersonTimeData();
			for (PersonTimeData data : agegender2data.values())
				aggregate.add(data);
			return aggregate;
		}

		public PopulationData copy() {
			PopulationData copy = new PopulationData();
			for (Map.Entry<String, PersonTimeData> entry : agegender2data.entrySet())
				copy.agegender2data.put(entry.getKey(), entry.getValue().copy());
			return copy;
		}

		public void add(PopulationData populationData) {
			for (Map.Entry<String, PersonTimeData> entry : populationData.agegender2data.entrySet()){
				PersonTimeData data = agegender2data.get(entry.getKey());
				if (data == null){
					data = new PersonTimeData();
					agegender2data.put(entry.getKey(), data);
				}
				data.add(entry.getValue());						
			}
		}
	}

	private class ExposureData {
		private Map<String, EventData> code2EventData = new HashMap<String, EventData>();

		public void add(String code, Map<String, Integer> eventCounts, Set<String> precedingEventTypes,String age, String gender, long days){
			EventData eventData = code2EventData.get(code);
			if (eventData == null){
				eventData = new EventData();
				code2EventData.put(code, eventData);
			}
			eventData.add(eventCounts, precedingEventTypes, age, gender, days);
		}

		public EventData getEventData(String code){
			return code2EventData.get(code);
		}

		public EventData aggregateOverExposure(){
			EventData aggregated = new EventData();
			for (Map.Entry<String, EventData> entry : code2EventData.entrySet())
				if (entry.getKey() == null || !entry.getKey().startsWith(POSTEXPOSUREPREFIX)) // Post exposure codes are not part of exposure!!!!
					aggregated.add(entry.getValue());
			return aggregated;
		}
	}

	private class EventData {
		Map<String,PopulationData> eventType2PopulationData = new HashMap<String, PopulationData>();
		PopulationData genericData = new PopulationData();

		public void add(Map<String, Integer> eventCounts, Set<String> precedingEventTypes,String age, String gender, long days){
			ensureEventsHaveData(eventCounts.keySet());
			ensureEventsHaveData(precedingEventTypes);
			genericData.add(age,gender,0,days);
			for (Map.Entry<String, PopulationData> entry  : eventType2PopulationData.entrySet()){
				if (!precedingEventTypes.contains(entry.getKey())){
					Integer events = eventCounts.get(entry.getKey());
					if (events == null)
						events = 0;
					entry.getValue().add(age, gender, events, days);
				}
			}
		}

		public void add(EventData other) {
			for (Map.Entry<String, PopulationData> entry  : other.eventType2PopulationData.entrySet()){
				PopulationData populationData = eventType2PopulationData.get(entry.getKey());
				if (populationData == null){
					populationData = genericData.copy();
					eventType2PopulationData.put(entry.getKey(), populationData);
				} 
				populationData.add(entry.getValue());
			}
			for (Map.Entry<String, PopulationData> entry  : eventType2PopulationData.entrySet()){
				if (!other.eventType2PopulationData.containsKey(entry.getKey()))
					entry.getValue().add(other.genericData);
			}
			genericData.add(other.genericData);
		}

		private void ensureEventsHaveData(Set<String> events) {
			for (String event : events)
				if (eventType2PopulationData.get(event) == null)
					eventType2PopulationData.put(event,genericData.copy());
		}

		public Set<String> getEventTypes() {
			return eventType2PopulationData.keySet();
		}

		public PopulationData getPopulationData(String eventType) {
			PopulationData populationData = eventType2PopulationData.get(eventType);
			if (populationData == null)
				return genericData;
			else
				return populationData;
		}
	}


	protected void runModule(String outputFilename){
		FileSorter.sort(aggregatedData.getResultFilename(), "ATC");
		process(aggregatedData.getResultFilename(), outputFilename);
	}

	public void process(String source, String target){
		loadATCTable();
		identifyExposureCodes(source);

		Output output;
		if (outputSorted)
			output = new SortedOutput();
		else
			output = new DirectOutput();

		output.open(target);
		output.write(createHeader(), Double.POSITIVE_INFINITY);

		generateLines(source, output);

		output.close();		
		if (verbose)
			StringUtilities.outputWithTime("Done");
		
		//Dereference data objects:
		background = null;
		exposureCodes = null;
		postExposureCodes = null;	
	}

	private void loadATCTable() {
		if (atc2Name == null)
			atc2Name = new ATC2Name(RelativeRiskModule.class.getResourceAsStream("ATC_2008.xml"));
	}

	private void identifyExposureCodes(String source) {
		if (verbose)
			StringUtilities.outputWithTime("Identifying exposure codes");
		exposureCodes = new HashSet<String>();
		postExposureCodes = new HashSet<String>();


		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next(); 
		int atcCol = header.indexOf("ATC");
		int lineCount = 0;
		while (iterator.hasNext() && lineCount < maxLineCountForCodeIdentification){
			lineCount++;
			String atcString = iterator.next().get(atcCol);
			String[] parts = atcString.split(":");
			if (parts.length == 2)
				if (parts[1].startsWith(POSTEXPOSUREPREFIX))
					postExposureCodes.add(parts[1]);
				else
					exposureCodes.add(parts[1]);
		}
	}

	private void generateLines(String source, Output output) {
		if (verbose)
			StringUtilities.outputWithTime("Reading data from " + source);
		ProgressHandler.reportProgress();
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();

		parseHeader(iterator.next()); 
		AggregatedData aggregatedData = new AggregatedData(iterator.next());
		int lineCount = 0;
		//Read background data:
		background = new EventData();
		while (aggregatedData.atcWithCode.atc.equals("")){
			ProgressHandler.reportProgress();
			lineCount++;
			if (verbose && lineCount%10000 == 0)
				System.out.println("Processed " + lineCount + " lines");
			background.add(aggregatedData.eventCounts, aggregatedData.precedingEvents, aggregatedData.age, aggregatedData.gender, aggregatedData.days);
			if (iterator.hasNext())
				aggregatedData = new AggregatedData(iterator.next());
			else 
				return;
		}
		ExposureData exposureData = new ExposureData();
		ExposureData subExposureData = new ExposureData();
		Map<String, ExposureData> atc2exposureData = new HashMap<String, ExposureData>();

		String previousATC = aggregatedData.atcWithCode.atc;
		String previousSubATC = aggregatedData.subATC;
		while (aggregatedData != null){
			lineCount++;
			if (verbose && lineCount%10000 == 0)
				System.out.println("Processed " + lineCount + " lines");
			if (!aggregatedData.atcWithCode.atc.equals(previousATC)){
				
				if (previousSubATC == null )
					generateLinesForATC(previousATC, exposureData, background, null, output, false);
				else
					atc2exposureData.put(previousATC, exposureData);
				
				if (previousSubATC != null && !previousSubATC.equals(aggregatedData.subATC)){
					if (atc2exposureData.size() == 1)
						generateLinesForATC(previousATC, exposureData, background, null, output, false);
					else
					  for (String atc : atc2exposureData.keySet())
						  generateLinesForATC(atc, atc2exposureData.get(atc), subExposureData.aggregateOverExposure(), subExposureData, output, true);
					subExposureData = new ExposureData();
					atc2exposureData = new HashMap<String, ExposureData>();
				}
				exposureData = new ExposureData();
				previousATC = aggregatedData.atcWithCode.atc;
				previousSubATC = aggregatedData.subATC;
			}
			exposureData.add(aggregatedData.atcWithCode.exposureCode, aggregatedData.eventCounts, aggregatedData.precedingEvents, aggregatedData.age, aggregatedData.gender, aggregatedData.days);
			if (aggregatedData.subATC != null)
				subExposureData.add(aggregatedData.atcWithCode.exposureCode, aggregatedData.eventCounts, aggregatedData.precedingEvents, aggregatedData.age, aggregatedData.gender, aggregatedData.days);
			if (iterator.hasNext())
				aggregatedData = new AggregatedData(iterator.next());
			else
				aggregatedData = null;
		} //end while
		if (previousSubATC == null )
			generateLinesForATC(previousATC, exposureData, background, null, output, false);
		else {
			atc2exposureData.put(previousATC, exposureData);
			if (atc2exposureData.size() == 1)
				generateLinesForATC(previousATC, exposureData, background, null, output, false);
			else
			  for (String atc : atc2exposureData.keySet())
				  generateLinesForATC(atc, atc2exposureData.get(atc), subExposureData.aggregateOverExposure(), subExposureData,  output, true);
		}
	}

	private void generateLinesForATC(String atc, ExposureData exposureData, EventData backgroundEventData, ExposureData backgroundExposureData,	Output output, boolean isComparedToSubATC) {
		ProgressHandler.reportProgress();
		EventData exposedEventData = exposureData.aggregateOverExposure();
		for (String eventType : background.getEventTypes()){					
			PopulationData exposedPopulationData = exposedEventData.getPopulationData(eventType);
			PersonTimeData data = exposedPopulationData.aggregate();
			if (data.events >= minEvents){
				PopulationData backgroundPopulationData = backgroundEventData.getPopulationData(eventType);
				
				Stats stats = MantelHaenszel.calculateRateStats(exposedPopulationData.getMap(), backgroundPopulationData.getMap());
				if (stats.expected == 0) //Skip this line if RR will be infinity
					continue;

				List<String> cells = new ArrayList<String>();
				cells.add(atc);
				cells.add(createName(atc2Name, atc));
				cells.add(eventType);
				cells.add(Long.toString(data.days));
				cells.add(Long.toString(data.events));
				  if (compareToSubATC)
				  cells.add(isComparedToSubATC?Integer.toString(subATCLevel):"0");
				cells.add(Double.toString(stats.rrmh));
				cells.add(Double.toString(stats.ci95up));
				cells.add(Double.toString(stats.ci95down));
				cells.add(Double.toString(stats.p));
				if (outputExactP){
					double p = new FastStratifiedExact().calculateExactStats(exposedPopulationData.getMap(), backgroundPopulationData.getMap()).upFishPVal;
					if (p < 0)
						p = 0;
					cells.add(Double.toString(p));
				}
				if (outputStandardError)
					cells.add(Double.toString(stats.se));
				if (outputExpected)
					cells.add(Double.toString(stats.expected));
				if (outputNonExposed){
					PersonTimeData aggregatedBackgroundData = new PersonTimeData();
					for (PersonTimeData backgroundData : backgroundPopulationData.getMap().values())
						aggregatedBackgroundData.add(backgroundData);

					cells.add(Long.toString(aggregatedBackgroundData.days - data.days));
					cells.add(Integer.toString(aggregatedBackgroundData.events - data.events));
				}
				for (String code : exposureCodes)
					cells.addAll(generateExposureCodeStatistics(code, eventType, exposureData, backgroundExposureData, backgroundPopulationData));
				for (String code : postExposureCodes)
					cells.addAll(generateExposureCodeStatistics(code, eventType, exposureData, backgroundExposureData, backgroundPopulationData));
				output.write(cells, stats.rrmh);

			}
		}
	}


	private void parseHeader(List<String> header) {
		daysCol = header.indexOf("Days");
		eventsCol = header.indexOf("Events");
		ageCol = header.indexOf("AgeRange");
		genderCol = header.indexOf("Gender");
		atcCol = header.indexOf("ATC");
		precedingEventTypesCol = header.indexOf("PrecedingEventTypes");
	}

	private class AggregatedData { 
		String gender;
		String age;
		long days;
		Map<String, Integer> eventCounts;
		Set<String> precedingEvents;
		ATCCode atcWithCode;
		String subATC;
		public AggregatedData(List<String> cells){
			gender = cells.get(genderCol);
			age = cells.get(ageCol);
			days = Long.parseLong(cells.get(daysCol));
			eventCounts = parseEventsString(cells.get(eventsCol));
			precedingEvents = parsePrecedingEventsString(cells.get(precedingEventTypesCol));
			atcWithCode = new ATCCode(cells.get(atcCol));

			if (compareToSubATC && atcWithCode.atc.length() >= subATCLevel)
				subATC = atcWithCode.atc.substring(0,subATCLevel);

			if (atcWithCode.atc.length() > atcLevel)
				atcWithCode.atc = atcWithCode.atc.substring(0,atcLevel);
		}
	}

	private List<String> generateExposureCodeStatistics(String code, String eventType, ExposureData exposureData, ExposureData backgroundExposureData, PopulationData backgroundPopulationData) {
		List<String> statistics = new ArrayList<String>();
		EventData eventData = exposureData.getEventData(code);
		if (eventData == null) { //drug does not appear with this exposure code. Return default values:
			statistics.add("0");
			statistics.add("0");
			statistics.add("");
			statistics.add("");
			statistics.add("");
			statistics.add("");
			if (outputStandardError)
				statistics.add("");
			if (outputExpected)
				statistics.add("0");
		} else { //drug does appear with this exposure code: calculate statistics:
			PopulationData exposedCodePopulationData = eventData.getPopulationData(eventType);
			PopulationData backgroundCodePopulationData;
			if (backgroundExposureData == null)
				backgroundCodePopulationData = backgroundPopulationData;
			else
				backgroundCodePopulationData = backgroundExposureData.getEventData(code).getPopulationData(eventType);
			Stats codeStats;
			PersonTimeData exposureCodeData = new PersonTimeData();

			if (exposedCodePopulationData == null){
				codeStats = new Stats();
			} else {
				codeStats = MantelHaenszel.calculateRateStats(exposedCodePopulationData.getMap(), backgroundCodePopulationData.getMap());
				for (PersonTimeData localData : exposedCodePopulationData.agegender2data.values()){
					exposureCodeData.days += localData.days;
					exposureCodeData.events += localData.events;							
				}
			}
			statistics.add(Long.toString(exposureCodeData.days));
			statistics.add(Long.toString(exposureCodeData.events));							
			statistics.add(Double.toString(codeStats.rrmh));
			statistics.add(Double.toString(codeStats.ci95up));
			statistics.add(Double.toString(codeStats.ci95down));
			statistics.add(Double.toString(codeStats.p));
			if (outputStandardError)
				statistics.add(Double.toString(codeStats.se));
			if (outputExpected)
				statistics.add(Double.toString(codeStats.se));
		}
		return statistics;
	}

	private List<String> createHeader() {
		List<String> header = new ArrayList<String>();
		header.add("ATC");
		header.add("Name");
		header.add("EventType");
		header.add("Exposure");
		header.add("Events");
		if (compareToSubATC)
			header.add("ComparedToSublevel");
		header.add("RRmh");
		header.add("CI95up");
		header.add("CI95down");
		header.add("p");
		if (outputExactP)
			header.add("Exact p");
		if (outputStandardError)
			header.add("se");
		if (outputExpected)
			header.add("expected");
		if (outputNonExposed){
			header.add("NonExposedDays");
			header.add("NonExposedEvents");
		}
		for (String code : exposureCodes){
			header.add(code + "_Exposure");
			header.add(code + "_Events");
			header.add(code + "_RRmh");
			header.add(code + "_CI95up");
			header.add(code + "_CI95down");
			header.add(code + "_p");
			if (outputStandardError)
				header.add(code+"_se");
			if (outputExpected)
				header.add(code+"_expected");
		}
		for (String code : postExposureCodes){
			header.add(code + "_Exposure");
			header.add(code + "_Events");
			header.add(code + "_RRmh");
			header.add(code + "_CI95up");
			header.add(code + "_CI95down");
			header.add(code + "_p");
			if (outputStandardError)
				header.add(code+"_se");
			if (outputExpected)
				header.add(code+"_expected");
		}
		return header;
	}


	private String createName(ATC2Name atc2Name, String atcs) {
		StringBuilder string = new StringBuilder();
		for (String atc : StringUtilities.safeSplit(atcs, '&')){
			if (string.length() != 0)
				string.append(" & ");
			string.append(atc2Name.getName(atc));
		}
		return string.toString();
	}


	private Set<String> parsePrecedingEventsString(String precedingEventsString) {
		if (precedingEventsString.length() == 0)
			return Collections.emptySet();
		else
			return new HashSet<String>(StringUtilities.safeSplit(precedingEventsString, '+'));
	}

	private Map<String, Integer> parseEventsString(String eventsString) {
		Map<String, Integer> eventCounts;
		if (eventsString.length() == 0)
			eventCounts = Collections.emptyMap();
		else {
			eventCounts = new HashMap<String, Integer>(1);			
			for (String eventString : StringUtilities.safeSplit(eventsString,'+')){
				String[] parts = eventString.split(":");
				eventCounts.put(parts[0], Integer.parseInt(parts[1]));
			}	
		}
		return eventCounts;
	}

	private interface Output {
		public void open(String filename);
		public void write(List<String> line, double sortValue);
		public void close();
	}

	private class DirectOutput implements Output {
		private WriteCSVFile out;
		@Override
		public void close() {
			out.close();
		}

		@Override
		public void open(String filename) {
			out = new WriteCSVFile(filename);			
		}

		@Override
		public void write(List<String> line, double sortValue) {
			out.write(line);			
		}


	}

	private class SortedOutput implements Output {
		private String filename;
		private List<SortValue2line> list = new ArrayList<SortValue2line>();

		@Override
		public void close() {
			if (verbose)
				StringUtilities.outputWithTime("Sorting output");
			Collections.sort(list, new Comparator<SortValue2line>(){
				public int compare(SortValue2line o1, SortValue2line o2) {
					return Double.compare(o2.value,o1.value);
				}});

			WriteCSVFile out = new WriteCSVFile(filename);
			for (SortValue2line ratio2line : list){
				out.write(ratio2line.line);
			}
			out.close(); 
		}

		@Override
		public void open(String filename) {
			this.filename = filename;			
		}

		@Override
		public void write(List<String> line, double sortValue) {
			list.add(new SortValue2line(sortValue, line));
		}

		private class SortValue2line {
			double value;
			List<String> line;
			public SortValue2line(double value, List<String> line){
				this.value = value;
				this.line = line;
			}
		}
	}
}
