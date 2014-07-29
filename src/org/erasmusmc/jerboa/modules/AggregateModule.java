package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.MergedDataFileReader;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.jerboa.userInterface.VirtualTable;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Aggregates data from a merged data file by age and gender, and optionally by calendar year and month.
 * @author schuemie
 *
 */
public class AggregateModule extends JerboaModule{

	public JerboaModule mergedData;

	/**
	 * The minimum number of subjects in a row of the resulting aggregated table. Rows with fewer subjects
	 * are deleted.<BR>
	 * default = 0
	 */
	public int minSubjectsPerRow = 0;

	/**
	 * Create aggregates for each calendar year.<BR>
	 * Warning: the input table should be split by calendar year (i.e it should not contain episodes spanning
	 * two years).<BR>
	 * default = false 
	 */
	public boolean includeYear = false;

	/**
	 * Create aggregates for each calendar month.<BR>
	 * Warning: the input table should be split by calendar month (i.e it should not contain episodes spanning
	 * two months).<BR>
	 * default = false 
	 */
	public boolean includeMonth = false;

	private Data statistics;
	private Data deleted;
	private Data overall;
	private DataPool dataPool;
	private static final long serialVersionUID = 4857025429275256502L;

	public static void main(String[] args){
		//FileSorter.sort("/home/data/IPCI/study4/testMergedata.txt", new String[]{"PatientID", "Date"});
		AggregateModule module = new AggregateModule();
		module.includeMonth = true;
		module.process("/home/data/IPCI/study4/testMergedata.txt", "/home/data/IPCI/study4/testAggregate.txt");
	}

	protected void runModule(String outputFilename){
		if (mergedData.isVirtual()){
			@SuppressWarnings("unchecked")
			VirtualTable<MergedData> virtualTable = (VirtualTable<MergedData>) mergedData;
			process(virtualTable.getIterator(), outputFilename);
		} else {
			FileSorter.sort(mergedData.getResultFilename(), new String[]{"PatientID", "Date"});
			process(mergedData.getResultFilename(), outputFilename);
		}
	}

	public void process(String sourceMergedDate, String target) {
		Iterator<MergedData> mergedDateIterator = new MergedDataFileReader(sourceMergedDate).iterator();
		process(mergedDateIterator, target);
	}

	private void process(Iterator<MergedData> mergedDateIterator, String target) {
		dataPool = new DataPool();
		overall = new Data();
		String oldSubjectKey = "";
		String oldPatientID = "";
		while (mergedDateIterator.hasNext()){
			MergedData mergedData = mergedDateIterator.next();
			if (!mergedData.outsideCohortTime){
				ProgressHandler.reportProgress();
				mergedData.atcCodes.add(new ATCCode(""));
				oldPatientID = addToOverall(mergedData, oldPatientID);
				oldSubjectKey = addToDataPool(mergedData, oldSubjectKey);
			}
		}
		dumpDataToFile(target);

		//Dereference data objects:
		statistics = null;
		deleted = null;
		overall = null;
		dataPool = null;
	}

	private String addToOverall(MergedData mergedData, String oldPatientID) {
		if (!mergedData.patientID.equals(oldPatientID))
			overall.subjects++;
		overall.days += mergedData.duration;
		overall.events += mergedData.events.size();
		return mergedData.patientID;
	}

	private String addToDataPool(MergedData mergedData, String oldSubjectKey) {
		String subjectKey = mergedData.patientID + "|" + mergedData.ageRange;
		int year = -1;
		int month = -1;
		if (includeYear){
			year = mergedData.year;
			subjectKey += "|" + year;
		}
		if (includeMonth){
			month = mergedData.month;
			subjectKey += "|" + month;
		}

		String key = mergedData.ageRange + "|" + ((mergedData.gender == Patient.MALE)?"M":"F");
		if (includeYear)
			key += "|" + year;
		if (includeMonth)
			key += "|" + month;

		int subjects = (subjectKey.equals(oldSubjectKey)?0:1);

		dataPool.add(mergedData.events, mergedData.precedingEventTypes, key, mergedData.duration, subjects);

		return subjectKey;
	}

	private void dumpDataToFile(String target) {
		WriteCSVFile out = new WriteCSVFile(target);
		out.write(generateHeader());
		out.write(generatePopulationCounts());
		statistics = new Data();
		deleted = new Data();
		for (String eventType : dataPool.getEventTypes()){
			PopulationData populationData = dataPool.getData(eventType);	
			for (Map.Entry<String, Data> entry : populationData.entrySet()){
				if (entry.getValue().subjects < minSubjectsPerRow){
					deleted.days += entry.getValue().days;
					deleted.events += entry.getValue().events;
					deleted.subjects++;
				} else {
					List<String> cells = new ArrayList<String>();
					cells.add(eventType);
					String[] keys = entry.getKey().split("\\|");
					for (String key : keys)
						cells.add(key);
					cells.add(Long.toString(entry.getValue().days));
					cells.add(Integer.toString(entry.getValue().subjects));
					cells.add(Integer.toString(entry.getValue().events));
					out.write(cells);
					statistics.subjects++;
					statistics.days+= entry.getValue().days;
					statistics.events += entry.getValue().events;
				}
			}
		}
		//out.writeFooter(deleted.subjects, deleted.days, deleted.events);
		System.out.println("Aggregated table has " + statistics.subjects + " rows, contains " + statistics.days + " days, and " + statistics.events + " events");
		if (deleted.subjects != 0)
			System.out.println("Deleted " + deleted.subjects + " rows with too few subjects, containing " + deleted.days + " days, and " + deleted.events + " events");
		out.close();
	}

	private List<String> generatePopulationCounts() {
		List<String> popCountCells = new ArrayList<String>();
		popCountCells.add("All");
		popCountCells.add("All");
		popCountCells.add("All");
		if (includeYear)
			popCountCells.add("All");
		if (includeMonth)
			popCountCells.add("All");
		popCountCells.add(Long.toString(overall.days));
		popCountCells.add(Integer.toString(overall.subjects));
		popCountCells.add(Integer.toString(overall.events));
		return popCountCells;

	}

	private List<String> generateHeader() {
		List<String> header = new ArrayList<String>();
		header.add("EventType");
		header.add("AgeRange");
		header.add("Gender");
		if (includeYear)
			header.add("Year");
		if (includeMonth)
			header.add("Month");
		header.add("Days");
		header.add("Subjects");
		header.add("Events");
		return header;
	}

	private class PopulationData {
		private Map<String, Data> key2data = new HashMap<String, Data>();
		public void add(String key, long days, int events, int subjects){
			Data data = key2data.get(key);
			if (data == null) {
				data = new Data();
				key2data.put(key, data);
			}
			data.days += days;
			data.events += events;
			data.subjects += subjects;
		}

		public PopulationData copy(){
			PopulationData copy = new PopulationData();
			for (Map.Entry<String, Data> entry : key2data.entrySet())
				copy.key2data.put(entry.getKey(), entry.getValue().copy());
			return copy;
		}

		public Set<Map.Entry<String, Data>> entrySet() {
			return key2data.entrySet();
		}
	}

	private class DataPool {
		private Map<String, PopulationData> event2populationData = new HashMap<String, PopulationData>();
		private PopulationData genericData = new PopulationData();

		public void add(List<Event> events, Set<String> precedingEvents, String key, long days, int subjects){
			CountingSet<String> eventCounts = new CountingSet<String>();
			for (Event event : events)
				eventCounts.add(event.eventType);

			ensureEventsHaveData(eventCounts);
			ensureEventsHaveData(precedingEvents);
			genericData.add(key, days, 0, subjects);
			for (Map.Entry<String, PopulationData> entry : event2populationData.entrySet()){
				if (!precedingEvents.contains(entry.getKey())){
					int eventCount = eventCounts.getCount(entry.getKey());
					entry.getValue().add(key, days, eventCount, subjects);
				}
			}
		}

		private void ensureEventsHaveData(Set<String> events) {
			for (String event : events)
				if (!event.equals("") && event2populationData.get(event) == null)
					event2populationData.put(event,genericData.copy());
		}

		public Set<String> getEventTypes() {
			return event2populationData.keySet();
		}

		public PopulationData getData(String eventType) {
			return event2populationData.get(eventType);
		}
	}

	private class Data {
		long days = 0;
		int subjects = 0;
		int events = 0;
		public Data copy(){
			Data data = new Data();
			data.subjects = subjects;
			data.days = days;
			data.events = events;
			return data;
		}
	}
}
