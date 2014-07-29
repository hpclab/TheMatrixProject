package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.concurrency.BatchProcessingThread;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.MergedDataFileReader;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Generates per drug-event combination data per accumulated exposure.
 * @author schuemie
 *
 */
public class ExposureProfilingModule extends JerboaModule {

	public JerboaModule mergedData;

	/**
	 * Use only the first event of a type per patient.<BR>
	 * default = true
	 */
	public boolean onlyFirstEvent = true;

	/**
	 * Only generate profiles for drug-event combinations where the event appears at least this many times during or after exposure.<BR>
	 * default = 1
	 */
	public int minEventCount = 1;

	/**
	 * If set to true, only events and patient time during exposure will be considered for
	 * the association.<BR>
	 * default = false
	 */
	public boolean duringExposureOnly = false;

	/**
	 * Bins of accumulated exposure. Each bin should be represented as 
	 * three semicolon separated values:
	 * <OL>
	 * <LI>The start number of days of accumulated exposure</LI>
	 * <LI>The end number of days of accumulated exposure</LI>
	 * <LI>The label of the code</LI>
	 * </OL>
	 * For example: 0;7;VS
	 */
	public List<String> exposureBin = new ArrayList<String>();

	private BackgroundRates backgroundRates;
	private Map<ATCCode, CountingSet<String>> atc2eventCounts;
	private Map<ATCCode, ExposureProfile> atc2exposureProfile;
	private List<Code> bins;
	private static final long serialVersionUID = 6078653416407177683L;
	private int nrOfThreads = Runtime.getRuntime().availableProcessors();
	private int threadBatchSize = 100000;
	private List<ExecutionThread> threads;

	public static void main(String[] args){
		//String folder = "/home/data/Simulated/LongTerm/";
		//String folder = "x:/Study5/";
		String folder = "/data/OSIM/Balanced/";
		//FileSorter.sort(folder+"Mergedata.txt", new String[]{"PatientID","Date"});
		ExposureProfilingModule module = new ExposureProfilingModule();
		//module.exposureBin.add("0;30;0-30 days");
		//module.exposureBin.add("30;90;30-90 days");
		//module.exposureBin.add("90;210;90-210 days");
		//module.exposureBin.add("210;999999;210-999999 days");
		int bins = 15;
		int binSize = 30;
		for (int i = 0; i < bins; i++){
			int start = (i*binSize);
			int end = (i!=bins-1?(i*binSize + binSize):999999);
			String newBin = start+";"+end+";"+start+"-"+end+" days";
			System.out.println("  exposureBin = "+newBin);
			module.exposureBin.add(newBin);
		}
		
		module.minEventCount = 1;
		module.onlyFirstEvent = true;
		module.process(folder+"Mergedata.txt",folder+"ExposureProfiles.txt");
	}

	@Override
	protected void runModule(String outputFilename) {
		FileSorter.sort(mergedData.getResultFilename(), new String[]{"PatientID", "Date"});
		process(mergedData.getResultFilename(), outputFilename);
	}

	public void process(String source, String target) {
		bins = parseAndSortCodes(exposureBin);
		getBackgroundRatesAndCountPairs(source);
		prepareExposureProfiles();
		generateExposureProfiles(source);
		saveExposureProfiles(target);
		
		//Dereference data objects:
		backgroundRates = null;
		atc2eventCounts = null;
		atc2exposureProfile = null;
		bins = null;
		threads = null;
	}

	private void saveExposureProfiles(String target) {
		List<Integer> binIndices = getBinIndices();
		WriteCSVFile out = new WriteCSVFile(target);
		out.write(generateHeader());
		long totalPatientTime = backgroundRates.getTotalPatientTime();
		for (Map.Entry<ATCCode, ExposureProfile> atcProfileEntry : atc2exposureProfile.entrySet()){
			ATCCode atc = atcProfileEntry.getKey();
			ExposureProfile profile = atcProfileEntry.getValue();


			for (Map.Entry<String, Integer> eventEntry : profile.eventType2Index.entrySet()){
				String eventType = eventEntry.getKey();
				int eventIndex = eventEntry.getValue();
				long totalExposure = profile.getTotalExposure(eventIndex);
				long totalNonExposure = totalPatientTime - totalExposure;
				int totalEvents = backgroundRates.getTotalEvents(eventType);
				int totalExposedEvents = profile.getTotalExposedEvents(eventIndex);
				int totalNonExposedEvents = totalEvents - totalExposedEvents;

				List<String> cells = new ArrayList<String>();
				cells.add(atc.toString());
				cells.add(eventType);
				cells.add(Long.toString(totalNonExposure)); // Days-Background
				for (int binIndex : binIndices)
					cells.add(Long.toString(profile.days[binIndex][eventIndex])); //Days-bins
				cells.add(Integer.toString(totalNonExposedEvents)); // Events-Background
				for (int binIndex : binIndices)
					cells.add(Integer.toString(profile.events[binIndex][eventIndex])); //Events-bins
				cells.add(Integer.toString(totalNonExposedEvents)); // Expected-Background
				for (int binIndex : binIndices)
					cells.add(Double.toString(profile.expected[binIndex][eventIndex])); //Expected-bins
				cells.add("1"); // RR-Background
				for (int binIndex : binIndices){
					double expected = profile.expected[binIndex][eventIndex];
					if (expected == 0)
						cells.add("0");
					else
						cells.add(Double.toString(profile.events[binIndex][eventIndex]/expected)); //RR-bins			
				}
				out.write(cells);
			}
		}		
		out.close();
	}

	private List<Integer> getBinIndices() {
		List<Integer> indices = new ArrayList<Integer>(bins.size());
		for (Code bin : bins)
			indices.add(bin.index);
		return indices;
	}

	private List<String> generateHeader() {
		List<String> header = new ArrayList<String>();
		header.add("ATC");
		header.add("EventType");

		header.add("Days-Background");
		for (Code bin : bins)
			header.add("Days-" + bin.code);

		header.add("Events-Background");
		for (Code bin : bins)
			header.add("Events-" + bin.code);

		header.add("Expected-Background");
		for (Code bin : bins)
			header.add("Expected-" + bin.code);

		header.add("RR-Background");
		for (Code bin : bins)
			header.add("RR-" + bin.code);

		return header;
	}

	private void generateExposureProfiles(String source) {
		List<MergedData> batch = new ArrayList<MergedData>(threadBatchSize);
		for (MergedData md : new MergedDataFileReader(source)){
			batch.add(md);
			if (batch.size() == threadBatchSize){
				for (ExecutionThread thread : threads){
					thread.setBuffer(batch);
					thread.proceed();
				}
				for (ExecutionThread thread : threads)
					thread.waitUntilFinished();
				batch.clear();
			}
		}
		//Process the remaining data:
		for (ExecutionThread thread : threads){
			thread.setBuffer(batch);
			thread.proceed();
		}
		//Then terminate all threads:
		for (ExecutionThread thread : threads){
			thread.waitUntilFinished();
			thread.terminate();
		}
	}


/*
	private void reportProgress(int newLineCount) {
		long timeTaken = System.currentTimeMillis() - startTime;
		Double remaining = timeTaken * (lineCount-newLineCount)/(double)newLineCount;
		System.out.println("-Processed " + newLineCount + " of " + lineCount + " lines in " + timeTaken+ " ms, " + remaining/3600000 +" hours remaining");
	}
*/


	private void prepareExposureProfiles() {
		atc2exposureProfile = new HashMap<ATCCode, ExposureProfile>();
		int beforeCount = 0;
		int afterCount = 0;
		for (Map.Entry<ATCCode, CountingSet<String>> entry : atc2eventCounts.entrySet()){
			ExposureProfile exposureProfile = new ExposureProfile();
			for (Map.Entry<String, CountingSet.Count> countEntry : entry.getValue().key2count.entrySet()){
				beforeCount++;
				int eventsDuringExposure = countEntry.getValue().count;
				if (eventsDuringExposure >= minEventCount){
					afterCount++;
					exposureProfile.eventType2Index.put(countEntry.getKey(), exposureProfile.eventType2Index.size());
				}
			}
			if (exposureProfile.eventType2Index.size() != 0){
				int eventCount = exposureProfile.eventType2Index.size();
				exposureProfile.days = new long[bins.size()][eventCount];
				exposureProfile.events = new int[bins.size()][eventCount];
				exposureProfile.expected = new double[bins.size()][eventCount];
				for (int i = 0; i < bins.size(); i++){
					for (int j = 0; j < eventCount; j++){
						exposureProfile.days[i][j] = 0;
						exposureProfile.events[i][j] = 0;
						exposureProfile.expected[i][j] = 0;
					}
				}
				atc2exposureProfile.put(entry.getKey(), exposureProfile);
			}
		}

		intializeThreads();
		System.out.println("Total number of drug-event combinations found: " + beforeCount);
		System.out.println("Number of drug-event combinations with at least " + minEventCount + " events: " + afterCount);
		System.out.println("Creating profiles for " + atc2exposureProfile.size() + " drugs");
	}

	private void intializeThreads() {
		/*
		threads = new ArrayList<ExecutionThread>(nrOfThreads);
		List<String> atcs = new ArrayList<String>(atc2exposureProfile.keySet());
		int signalsPerThread = signalCount / 8;
		int assignedSignals = 0;
		for (int i = 0; i < nrOfThreads; i++){
			int target;
			if (i == nrOfThreads - 1)
				target = signalCount;
			else
				 target = (i+1)*signalsPerThread;
			List<String> atcsSubset = new ArrayList<String>();
			while (assignedSignals < target && atcs.size() != 0){
				String atc = atcs.remove(atcs.size()-1);
				atcsSubset.add(atc);
				assignedSignals += atc2exposureProfile.get(atc).eventType2Index.size();
			}
			ExecutionThread thread = new ExecutionThread(atcsSubset);
			threads.add(thread);
		}
		 */
		threads = new ArrayList<ExecutionThread>(nrOfThreads);
		List<ATCCode> atcs = new ArrayList<ATCCode>(atc2exposureProfile.keySet());
		int atcsPerThread = atcs.size() / nrOfThreads;
		for (int i = 0; i < nrOfThreads; i++){
			List<ATCCode> atcsSubset;
			if (i == nrOfThreads-1)
				atcsSubset = atcs.subList(i*atcsPerThread, atcs.size());
			else
				atcsSubset = atcs.subList(i*atcsPerThread, ((i+1)*atcsPerThread));
			ExecutionThread thread = new ExecutionThread(atcsSubset);
			threads.add(thread);
		}
	}

	private void getBackgroundRatesAndCountPairs(String source) {
		backgroundRates = new BackgroundRates();
		atc2eventCounts = new HashMap<ATCCode, CountingSet<String>>();
		String oldPatientID = "";
		Set<ATCCode> pastATCs = new HashSet<ATCCode>();
		for (MergedData md : new MergedDataFileReader(source)){
			//Add to background data:
			if (!md.outsideCohortTime){
				AgeGenderData data = backgroundRates.getData(md.ageRange, md.gender);
				data.days += md.duration;
				for (Event event : md.events)
					if (!onlyFirstEvent || !md.precedingEventTypes.contains(event.eventType))
						data.eventCounts.add(event.eventType);
			}

			//Add to pair count:
			if (!duringExposureOnly){
				if (!md.patientID.equals(oldPatientID)){
					oldPatientID = md.patientID;
					pastATCs.clear();
				}
				pastATCs.addAll(md.atcCodes);
			}
			if (!md.outsideCohortTime){
				for (Event event : md.events)
					if (!onlyFirstEvent || !md.precedingEventTypes.contains(event.eventType))
						for (ATCCode atc : (duringExposureOnly?md.atcCodes:pastATCs)){
							CountingSet<String> eventCounts = atc2eventCounts.get(atc);
							if (eventCounts == null){
								eventCounts = new CountingSet<String>();
								atc2eventCounts.put(atc, eventCounts);
							}
							eventCounts.add(event.eventType);
						}
			}
		}
	}


	private class ExposureProfile {
		public long[][] days; // per bin, per event type
		public int[][] events; // per bin, per event type
		public double[][] expected; // per bin, per event type
		public Map<String,Integer> eventType2Index = new HashMap<String, Integer>();
		public long getTotalExposure(int eventIndex) {
			long total = 0;
			for (int i = 0; i < days.length; i++)
				total += days[i][eventIndex];
			return total;
		}
		public int getTotalExposedEvents(int eventIndex) {
			int total = 0;
			for (int i = 0; i < events.length; i++)
				total += events[i][eventIndex];
			return total;
		}
	}

	private class BackgroundRates {
		private Map<String,AgeGenderData> ageGender2Data = new HashMap<String, AgeGenderData>();
		public AgeGenderData getData(String age, byte gender){
			String ageGender = toKey(age,gender);
			AgeGenderData data = ageGender2Data.get(ageGender);
			if (data == null){
				data = new AgeGenderData();
				ageGender2Data.put(ageGender, data);
			}
			return data;
		}

		public int getTotalEvents(String eventType) {
			int total = 0;
			for (AgeGenderData data : ageGender2Data.values())
				total += data.eventCounts.getCount(eventType);
			return total;
		}

		public long getTotalPatientTime() {
			long total = 0;
			for (AgeGenderData data : ageGender2Data.values())
				total += data.days;
			return total;
		}

		public String toKey(String age, byte gender){
			return age + "\t" + gender;
		}
	}

	private class AgeGenderData{
		long days = 0;
		CountingSet<String> eventCounts = new CountingSet<String>();
	}

	private static class Code{
		long start;
		long end;
		public String code;
		public int index;
		public Code(int startday, int endday, String code){
			start = startday;
			end = endday;
			this.code = code;
		}
	}

	private final class ExecutionThread extends BatchProcessingThread {
		private Set<ATCCode> atcs;
		private String oldPatientID = "";
		private Map<ATCCode, LongCount> atcExposureCounts = new HashMap<ATCCode, LongCount>();
		private List<MergedData> buffer;

		public void setBuffer(List<MergedData> buffer){
			this.buffer = buffer;
		}

		public ExecutionThread(Collection<ATCCode> atcs){
			super();
			this.atcs = new HashSet<ATCCode>(atcs);
		}

		protected void process() {
			for (MergedData md : buffer){
				if (!md.patientID.equals(oldPatientID)){
					oldPatientID = md.patientID;
					atcExposureCounts.clear();
				}

				//First go through ATCs that occur in this period:
				for (ATCCode atc : md.atcCodes)
					if (atcs.contains(atc)){ //only worry about atcs for which we are building a profile
						//Fetch past accumulated exposure:
						LongCount count = atcExposureCounts.get(atc);
						if (count == null){
							count = new LongCount();
							atcExposureCounts.put(atc, count);
						}

						//Find the appropriate bin(s):
						long startAccumulatedExposure = count.count;
						long endAccumulatedExposure = startAccumulatedExposure + md.duration;
						if (!md.outsideCohortTime)
							for (Code bin : bins){
								if (bin.start > endAccumulatedExposure)
									break;
								if (bin.end > startAccumulatedExposure){
									long start = Math.max(bin.start, startAccumulatedExposure);
									long absoluteStart = start+md.start-startAccumulatedExposure;
									long duration = Math.min(bin.end, endAccumulatedExposure) - start;
									addToProfile(bin.index, atc, md.ageRange, md.gender, absoluteStart, duration, md.events, md.precedingEventTypes);
								}
							}
						//Add this exposure to the accumulated exposure:
						count.count = endAccumulatedExposure;
					}
				if (!duringExposureOnly && !md.outsideCohortTime){
					//Then go through ATCs that occurred in previous periods:
					for (Map.Entry<ATCCode, LongCount> entry : atcExposureCounts.entrySet()){
						ATCCode atc = entry.getKey();
						if (!md.atcCodes.contains(atc)){ //ATCS that occur in this period were handled above
							long accumulatedExposure = entry.getValue().count;
							for (Code bin : bins){
								if (bin.start > accumulatedExposure)
									break;
								if (bin.end > accumulatedExposure){
									addToProfile(bin.index, atc, md.ageRange, md.gender, md.start, md.duration, md.events, md.precedingEventTypes);
								}
							}
						}
					}
				}
			}
			buffer = null;
		}  

		private void addToProfile(int binIndex, ATCCode atc, String age, byte gender, long start, long duration, List<Event> events, Set<String> precedingEventTypes){
			ExposureProfile profile = atc2exposureProfile.get(atc);
			if (profile != null){
				addExpectedEventsAndDays(profile, binIndex, age, gender, duration, precedingEventTypes);
				
				//Add observed events:
				for (Event event : events)
					if (event.date > start && event.date <= start+duration){
						Integer eventIndex = profile.eventType2Index.get(event.eventType);
						if (eventIndex != null)
							if (!onlyFirstEvent || !precedingEventTypes.contains(event.eventType))
								profile.events[binIndex][eventIndex]++;
					}
			}
		}

		private void addExpectedEventsAndDays(ExposureProfile profile, int binIndex, String age, byte gender, long duration, Set<String> precedingEventTypes) {
			AgeGenderData data = backgroundRates.ageGender2Data.get(backgroundRates.toKey(age,gender));
			for (Map.Entry<String,Integer> entry : profile.eventType2Index.entrySet())
				if (!onlyFirstEvent || !precedingEventTypes.contains(entry.getKey())){ //If only looking at first event, after first event stop adding expected events and days!
					profile.days[binIndex][entry.getValue()] += duration;
					if (data != null){
						int eventCount = data.eventCounts.getCount(entry.getKey());
						double expected = eventCount * duration / (double)data.days;
						profile.expected[binIndex][entry.getValue()]+= expected;
					}
				}
		}
	}


private List<Code> parseAndSortCodes(List<String> codesStrings) {
	List<Code> codes = new ArrayList<Code>();
	for (String row : codesStrings){
		String[] cols = row.split(";");
		int startDay = Integer.parseInt(cols[0]);
		int endDay = Integer.parseInt(cols[1]);
		String label  = cols[2];
		Code code = new Code(startDay, endDay, label);
		codes.add(code);
	}

	Collections.sort(codes, new Comparator<Code>(){

		@Override
		public int compare(Code arg0, Code arg1) {
			Long start0 = arg0.start;
			return start0.compareTo(arg1.start);
		}});

	int binCount = 0;
	for (Code bin : codes)
		bin.index = binCount++;
	return codes;
}

private final class LongCount {
	long count = 0;
}
}
