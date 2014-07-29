package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.erasmusmc.collections.OneToManyList;
import org.erasmusmc.jerboa.dataClasses.CaseOrControl;
import org.erasmusmc.jerboa.dataClasses.CaseOrControl.DrugStats;
import org.erasmusmc.jerboa.dataClasses.CaseOrControl.WindowStats;
import org.erasmusmc.jerboa.dataClasses.CaseOrControlFileWriter;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEventIterator;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.jerboa.modules.matchingCriteria.ChronicDiseaseScorer;
import org.erasmusmc.jerboa.modules.matchingCriteria.DrugCounter;
import org.erasmusmc.jerboa.modules.matchingCriteria.ExternalCaseId;
import org.erasmusmc.jerboa.modules.matchingCriteria.SameATCClass;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.SampleUtilities;
import org.erasmusmc.utilities.StringUtilities;

/**
 * Generates case sets for a case-control type analysis. For every case (event), all controls are identified based
 * on the index date, and a number of optional matching criteria.<BR>
 * mainPrescriptions can be used is the cohort was defined based on prescriptions. The information of the prescription starting on the cohort entry 
 * will be included in the output.
 * @author schuemie
 *
 */
public class CaseControlModule extends JerboaModule {

	public JerboaModule prescriptions;
	public JerboaModule patients;
	public JerboaModule events;
	public JerboaModule mainPrescriptions;

	/**
	 * Specifies the maximum number of controls to be sampled per case.<BR>
	 * default = 2
	 */
	public int controlsPerCase = 2;

	/**
	 * Specifies whether controls should have the same gender as the case.<BR>
	 * default = true
	 */
	public boolean matchOnGender = true;

	/**
	 * Maximum number of days difference in date of birth between case and control.<BR>
	 * default = 365
	 */
	public int maxDifferenceBetweenBirthDates = 365;

	/**
	 * Maximum number of days difference in days in cohort date at index date.<BR>
	 * default = 99999999
	 */
	public int maxDifferenceInTimeInCohort = 99999999;

	/**
	 * Specifies whether controls should have the same Chronic Disease Score as the case.<BR>
	 * default = false
	 */
	public boolean matchOnChronicDiseaseScore = false;

	/**
	 * Specifies whether controls should have the same drug count as the case.<BR>
	 * default = false
	 */
	public boolean matchOnDrugCount = false;

	/**
	 * Specifies whether controls should be matched on an external caseset ID included in the prescriptions file with
	 * prefix CASESET_<BR>
	 * default = false
	 */
	public boolean matchOnExternalCaseSetId = false;

	/**
	 * Specifies whether controls should be matched on exposure to the same ATC class.<BR>
	 * default = false
	 */
	public boolean matchOnATCClass = false;

	/**
	 * Output the calendar year of the index date.<BR>
	 * default = true
	 */
	public boolean outputYear = true;

	/**
	 * Output the calendar month of the index date.<BR>
	 * default = true
	 */
	public boolean outputMonth = true;

	/**
	 * Output the gender of the cases and controls.<BR>
	 * default = true
	 */
	public boolean outputGender = true;

	/** Output the age group of the cases and controls.<BR>
	 * default = true
	 */
	public boolean outputAge = true;

	/**
	 * Output the Chronic Disease score of the cases and controls.<BR>
	 * default = false
	 */
	public boolean outputChronicDiseaseScore = false;

	/**
	 * Output the drug counts for the cases and controls.<BR>
	 * default = false
	 */
	public boolean outputDrugCount = false;

	/**
	 * Output the time the patient has been in the cohort at index date.<BR>
	 * default = false
	 */
	public boolean outputTimeInCohort = false;

	/**
	 * If set to true, for each drug the days since use are added to the table.<BR>
	 * default = false
	 */
	public boolean outputDaysSinceUse = false;

	/**
	 * If set to true, for each drug the days of use (in the window) are added to the table.<BR>
	 * default = false
	 */
	public boolean outputDaysOfUse = false;
	
	/**
	 * If set to true, the patient IDs will be included in the output. Intended for debugging!<BR>
	 * default = false
	 */
	public boolean outputPatientID = false;

	/**
	 * Windows during which exposure will be measured. The format is:<BR>
	 * window start;window end;label;point of reference (e.g. -30;0;current;index)<BR>
	 * point of reference can be:
	 * <UL>
	 *   <LI>index - Index date (time of event)</LI>
	 *   <LI>startOfCohort - Start date of cohort time</LI>
	 * </UL>
	 * For exposure only at the time of event, use an interval from -1 to 0 (e.g. "-1;0;Current;index")	 * 
	 */
	public List<String> timeWindow = new ArrayList<String>();

	/**
	 * Only consider first event of that type for a patient as a case.<BR>
	 * default = true
	 */
	public boolean firstEventOnly = true;

	/**
	 * Seed for the random sampling. Set to -1 to auto-generate.<BR>
	 * default = true
	 */
	public int seed = -1;

	private static final long serialVersionUID = 1991189943261765189L;
	private static enum PointOfReference {index,startOfCohort};
	private List<TimeWindow> timeWindows;
	private static ChronicDiseaseScorer chronicDiseaseScorer = new ChronicDiseaseScorer();
	private static DrugCounter drugCounter = new DrugCounter();
	private static ExternalCaseId externalCaseId = new ExternalCaseId();
	private static SameATCClass sameATCClass = new SameATCClass();
	private int casePatternCount;
	private long controlCount;
	private CasePatternMatcher casePatternMatcher;
	private List<CasePattern> casePatterns;

	public static void main(String[] args){

		String folder = "/home/data/Simulated/SOS/";
		//FileSorter.sort(folder + "ExposureSPtrueMTfalseFPtrueCO30SDtrue.txt", new String[]{"PatientID","Date"});
		//FileSorter.sort(folder + "Cohortentrydate.txt", new String[]{"PatientID","Startdate"});
		//FileSorter.sort(folder + "Firstevents.txt", new String[]{"PatientID","Date"});
		CaseControlModule module = new CaseControlModule();
		module.controlsPerCase = 100;
		module.matchOnGender = true;
		module.maxDifferenceBetweenBirthDates = 365;
		module.maxDifferenceInTimeInCohort = 28;
		module.outputGender = true;
		module.outputAge = true;
		module.outputTimeInCohort = true;
		module.outputDaysSinceUse = true;
		module.outputDaysOfUse = true;
		module.firstEventOnly = true;
		module.timeWindow.add("-365;0;beforeCohort;startOfCohort");
		module.timeWindow.add("-365;0;beforeIndex;index");
		module.timeWindow.add("0;31;afterIndex;index");
	

		module.process(folder + "Concatenatecovariatefiles.txt",folder + "NSAIDcohortentrydate.txt",folder + "Events.txt", folder+"Prescriptions.txt", folder + "CaseSets.txt");

		FileSorter.sort(folder + "CaseSets.txt", new String[]{"CaseSetID","IsCase"});
	}

	protected void runModule(String outputFilename) {
		FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID","Date"});
		FileSorter.sort(patients.getResultFilename(), new String[]{"PatientID","Startdate"});
		FileSorter.sort(events.getResultFilename(), new String[]{"PatientID","Date"});
		if (mainPrescriptions != null)
			FileSorter.sort(mainPrescriptions.getResultFilename(), new String[]{"PatientID","Date"});
		process(prescriptions.getResultFilename(), patients.getResultFilename(), events.getResultFilename(), mainPrescriptions==null?null:mainPrescriptions.getResultFilename(), outputFilename);
	}

	public void process(String sourcePrescriptions, String sourcePatients, String sourceEvents, String sourceMainPrescriptions, String target) {
		parseTimeWindows();
		CaseOrControlFileWriter out = openTarget(target, sourceMainPrescriptions != null);

		findAndOutputCases(sourcePrescriptions, sourcePatients, sourceEvents, sourceMainPrescriptions, out);
		if (controlsPerCase != 0){
			identifyPotentialControls(sourcePrescriptions, sourcePatients, sourceEvents);
			sampleControls();
			outputControls(sourcePrescriptions, sourcePatients, sourceEvents, sourceMainPrescriptions, out);
		}

		out.close();

		//Dereference objects:
		timeWindows = null;
		casePatternMatcher = null;
		casePatterns = null;
	}

	private void parseTimeWindows() {
		timeWindows = new ArrayList<TimeWindow>();
		for (String row : timeWindow){
			String[] cells = row.split(";");
			timeWindows.add(new TimeWindow(Integer.parseInt(cells[0]), Integer.parseInt(cells[1]), cells[2], cells.length>3?cells[3]:PointOfReference.index.toString()));
		}
	}

	private CaseOrControlFileWriter openTarget(String filename, boolean outputMainPrescription) {
		List<String> variables = new ArrayList<String>();
		if (outputYear)
			variables.add("Year");

		if (outputMonth)
			variables.add("Month");		

		if (outputGender)
			variables.add("Gender");

		if (outputAge)
			variables.add("Age");

		if (outputChronicDiseaseScore)
			variables.add("ChronicDiseaseScore");

		if (outputDrugCount)
			variables.add("DrugCount");

		if (outputTimeInCohort)
			variables.add("DaysInCohort");

		List<String> windows = new ArrayList<String>();
		for (TimeWindow timeWindow : timeWindows)
			windows.add(timeWindow.label);
		CaseOrControlFileWriter.outputPatientID = outputPatientID;
		return new CaseOrControlFileWriter(filename, variables, windows, outputDaysSinceUse, outputDaysOfUse, outputMainPrescription);
	}


	
	private void findAndOutputCases(String sourcePrescriptions, String sourcePatients,	String sourceEvents, String sourceMainPrescriptions, CaseOrControlFileWriter out) {
		System.out.println("Fetching cases");
		casePatternCount = 0;
		casePatternMatcher = new CasePatternMatcher();
		casePatterns = new ArrayList<CasePattern>();
		Iterator<Patient> patientIterator = new PatientFileReader(sourcePatients).iterator();
		Iterator<Prescription> prescriptionIterator = new PrescriptionFileReader(sourcePrescriptions).iterator();
		Iterator<Event> eventIterator = new EventFileReader(sourceEvents).iterator();
		PatientPrescriptionEventIterator iterator = new PatientPrescriptionEventIterator(patientIterator, prescriptionIterator, eventIterator);
		MainPrescriptionMatcher mainPrescriptionMatcher = new MainPrescriptionMatcher(sourceMainPrescriptions);
		while (iterator.hasNext()){
			PatientPrescriptionEvent patientPrescriptionEvent = iterator.next();
			addCases(patientPrescriptionEvent,mainPrescriptionMatcher.findMatchingPrescription(patientPrescriptionEvent.patient), out);
		}
		casePatternMatcher.prepareIndex();
		System.out.println("- Found " + casePatternCount + " cases");
	}

	private class MainPrescriptionMatcher {
		private Prescription prescription;
		private Iterator<Prescription> mainPrescriptionIterator;
		
		public MainPrescriptionMatcher(String sourceMainPrescriptions){
			mainPrescriptionIterator = sourceMainPrescriptions==null?null:new PrescriptionFileReader(sourceMainPrescriptions).iterator();
			prescription = mainPrescriptionIterator==null?null:mainPrescriptionIterator.hasNext()?mainPrescriptionIterator.next():null;
		}

		public Prescription findMatchingPrescription(Patient patient) {
			//Skip to mainPrescriptions belonging to this patient:
			String patientID = patient.patientID;
			while (prescription != null && prescription.patientID.compareTo(patientID)<0)
				if  (mainPrescriptionIterator.hasNext())
					prescription = mainPrescriptionIterator.next();  
				else
					prescription = null;

			//Attempt to find prescription with startdate = cohort start date
			while (prescription != null && prescription.patientID.equals(patientID)){
				if (prescription.start == patient.startdate)	
					return prescription;
				if (mainPrescriptionIterator.hasNext())
					prescription = mainPrescriptionIterator.next();
				else
					prescription = null;
			}
			return null;
		}
	}

	private void addCases(PatientPrescriptionEvent patientPrescriptionEvent, Prescription mainPrescription, CaseOrControlFileWriter out) {
		if (patientPrescriptionEvent.events.size() == 0)
			return;
		Set<String> seenEventTypes = new HashSet<String>();
		for (Event event : patientPrescriptionEvent.events){
			if (!firstEventOnly || seenEventTypes.add(event.eventType)){
				if (event.date >= patientPrescriptionEvent.patient.startdate && event.date < patientPrescriptionEvent.patient.enddate){
					CasePattern casePattern = new CasePattern(event, patientPrescriptionEvent, casePatternCount++);
					casePatternMatcher.add(casePattern);
					casePatterns.add(casePattern);
					if (mainPrescription == null)
						System.out.println("asdfsdaf");
					writeCaseOrControl(out, casePattern, patientPrescriptionEvent, mainPrescription, true);
				}
			}
		}
	}

	private void identifyPotentialControls(String sourcePrescriptions,String sourcePatients, String sourceEvents) {
		System.out.println("Identifying potential controls");
		controlCount = 0;
		Iterator<Patient> patientIterator = new PatientFileReader(sourcePatients).iterator();
		Iterator<Prescription> prescriptionIterator = new PrescriptionFileReader(sourcePrescriptions).iterator();
		Iterator<Event> eventIterator = new EventFileReader(sourceEvents).iterator();
		PatientPrescriptionEventIterator iterator = new PatientPrescriptionEventIterator(patientIterator, prescriptionIterator, eventIterator);
		while (iterator.hasNext()){
			PatientPrescriptionEvent patientPrescriptionEvent = iterator.next();
			for (CasePattern casePattern : casePatternMatcher.findCases(patientPrescriptionEvent)){
				if (!patientPrescriptionEvent.patient.patientID.equals(casePattern.patient.patientID)){
					casePattern.potentialControlCount++;
					controlCount++;
				}
			}
		}
		System.out.println("- Found " + controlCount + " potential controls. Average " + (controlCount/(double)casePatternCount) + " controls per case");
	}

	private void sampleControls() {
		System.out.println("Sampling controls");
		Random random;
		if (seed == -1)
			random = new Random();
		else
			random = new Random(seed);
		controlCount = 0;
		for (CasePattern casePattern : casePatterns){
			int numberOfControls = Math.min(controlsPerCase, casePattern.potentialControlCount);
			if (numberOfControls == 0)
				casePatternMatcher.remove(casePattern);
			else {			
				casePattern.controlIDs = new int[numberOfControls];
				casePattern.controlIDs = SampleUtilities.sampleWithoutReplacement(numberOfControls, casePattern.potentialControlCount, random);
				casePattern.potentialControlCount = 0;
				controlCount += numberOfControls;
			}
		}
		System.out.println("- After sampling there are " + controlCount + " controls, average " + (controlCount/(double)casePatternCount) + " controls per case");
	}

	private void outputControls(String sourcePrescriptions,	String sourcePatients, String sourceEvents, String sourceMainPrescriptions, CaseOrControlFileWriter out) {
		System.out.println("Writing controls");
		Iterator<Patient> patientIterator = new PatientFileReader(sourcePatients).iterator();
		Iterator<Prescription> prescriptionIterator = new PrescriptionFileReader(sourcePrescriptions).iterator();
		Iterator<Event> eventIterator = new EventFileReader(sourceEvents).iterator();
		PatientPrescriptionEventIterator iterator = new PatientPrescriptionEventIterator(patientIterator, prescriptionIterator, eventIterator);
		MainPrescriptionMatcher mainPrescriptionMatcher = new MainPrescriptionMatcher(sourceMainPrescriptions);
		while (iterator.hasNext()){
			PatientPrescriptionEvent patientPrescriptionEvent = iterator.next();
			for (CasePattern casePattern : casePatternMatcher.findCases(patientPrescriptionEvent)){ //Matches case pattern
				if (!patientPrescriptionEvent.patient.patientID.equals(casePattern.patient.patientID)){ //It is not the case
					if (casePattern.isControl(casePattern.potentialControlCount++)){ //It is the control we sampled
						writeCaseOrControl(out, casePattern, patientPrescriptionEvent, mainPrescriptionMatcher.findMatchingPrescription(patientPrescriptionEvent.patient), false);
						if (casePattern.allControlsEncountered()){ //All controls for this case pattern sampled
							casePatternMatcher.remove(casePattern); //Remove case pattern
							if (casePatternMatcher.noMoreCasePatterns())
								break;
						}
					}
				}
			}
		}
	}

	protected void writeCaseOrControl(CaseOrControlFileWriter out,CasePattern casePattern, PatientPrescriptionEvent patientPrescriptionEvent, Prescription mainPrescription, boolean isCase) {
		CaseOrControl caseOrControl = new CaseOrControl();
		caseOrControl.caseSetID = casePattern.id;
		caseOrControl.isCase = isCase;
		caseOrControl.eventType = casePattern.event.eventType;
		caseOrControl.patientID = patientPrescriptionEvent.patient.patientID;
		caseOrControl.mainPrescription = mainPrescription;
		addVariablesToCaseOrControl(caseOrControl, casePattern.event.date, patientPrescriptionEvent);
		addWindowStatsToCaseOrControl(caseOrControl, casePattern.event.date, patientPrescriptionEvent);
		out.write(caseOrControl);
	}


	private void addWindowStatsToCaseOrControl(CaseOrControl caseOrControl, long indexDate, PatientPrescriptionEvent patientPrescriptionEvent) {
		for (TimeWindow timeWindow : timeWindows){
			WindowStats windowStats = new WindowStats();
			long referenceDate = timeWindow.pointOfReference==PointOfReference.index?indexDate:patientPrescriptionEvent.patient.startdate;
			long windowStart = referenceDate + timeWindow.startDate;
			long windowEnd = referenceDate + timeWindow.endDate;

			for (Prescription prescription : patientPrescriptionEvent.prescriptions){
				if (prescription.start < windowEnd && prescription.getEnd() >= windowStart){
					long startDate = Math.max(prescription.start, windowStart);
					long endDate = Math.min(prescription.getEnd(), windowEnd);
					long use = endDate-startDate;
					for (ATCCode atc : prescription.atcCodes){
						DrugStats drugStats = windowStats.get(atc);
						if (drugStats == null){
							drugStats = new DrugStats();
							windowStats.put(atc.toString(), drugStats);
						}
						drugStats.daysUsed += use;
						long daysSinceUse;
						if (referenceDate <= windowStart)
							daysSinceUse = startDate - referenceDate;
						else
						  daysSinceUse = referenceDate - endDate;
						drugStats.daysSinceUse = Math.min(drugStats.daysSinceUse,daysSinceUse);
					}
				}
			}
			caseOrControl.window2stats.put(timeWindow.label, windowStats);
		}
	}

	private void addVariablesToCaseOrControl(CaseOrControl caseOrControl, long indexDate, PatientPrescriptionEvent patientPrescriptionEvent) {
		String date = StringUtilities.daysToSortableDateString(indexDate);
		if (outputYear)
			caseOrControl.variables.put("Year",date.substring(0,4));

		if (outputMonth)
			caseOrControl.variables.put("Month", (date.charAt(4) == '0'?date.substring(5,6):date.substring(4,6)));

		if (outputAge)
			caseOrControl.variables.put("Age", Integer.toString((int)((indexDate - patientPrescriptionEvent.patient.birthdate)/365.25)));

		if (outputGender)
			caseOrControl.variables.put("Gender", (patientPrescriptionEvent.patient.gender == Patient.MALE)?"M":"F");

		if (outputChronicDiseaseScore)
			caseOrControl.variables.put("ChronicDiseaseScore", chronicDiseaseScorer.extractCriterium(indexDate, patientPrescriptionEvent));

		if (outputDrugCount)
			caseOrControl.variables.put("DrugCount", drugCounter.extractCriterium(indexDate, patientPrescriptionEvent));

		if (outputTimeInCohort)
			caseOrControl.variables.put("DaysInCohort", Long.toString(indexDate - patientPrescriptionEvent.patient.startdate));
	}

	private static class TimeWindow {
		public long startDate;
		public long endDate;
		public String label;
		public PointOfReference pointOfReference;

		public TimeWindow(long startDate, long endDate, String label, String pointOfReference){
			this.startDate = startDate;
			this.endDate = endDate;
			this.label = label;
			this.pointOfReference = PointOfReference.valueOf(pointOfReference);
		}
	}

	private class CasePatternMatcher {
		private Map<Long, OneToManyList<Byte, CasePattern>> birthDate2Gender2CasePatterns = new HashMap<Long, OneToManyList<Byte,CasePattern>>();
		private List<Long> birthDates;

		public void add(CasePattern casePattern){
			byte gender = Patient.UNKNOWN_GENDER;
			if (matchOnGender)
				gender = casePattern.patient.gender;

			OneToManyList<Byte, CasePattern> gender2CasePatterns = birthDate2Gender2CasePatterns.get(casePattern.patient.birthdate);
			if (gender2CasePatterns == null){
				gender2CasePatterns = new OneToManyList<Byte, CaseControlModule.CasePattern>();
				birthDate2Gender2CasePatterns.put(casePattern.patient.birthdate, gender2CasePatterns);
			}
			gender2CasePatterns.put(gender, casePattern);
		}

		public boolean noMoreCasePatterns() {
			return birthDates.size() == 0;
		}

		public void remove(CasePattern casePattern) {
			long birthDate = casePattern.patient.birthdate;
			OneToManyList<Byte, CasePattern> gender2CasePatterns = birthDate2Gender2CasePatterns.get(birthDate);
			List<CasePattern> casePatterns = gender2CasePatterns.get(casePattern.patient.gender);
			casePatterns.remove(casePattern);
			if (casePatterns.size() == 0){
				gender2CasePatterns.remove(casePattern.patient.gender);
				if (gender2CasePatterns.keySet().size() == 0){
					birthDate2Gender2CasePatterns.remove(birthDate);
					birthDates.remove(birthDate);
				}
			}
		}

		public List<CasePattern> findCases(PatientPrescriptionEvent patientPrescriptionEvent) {
			List<CasePattern> result = new ArrayList<CasePattern>();
			long startRange = patientPrescriptionEvent.patient.birthdate - maxDifferenceBetweenBirthDates;
			long endRange = patientPrescriptionEvent.patient.birthdate + maxDifferenceBetweenBirthDates;
			for (Long birthDate : birthDates)
				if (birthDate > startRange){
					if (birthDate > endRange)
						break;
					else { // Matched on age
						byte gender = Patient.UNKNOWN_GENDER;
						if (matchOnGender)
							gender = patientPrescriptionEvent.patient.gender;
						for (CasePattern casePattern : birthDate2Gender2CasePatterns.get(birthDate).get(gender)){ //Matched on gender
							long indexDate = casePattern.event.date;

							if (indexDate < patientPrescriptionEvent.patient.startdate || indexDate >= patientPrescriptionEvent.patient.enddate) //Index data within patient time
								continue;

							if (firstEventOnly) // Control did not have event of same eventtype prior to index date
								for (Event event : patientPrescriptionEvent.events)
									if (event.eventType.equals(casePattern.event.eventType) && event.date <= indexDate)
										continue;

							if (matchOnChronicDiseaseScore) // Match on CDS
								if (!chronicDiseaseScorer.extractCriterium(indexDate, patientPrescriptionEvent).equals(casePattern.chronicDiseaseScore))
									continue;

							if (matchOnDrugCount) // Match on drug count
								if (!drugCounter.extractCriterium(indexDate, patientPrescriptionEvent).equals(casePattern.drugCount))
									continue;

							if (matchOnATCClass) // Match on exposure to same ATC class
								if (!sameATCClass.extractCriterium(indexDate, patientPrescriptionEvent).equals(casePattern.atcClass))
									continue;

							if (matchOnExternalCaseSetId){ // Match on external caseset ID
								String otherExternalId = externalCaseId.extractCriterium(indexDate, patientPrescriptionEvent);
								if (otherExternalId == null || !otherExternalId.equals(casePattern.externalCaseSetId))
									continue;
							}

							if (Math.abs(casePattern.patient.startdate - patientPrescriptionEvent.patient.startdate) > maxDifferenceInTimeInCohort) // Match on time in cohort
								continue;

							result.add(casePattern);
						}
					}
				}
			return result;
		}

		public void prepareIndex(){
			birthDates = new ArrayList<Long>(birthDate2Gender2CasePatterns.keySet());
			Collections.sort(birthDates);
		}
	}

	protected class CasePattern {
		public String atcClass;
		public Patient patient;
		public int id;
		public String chronicDiseaseScore;
		public String drugCount;
		public Event event;
		public String externalCaseSetId;
		public int potentialControlCount = 0;
		public int[] controlIDs;
		private int controlCursor = 0;
		public long timeInCohort;

		public boolean isControl(int count){
			if (controlIDs[controlCursor] == count){
				controlCursor++;
				return true;
			} else
				return false;
		}

		public boolean allControlsEncountered(){
			return controlCursor == controlIDs.length;
		}

		public CasePattern(Event event, PatientPrescriptionEvent patientPrescriptionEvent, int patternID){
			this.patient = patientPrescriptionEvent.patient;

			if (matchOnChronicDiseaseScore)
				chronicDiseaseScore = chronicDiseaseScorer.extractCriterium(event.date, patientPrescriptionEvent);

			if (matchOnDrugCount)
				drugCount = drugCounter.extractCriterium(event.date, patientPrescriptionEvent);

			if (matchOnExternalCaseSetId)
				externalCaseSetId = externalCaseId.extractCriterium(event.date, patientPrescriptionEvent);

			if (matchOnATCClass)
				atcClass = sameATCClass.extractCriterium(event.date, patientPrescriptionEvent);

			this.timeInCohort = event.date - patientPrescriptionEvent.patient.startdate;
			this.event = event;
			this.id = patternID;
		}
	}
}
