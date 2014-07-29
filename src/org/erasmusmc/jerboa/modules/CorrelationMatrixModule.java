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

import org.erasmusmc.collections.Pair;
import org.erasmusmc.collections.SortedListSet;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.DummyIterator;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEventIterator;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.utilities.CountingSetLong;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Module for generating statistics that can be used to compute a correlation matrix between all variables in the input files.
 * @author schuemie
 *
 */
public class CorrelationMatrixModule extends JerboaModule{

	public JerboaModule patients;
	public JerboaModule events;

	/**
	 * Number of days in the interval into which the patient time is divided. Intervals are computed relative to the data of birth<BR>
	 * default = 365
	 */
	public int interval = 365;

	/**
	 * Use only the first occurrence of an event for a patient? If so, patient time after an event is censored for that event.<BR>
	 * default = true
	 */
	public boolean firstEventOnly = true;

	private Map<String, Data> event2Data;
	private Map<Pair<String,String>, PairData> pair2Data;
	private long totalIntervalCount;
	private static final Comparator<String> stringComparator = new GenericComparator<String>();
	private static final long serialVersionUID = -5643990787648547347L;

	public static void main(String[] args){
		//String folder = "/home/data/simulated/correlations/";
		//String folder = "x:/study5/";
		//String folder = "x:/CorrelationMatrix/";
		//CorrelationMatrixModule module = new CorrelationMatrixModule();
		//module.process(folder+"Patients.txt", folder+"Events.txt", folder+"CorrelationMatrix.csv");

		String folder = "x:/CorrelationMatrix/";
		CorrelationMatrixModule module = new CorrelationMatrixModule();
		for (int i = 0; i < 4; i++){
			if (i==0)
				module.process(folder+"Patients"+i+".txt", folder+"Events0Noisy.txt", folder+"CorrelationMatrix"+i+".csv");
			else
			module.process(folder+"Patients"+i+".txt", folder+"Events.txt", folder+"CorrelationMatrix"+i+".csv");
		}
	}

	@Override
	protected void runModule(String outputFilename) {
		FileSorter.sort(patients.getResultFilename(), new String[]{"PatientID","Startdate"});
		FileSorter.sort(events.getResultFilename(), new String[]{"PatientID","Date"});
		process(patients.getResultFilename(), events.getResultFilename(),outputFilename);
	}

	public void process(String sourcePatients, String sourceEvents, String target) {
		event2Data = new HashMap<String, Data>();
		pair2Data = new HashMap<Pair<String,String>, PairData>();
		totalIntervalCount = 0;
		Iterator<Patient> patientIterator = new PatientFileReader(sourcePatients).iterator(); 
		Iterator<Prescription> prescriptionIterator = new DummyIterator<Prescription>();
		Iterator<Event> eventIterator = new EventFileReader(sourceEvents).iterator();
		PatientPrescriptionEventIterator iterator = new PatientPrescriptionEventIterator(patientIterator, prescriptionIterator, eventIterator);
		while (iterator.hasNext())
			processPatient(iterator.next());

		writeOutput(target);
	}

	private void writeOutput(String filename) {
		WriteCSVFile out = new WriteCSVFile(filename);
		out.write(generateHeader());
		Set<String> continousVars = new HashSet<String>();
		Set<String> binaryVars = new HashSet<String>();
		for (String var : event2Data.keySet())
			if (var.contains(":"))
				continousVars.add(var.substring(0,var.indexOf(":")));
			else
				binaryVars.add(var);

		List<String> vars = new ArrayList<String>(binaryVars);
		vars.addAll(continousVars);
		Collections.sort(vars);
		for (int i = 0; i < vars.size(); i++)
			for (int j = i+1; j < vars.size(); j++)
				//out.write(generateRow(vars.get(i), vars.get(j), continousVars.contains(vars.get(i)), continousVars.contains(vars.get(j))));
				outputSeries(vars.get(i), vars.get(j), continousVars.contains(vars.get(i)), continousVars.contains(vars.get(j)),out);
				out.close();
	}

	/*
	private List<String> generateRow(String varA, String varB,	boolean continuousA, boolean continuousB) {
		Series series = new Series();
		for (VarLevel varLevelA : generateVarLevels(varA, continuousA))
			for (VarLevel varLevelB : generateVarLevels(varB, continuousB)){
				DataRange dataRange = new DataRange();
				dataRange.valueA = varLevelA.level;
				dataRange.valueB = varLevelB.level;
				if (continuousA && continuousB)
					dataRange.count = getCount(varLevelA.id, varLevelB.id);//pair2Data.get(new Pair<String,String>(varLevelA.id,varLevelB.id)).count;
				else if (!continuousA && !continuousB){
					if (varLevelA.level == 0d && varLevelB.level == 0d){
						PairData pairData = pair2Data.get(new Pair<String,String>(varLevelA.var,varLevelB.var));
						long censored = event2Data.get(varLevelA.var).censoredCount + event2Data.get(varLevelB.var).censoredCount - ((pairData == null)?0:pairData.censoredCount);
						dataRange.count = totalIntervalCount - censored - getCountAnotB(varA, varB) - getCountAnotB(varB,varA) - getCount(varA,varB);
					} else if (varLevelA.level == 0d && varLevelB.level == 1d){
						dataRange.count = getCountAnotB(varB,varA);
					} else if (varLevelA.level == 1d && varLevelB.level == 0d){
						dataRange.count = getCountAnotB(varA,varB);
					} else if (varLevelA.level == 1d && varLevelB.level == 1d){
						dataRange.count = getCount(varLevelA.id, varLevelB.id);//dataRange.count = pair2Data.get(new Pair<String,String>(varLevelA.id,varLevelB.id)).count;
					}
				} else if (continuousA && !continuousB){
					if (varLevelB.level == 0d){
						dataRange.count = getCountAnotB(varLevelA.id,varLevelB.id);
					} else {
						dataRange.count = getCount(varLevelA.id,varLevelB.id);
					}
				} else if (!continuousA && continuousB){
					if (varLevelA.level == 0d){
						dataRange.count = getCountAnotB(varLevelB.id,varLevelA.id);
					} else {
						dataRange.count = getCount(varLevelA.id,varLevelB.id);
					}
				}
				if (dataRange.count != 0)
					series.add(dataRange);
			}

		double r = Correlation.correlation(series);
		List<String> cells = new ArrayList<String>();
		cells.add(varA);
		cells.add(varB);
		cells.add(continuousA?"":Long.toString(countEvents(series,true,false)));
		cells.add(continuousB?"":Long.toString(countEvents(series,false,true)));
		cells.add((continuousA || continuousB)?"":Long.toString(countEvents(series,true,true)));
		cells.add(Double.toString(r));
		cells.add(Double.toString(Correlation.twoSidedP(Correlation.rToZTransform(r))));
		cells.add(Long.toString(series.totalCount()));

		return cells;
	}
*/
	private void outputSeries(String varA, String varB,	boolean continuousA, boolean continuousB, WriteCSVFile out) {
		Series series = new Series();
		for (VarLevel varLevelA : generateVarLevels(varA, continuousA))
			for (VarLevel varLevelB : generateVarLevels(varB, continuousB)){
				DataRange dataRange = new DataRange();
				dataRange.valueA = varLevelA.level;
				dataRange.valueB = varLevelB.level;
				if (continuousA && continuousB)
					dataRange.count = getCount(varLevelA.id, varLevelB.id);//pair2Data.get(new Pair<String,String>(varLevelA.id,varLevelB.id)).count;
				else if (!continuousA && !continuousB){
					if (varLevelA.level == 0d && varLevelB.level == 0d){
						PairData pairData = pair2Data.get(new Pair<String,String>(varLevelA.var,varLevelB.var));
						long censored = event2Data.get(varLevelA.var).censoredCount + event2Data.get(varLevelB.var).censoredCount - ((pairData == null)?0:pairData.censoredCount);
						dataRange.count = totalIntervalCount - censored - getCountAnotB(varA, varB) - getCountAnotB(varB,varA) - getCount(varA,varB);
					} else if (varLevelA.level == 0d && varLevelB.level == 1d){
						dataRange.count = getCountAnotB(varB,varA);
					} else if (varLevelA.level == 1d && varLevelB.level == 0d){
						dataRange.count = getCountAnotB(varA,varB);
					} else if (varLevelA.level == 1d && varLevelB.level == 1d){
						dataRange.count = getCount(varLevelA.id, varLevelB.id);//dataRange.count = pair2Data.get(new Pair<String,String>(varLevelA.id,varLevelB.id)).count;
					}
				} else if (continuousA && !continuousB){
					if (varLevelB.level == 0d){
						dataRange.count = getCountAnotB(varLevelA.id,varLevelB.id);
					} else {
						dataRange.count = getCount(varLevelA.id,varLevelB.id);
					}
				} else if (!continuousA && continuousB){
					if (varLevelA.level == 0d){
						dataRange.count = getCountAnotB(varLevelB.id,varLevelA.id);
					} else {
						dataRange.count = getCount(varLevelA.id,varLevelB.id);
					}
				}
				if (dataRange.count != 0)
					series.add(dataRange);
			}

		for (DataRange dataRange : series){
			List<String> cells = new ArrayList<String>();
			cells.add(varA);
			cells.add(varB);
			cells.add(Double.toString(dataRange.valueA));
			cells.add(Double.toString(dataRange.valueB));
			cells.add(Long.toString(dataRange.count));
			out.write(cells);
		}
	}

	/*
	private long countEvents(Series series, boolean a, boolean b) {
		long count = 0;
		for (DataRange dataRange : series)
			if ((a && !b && dataRange.valueA == 1d) || (!a && b && dataRange.valueB == 1d) || (a && b && dataRange.valueA == 1d && dataRange.valueB == 1d))
				count += dataRange.count;
		return count;
	}
*/
	private long getCount(String varA, String varB) {
		PairData pairData = pair2Data.get(new Pair<String,String>(varA,varB));
		return (pairData == null)?0:pairData.count;
	}

	private long getCountAnotB(String varA, String varB){
		Data data = event2Data.get(varA);
		String a = varA;
		String b = varB;
		if (varA.compareTo(varB) > 0){
			a = varB;
			b = varA;
		}
		return data.count - data.otherCensoredCount.getCount(varB) - getCount(a,b); 
	}

	private List<VarLevel> generateVarLevels(String var, boolean continuous) {
		List<VarLevel> varLevels = new ArrayList<VarLevel>();
		if (continuous){ // continuous
			for (String event : event2Data.keySet()){
				if (event.contains(":")){
					String[] parts = event.split(":");
					if (parts[0].equals(var)){
						VarLevel varLevel = new VarLevel();
						varLevel.var = var;
						varLevel.level = Double.parseDouble(parts[1]);
						varLevel.id = event;
						varLevels.add(varLevel);
					}
				}

			}
		} else { //binary
			VarLevel varLevel0 = new VarLevel();
			varLevel0.var = var;
			varLevel0.level = 0d;
			varLevel0.id = var;
			varLevels.add(varLevel0);
			VarLevel varLevel1 = new VarLevel();
			varLevel1.var = var;
			varLevel1.level = 1d;
			varLevel1.id = var;
			varLevels.add(varLevel1);			

		}
		return varLevels;
	}

	private List<String> generateHeader() {
		List<String> header = new ArrayList<String>();
		header.add("VarA");
		header.add("VarB");
		header.add("ValueA");
		header.add("ValueB");
		header.add("Count");
		/*header.add("CountA");
		header.add("CountB");
		header.add("CountAandB");
		header.add("Correlation");
		header.add("p-value");
		header.add("n");
		*/
		return header;
	}

	private void processPatient(PatientPrescriptionEvent patientPrescriptionEvent) {
		SortedListSet<String> seenEventTypes = new SortedListSet<String>(stringComparator);
		Patient patient = patientPrescriptionEvent.patient;
		long age = (patient.startdate - patient.birthdate) / interval;
		long ageStartDate = patient.birthdate + age*interval;
		long ageEndDate = ageStartDate + interval;

		// Add all events that occurred before the start date to seenEventTypes
		if (firstEventOnly)
			for (Event event : patientPrescriptionEvent.events)
				if (event.date < patient.startdate)
					seenEventTypes.add(event.eventType);

		while (ageStartDate < patient.enddate){
			totalIntervalCount++;
			long start = Math.max(patient.startdate, ageStartDate);
			long end = Math.min(patient.enddate, ageEndDate);

			// Record censored time of previously seen events
			if (firstEventOnly && seenEventTypes.size() != 0){
				for (String event : seenEventTypes)
					getData(event).censoredCount++;
				List<String> seenEventsList = seenEventTypes.getSortedList();
				for (int i = 0; i < seenEventsList.size(); i++)
					for (int j = i+1; j < seenEventsList.size(); j++)
						getData(new Pair<String,String>(seenEventsList.get(i), seenEventsList.get(j))).censoredCount++;
			}

			SortedListSet<String> cooccurringEvents = new SortedListSet<String>(stringComparator);
			for (Event event : patientPrescriptionEvent.events){
				if (event.date >= start && event.date < end)
					if (!firstEventOnly || !seenEventTypes.contains(event.eventType))
						cooccurringEvents.add(event.eventType);
			}
			List<String> nonRecurringEvents = new ArrayList<String>(cooccurringEvents.getSortedList()); 
			cooccurringEvents.add("AGE:"+age);
			cooccurringEvents.add("MALE:"+((patient.gender==Patient.MALE)?"1":"0"));
			for (String event : cooccurringEvents){
				Data data = getData(event);
				data.count++;
				for (String seenEvent : seenEventTypes)
					data.otherCensoredCount.add(seenEvent);
			}

			List<String> cooccurringEventsList = cooccurringEvents.getSortedList();				
			for (int i = 0; i < cooccurringEventsList.size(); i++)
				for (int j = i+1; j < cooccurringEventsList.size(); j++)
					getData(new Pair<String,String>(cooccurringEventsList.get(i), cooccurringEventsList.get(j))).count++;

			// Store seen events in seenEventTypes
			if (firstEventOnly)
				seenEventTypes.addAll(nonRecurringEvents);
			age++;
			ageStartDate = patient.birthdate + age*interval;
			ageEndDate = ageStartDate + interval;
		}
		ProgressHandler.reportProgress();
	}

	private Data getData(String event) {
		Data data = event2Data.get(event);
		if (data == null){
			data = new Data();
			event2Data.put(event, data);
		}
		return data;
	}

	private PairData getData(Pair<String,String> eventPair) {
		PairData data = pair2Data.get(eventPair);
		if (data == null){
			data = new PairData();
			pair2Data.put(eventPair, data);
		}
		return data;
	}

	private class Data{
		/**
		 * Counts how many times this variable occurred
		 */
		public long count = 0;
		/**
		 * Counts when this variable occurred, but another variable was censored
		 */
		public CountingSetLong<String> otherCensoredCount = new CountingSetLong<String>();

		/**
		 * Counts when this variable was censored
		 */
		public long censoredCount;
	}	

	private class PairData{
		/**
		 * Counts how many time these two variables occurred together
		 */
		public long count = 0;

		/**
		 * Counts when these two variables were both censored
		 */
		public long censoredCount;
	}	


	private static class GenericComparator<T extends Comparable<T>> implements Comparator<T>{
		@Override
		public int compare(T arg0, T arg1) {
			return arg0.compareTo(arg1);
		}	
	}

	private static class VarLevel {
		public String var;
		public double level;
		public String id;
	}
	
	public static class Series extends ArrayList<DataRange>{
		
		private static final long serialVersionUID = -4402840733182251345L;
		
		public void printSeries(){
			List<String> lines = new ArrayList<String>();
			for (DataRange dataRange : this)
				lines.add("A: " + dataRange.valueA + "\tB: " + dataRange.valueB + "\tCount: " + dataRange.count);
			Collections.sort(lines);
			for (String line : lines)
				System.out.println(line);
		}
		
		public long totalCount(){
			long sum = 0;
			for (DataRange dataRange : this)
				sum += dataRange.count;
			return sum;
		}
		
		public void merge(Series series){
			for (DataRange otherDataRange : series){
				boolean matchFound = false;
				for (DataRange dataRange : this)
					if ((dataRange.valueA == otherDataRange.valueA) && (dataRange.valueB == otherDataRange.valueB)){
						dataRange.count += otherDataRange.count;
						matchFound = true;
						break;
					}
				if (!matchFound){
					DataRange dataRange = new DataRange();
					dataRange.valueA = otherDataRange.valueA;
					dataRange.valueB = otherDataRange.valueB;
					dataRange.count = otherDataRange.count;
					this.add(dataRange);
				}
			}
		}
	}
	
	public static class DataRange {
		public long count;
		public double valueA;
		public double valueB;
	}

}