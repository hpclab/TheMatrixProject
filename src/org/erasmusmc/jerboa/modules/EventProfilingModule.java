package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.calculations.FastStratifiedExact;
import org.erasmusmc.jerboa.calculations.PersonTimeData;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEventIterator;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Generates a histogram of either prescription starts or exposure days centered around the event date. If relativeRisks is not specified, this is done 
 * for all drug-event combinations, else only for the drug-event combinations in the relative risk file. This file is the basis of the LEOPARD method.
 * @author schuemie
 *
 */
public class EventProfilingModule extends JerboaModule {

	public JerboaModule prescriptions;
	public JerboaModule patients;
	public JerboaModule events;
	public JerboaModule relativeRisks;

	/**
	 * Size of the window (days) around the event date.<BR>
	 * default = 51 
	 */
	public int dataPointCount = 51;

	/**
	 * Determines whether recurrent events should be considered as well.<BR>
	 * default = true
	 */
	public boolean deleteAfterFirst = true;

	/**
	 * Type of profiles to be generated. There are two types of profiles:<br>
	 * exposure - Whether the patient was exposed to the drug at the point in time.<br>
	 * prescriptionStart - whether a prescription was started at the point in time.<br>
	 * default = prescriptionStart
	 */
	public String profileType = "prescriptionStart";

	/**
	 * Add the prescription profiles to the output table.<BR>
	 * default = true
	 */
	public boolean outputProfiles = true;

	/**
	 * Compute and output the p value of the binomial test of whether prescriptions occur more often
	 * after the event than before.<BR>
	 * default = true
	 */
	public boolean outputP = true;

	/**
	 * If  the outputP is set to true, a column will be added that marks the signal as 'protopathic bias' when the p-value is lower than this threshold value.<BR>
	 * default = 0.5
	 */
	public double thresholdP = 0.5;

	/**
	 * If an events column exists in the input file, only consider signals where this column has this minimum value.<BR>
	 * default = 1
	 */
	public int minEvents = 1;

	private Map<String, Map<ATCCode, int[]>> eventType2Atc2Profile;
	private CountingSet<String> eventTypeCount;
	private int precedingDataPointCount;
	private int followingDataPointCount;
	private static final long serialVersionUID = -9117287768612883281L;
	private static int exposureType = 0;
	private static int prescriptionType = 1;
	private int profileTypeID = exposureType;
	private static FastStratifiedExact fastStratifiedExact = new FastStratifiedExact();
	private boolean predefinedPairs;

	public static void main(String[] args) {
		EventProfilingModule module = new EventProfilingModule();
		module.profileType = "prescriptionStart";
		module.outputP = true;
		module.outputProfiles = true;
		module.dataPointCount = 51;
		String folder = "x:/EUADR Gold/";
		module.process(null,folder+"Prescriptions.txt", folder+"Patients.txt", folder+"Events.txt", folder+"LEOPARD.txt");
	}

	@Override
	protected void runModule(String outputFilename) {
		FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID","Date"});
		FileSorter.sort(patients.getResultFilename(), "PatientID");
		FileSorter.sort(events.getResultFilename(), "PatientID");
		process(relativeRisks==null?null:relativeRisks.getResultFilename(),prescriptions.getResultFilename(), patients.getResultFilename(), events.getResultFilename(), outputFilename);
	}

	public void process(String rrSource, String sourcePrescriptions,	String sourcePatients, String sourceEvents, String outputFile) {
		if (profileType.toLowerCase().equals("exposure"))
			profileTypeID = exposureType;
		else
			profileTypeID = prescriptionType;

		if (rrSource==null)
			predefinedPairs = false;
		else
			predefinedPairs = true;

		precedingDataPointCount = (dataPointCount - 1) / 2;
		followingDataPointCount = dataPointCount - precedingDataPointCount;
		eventTypeCount = new CountingSet<String>();
		eventType2Atc2Profile = new HashMap<String, Map<ATCCode,int[]>>();
		if (predefinedPairs)
			loadSignals(rrSource);

		PatientPrescriptionEventIterator iterator = new PatientPrescriptionEventIterator(sourcePrescriptions, sourcePatients, sourceEvents);
		while (iterator.hasNext()){
			ProgressHandler.reportProgress();
			processPatient(iterator.next());
		}

		if (predefinedPairs)
			addProfilesToInputFile(rrSource, outputFile);
		else
			outputProfiles(outputFile);

		//Dereference data objects:
		eventType2Atc2Profile = null;
		eventTypeCount = null;
	}

	private void outputProfiles(String outputFile) {
		WriteCSVFile out = new WriteCSVFile(outputFile);
		List<String> header = new ArrayList<String>();
		header.add("ATC");
		header.add("EventType");
		header.addAll(generateAdditionalHeaders());
		out.write(header);
		for (String eventType : eventType2Atc2Profile.keySet()){
			Map<ATCCode, int[]> atc2Profile = eventType2Atc2Profile.get(eventType);	
			for (ATCCode atc : atc2Profile.keySet()){
				List<String> cells = new ArrayList<String>();
				cells.add(atc.toString());
				cells.add(eventType);
				int[] profile = atc2Profile.get(atc);
				cells.addAll(generateAdditionalCells(profile, eventType));
				out.write(cells);
			}
		}
		out.close();
	}

	private void addProfilesToInputFile(String inputFile, String outputFile) {		
		WriteCSVFile out = new WriteCSVFile(outputFile);

		Iterator<List<String>> iterator = new ReadCSVFile(inputFile).iterator();
		List<String> header = iterator.next();
		int atcCol = header.indexOf("ATC");
		int eventTypeCol = header.indexOf("EventType");
		header.addAll(generateAdditionalHeaders());
		out.write(header);

		while (iterator.hasNext()){
			List<String> cols = iterator.next();
			ATCCode atc = new ATCCode(cols.get(atcCol));
			String eventType = cols.get(eventTypeCol);

			Map<ATCCode, int[]> atc2Profile = eventType2Atc2Profile.get(eventType);
			boolean output = false;
			if (atc2Profile != null){
				int[] profile = atc2Profile.get(atc);
				if (profile != null){
					output = true;
					cols.addAll(generateAdditionalCells(profile, eventType));
				}
			} 
			if (!output) {//No LEOPARD scores, add empty variables
				if (outputP){
					cols.add("");
					cols.add("0");
				}
				if (outputProfiles){
					cols.add("");
					for (int i = 0; i < dataPointCount; i++)
						cols.add("");
				}
			}
			out.write(cols);
		}
		out.close();
	}

	private List<String> generateAdditionalCells(int[] profile, String eventType) {
		List<String> cells = new ArrayList<String>();	

		if (outputP){
			double p = calculateP(profile);
			cells.add(Double.toString(p));
			cells.add((p<thresholdP)?"1":"0");
		}
		if (outputProfiles){
			cells.add(Integer.toString(eventTypeCount.getCount(eventType)));
			for (int i = 0; i < dataPointCount; i++)
				cells.add(Integer.toString(profile[i]));
		}
		return cells;
	}

	public static double calculateP(int[] datapoints) {
		int dataPointCount = datapoints.length;
		int precedingDataPointCount = (dataPointCount - 1) / 2;
		int beforeCount = 0;
		for (int i = 0; i < precedingDataPointCount; i++)
			beforeCount += datapoints[i];
		int afterCount = 0;
		for (int i = precedingDataPointCount; i < dataPointCount; i++)
			afterCount += datapoints[i];
		//Abuse our fast exact test routine for p value calculation:
		Map<String, PersonTimeData> key2dataCases = new HashMap<String, PersonTimeData>();
		Map<String, PersonTimeData> key2dataBackground = new HashMap<String, PersonTimeData>();
		PersonTimeData dataCases = new PersonTimeData();
		dataCases.days = 1;
		dataCases.events = afterCount;
		PersonTimeData dataBackground = new PersonTimeData();
		dataBackground.days = 2;
		dataBackground.events = beforeCount + afterCount;
		key2dataCases.put("", dataCases);
		key2dataBackground.put("", dataBackground);
		return fastStratifiedExact.calculateExactStats(key2dataCases, key2dataBackground).upFishPVal;
	}

	private void processPatient(PatientPrescriptionEvent patientPrescriptionEvent) {
		Collections.sort(patientPrescriptionEvent.events);
		if (deleteAfterFirst)
			removeAllButFirst(patientPrescriptionEvent.events);
		Patient patient = patientPrescriptionEvent.patient;
		for (Event event : patientPrescriptionEvent.events){
			if (event.date >= patient.startdate && event.date < patient.enddate){
				eventTypeCount.add(event.eventType);
				addToProfile(event, patientPrescriptionEvent.prescriptions);
			}
		}
	}

	private void addToProfile(Event event, List<Prescription> prescriptions) {
		Map<ATCCode, int[]> atc2Profile = eventType2Atc2Profile.get(event.eventType);
		if (atc2Profile == null && !predefinedPairs){
			atc2Profile = new HashMap<Prescription.ATCCode, int[]>(1);
			eventType2Atc2Profile.put(event.eventType, atc2Profile);
		}

		if (atc2Profile != null)
			for (Prescription prescription : prescriptions) {
				if (profileTypeID == exposureType){
					matchToExposure(prescription, event, atc2Profile);
				} else {
					matchToPrescriptionStart(prescription, event, atc2Profile);
				}
			}
	}

	private void matchToExposure(Prescription prescription, Event event,Map<ATCCode, int[]> atc2Profile) {
		if (prescription.getEnd() >= event.date - precedingDataPointCount && prescription.start < event.date + followingDataPointCount){
			for (ATCCode atc : prescription.atcCodes){
				int[] profile = getProfile(atc2Profile,atc);
				if (profile != null){
					int startInterval = Math.max(0,(int)(prescription.start - (event.date - precedingDataPointCount)));
					int endInterval = Math.min(dataPointCount,(int)(prescription.getEnd() - (event.date - precedingDataPointCount)));			  	
					for (int i = startInterval; i < endInterval; i++)
						profile[i]++;
				}
			}		
		}
	}

	private int[] getProfile(Map<ATCCode, int[]> atc2Profile, ATCCode atc) {
		int[] profile = atc2Profile.get(atc);
		if (profile == null && !predefinedPairs){
			profile = new int[dataPointCount];
			atc2Profile.put(atc, profile);
		}
		return profile;
	}

	private void matchToPrescriptionStart(Prescription prescription, Event event,	Map<ATCCode, int[]> atc2Profile) {
		if (prescription.start >= event.date - precedingDataPointCount && prescription.start < event.date + followingDataPointCount) 
			for (ATCCode atc : prescription.atcCodes){
				int[] profile = getProfile(atc2Profile,atc);
				if (profile != null)
					profile[(int)(prescription.start - event.date + precedingDataPointCount)]++;
			}		
	}

	private void removeAllButFirst(List<Event> events) {
		Set<String> precedingEventTypes = new HashSet<String>();
		Iterator<Event> iterator = events.iterator();
		while (iterator.hasNext())
			if (!precedingEventTypes.add(iterator.next().eventType))
				iterator.remove();
	}

	private void loadSignals(String rrSource) {
		int signalCount = 0;
		boolean first = true;
		int atcCol = -1;
		int eventTypeCol = -1;
		int eventsCol = -1;
		for (List<String> cols : new ReadCSVFile(rrSource)){
			if (first){
				first = false;
				atcCol = cols.indexOf("ATC");
				eventTypeCol = cols.indexOf("EventType");
				eventsCol = cols.indexOf("Events");
			} else {
				if (eventsCol != -1 && Integer.parseInt(cols.get(eventsCol)) < minEvents)
					continue;
				ATCCode atc = new ATCCode(cols.get(atcCol));
				String eventType = cols.get(eventTypeCol);
				Map<ATCCode, int[]> atc2Profile = eventType2Atc2Profile.get(eventType);
				if (atc2Profile == null){
					atc2Profile = new HashMap<ATCCode, int[]>(1);
					eventType2Atc2Profile.put(eventType, atc2Profile);
				}
				atc2Profile.put(atc, new int[dataPointCount]);
				signalCount++;
			}
		}
		System.out.println("Creating profiles for " + signalCount + " drug - event combinations");
	}

	private List<String> generateAdditionalHeaders() {
		List<String> cells = new ArrayList<String>();
		if (outputP){
			cells.add("P(LEOPARD)");
			cells.add("Protopathic bias(LEOPARD)");
		}
		if (outputProfiles){
			cells.add("Total Event Count");

			for (int i = 0; i < dataPointCount; i++)
				cells.add("Datapoint (" + (i-precedingDataPointCount) + " days)");
		}
		return cells;
	}
}
