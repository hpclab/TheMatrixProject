package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class DrugUtilizationPatientModule extends JerboaModule {

	private static final long serialVersionUID = 8924295832723421351L;
	
	static final int switchATC          = 0;
	static final int switchPrescription = 1; 
	
	static final int startFirstPrescription = 0;
	static final int startPatientStart      = 1;
	
	//	public JerboaModule databaseID;
	public JerboaModule cohortTime;
	public JerboaModule prescriptions;
	
	/**
	 * The ATC-level to be used
	 */
	public int ATCLevel = 7;
	
	/**
	 * The list of ATC-codes of interest.
	 */
	public List<String> ATCOfInterest = new ArrayList<String>();
	
	/**
	 * Specifies the way switching is defined:
	 *   ATC          : Switch is defined as a change in the group of prescribed ATC-codes
	 *   Prescription : Switch is defined with each prescription 
	 */
	public String switchDefinition = "ATC";	
	
	/**
	 * Specifies the definition of the start of the follow-up: 
	 *   First prescription: Follow-up starts at the first prescription.
	 *   Patient start     : Follow-up starts at the patient's start date. 
	 */
	public String startFollowup = "First prescription";
	
	/**
	 * Debug flag generates debug output
	 *   0 : No debug output
	 *   1 : Add extra columns PatientId, PatientStart, PatientEnd at the left of the output file
	 *   >1: Like 1 but also generates extra console output
	 * Default: 0
	 */
	public int debug = 0;

	
	private int switchDef = switchATC;
	private int startFoll = startFirstPrescription;

	
	public static void main(String[] args) {
		DrugUtilizationPatientModule module = new DrugUtilizationPatientModule();
		String path = "D:\\Documents\\IPCI\\EU Projects\\SOS\\Test Data\\NSAIDs General\\";

		module.ATCLevel = 7;
		
		// NSAIDs
		module.ATCOfInterest.add("M01A");
/*
		module.ATCOfInterest.add("M01AA01");            // Phenylbutazone
		module.ATCOfInterest.add("M01AA02");            // Mofebutazone
		module.ATCOfInterest.add("M01AA03");            // Oxyphenbutazone
		module.ATCOfInterest.add("M01AA05");            // Clofezone
		module.ATCOfInterest.add("M01AA06");            // Kebuzone
		module.ATCOfInterest.add("M01AB01");            // Indometacin
		module.ATCOfInterest.add("M01AB02");            // Sulindac
		module.ATCOfInterest.add("M01AB03");            // Tolmetin
		module.ATCOfInterest.add("M01AB04");            // Zomepirac
		module.ATCOfInterest.add("M01AB05");            // Diclofenac
		module.ATCOfInterest.add("M01AB06");            // Alclofenac
		module.ATCOfInterest.add("M01AB07");            // Bumadizone
		module.ATCOfInterest.add("M01AB08");            // Etodolac
		module.ATCOfInterest.add("M01AB09");            // Lonazolac
		module.ATCOfInterest.add("M01AB10");            // Fentiazac
		module.ATCOfInterest.add("M01AB11");            // Acemetacin
		module.ATCOfInterest.add("M01AB12");            // Difenpiramide
		module.ATCOfInterest.add("M01AB13");            // Oxametacin
		module.ATCOfInterest.add("M01AB14");            // Proglumetacin
		module.ATCOfInterest.add("M01AB15");            // Ketorolac
		module.ATCOfInterest.add("M01AB16");            // Aceclofenac
		module.ATCOfInterest.add("M01AB17");            // Bufexamac
		module.ATCOfInterest.add("M01AB51");            // Indometacin, combinations
		module.ATCOfInterest.add("M01AB55");            // Diclofenac, combinations
		module.ATCOfInterest.add("M01AC01");            // Piroxicam
		module.ATCOfInterest.add("M01AC02");            // Tenoxicam
		module.ATCOfInterest.add("M01AC04");            // Droxicam
		module.ATCOfInterest.add("M01AC05");            // Lornoxicam
		module.ATCOfInterest.add("M01AC06");            // Meloxicam
		module.ATCOfInterest.add("M01AE01");            // Ibuprofen
		module.ATCOfInterest.add("M01AE02");            // Naproxen
		module.ATCOfInterest.add("M01AE03");            // Ketoprofen
		module.ATCOfInterest.add("M01AE04");            // Fenoprofen
		module.ATCOfInterest.add("M01AE05");            // Fenbufen
		module.ATCOfInterest.add("M01AE06");            // Benoxaprofen
		module.ATCOfInterest.add("M01AE07");            // Suprofen
		module.ATCOfInterest.add("M01AE08");            // Pirprofen
		module.ATCOfInterest.add("M01AE09");            // Flurbiprofen
		module.ATCOfInterest.add("M01AE10");            // Indoprofen
		module.ATCOfInterest.add("M01AE11");            // Tiaprofenic acid
		module.ATCOfInterest.add("M01AE12");            // Oxaprozin
		module.ATCOfInterest.add("M01AE13");            // Ibuproxam
		module.ATCOfInterest.add("M01AE14");            // Dexibuprofen
		module.ATCOfInterest.add("M01AE15");            // Flunoxaprofen
		module.ATCOfInterest.add("M01AE16");            // Alminoprofen
		module.ATCOfInterest.add("M01AE17");            // Dexketoprofen
		module.ATCOfInterest.add("M01AE51");            // Ibuprofen, combinations
		module.ATCOfInterest.add("M01AE53");            // Ketoprofen, combinations
		module.ATCOfInterest.add("M01AG01");            // Mefenamic  acid
		module.ATCOfInterest.add("M01AG02");            // Tolfenamic  acid
		module.ATCOfInterest.add("M01AG03");            // Flufenamic acid
		module.ATCOfInterest.add("M01AG04");            // Meclofenamic acid
		module.ATCOfInterest.add("M01AH01");            // Celecoxib
		module.ATCOfInterest.add("M01AH02");            // Rofecoxib
		module.ATCOfInterest.add("M01AH03");            // Valdecoxib
		module.ATCOfInterest.add("M01AH04");            // Parecoxib
		module.ATCOfInterest.add("M01AH05");            // Etoricoxib
		module.ATCOfInterest.add("M01AH06");            // Lumiracoxib
		module.ATCOfInterest.add("M01AX01");            // Nabumetone
		module.ATCOfInterest.add("M01AX02");            // Niflumic  acid
		module.ATCOfInterest.add("M01AX04");            // Azapropazone
		module.ATCOfInterest.add("M01AX05");            // Glucosamine
		module.ATCOfInterest.add("M01AX07");            // Benzydamine
		module.ATCOfInterest.add("M01AX12");            // Glucosaminoglycan polysulfate
		module.ATCOfInterest.add("M01AX13");            // Proquazone
		module.ATCOfInterest.add("M01AX14");            // Orgotein
		module.ATCOfInterest.add("M01AX17");            // Nimesulide
		module.ATCOfInterest.add("M01AX18");            // Feprazone
		module.ATCOfInterest.add("M01AX21");            // Diacerein
		module.ATCOfInterest.add("M01AX22");            // Morniflumate
		module.ATCOfInterest.add("M01AX23");            // Tenidap
		module.ATCOfInterest.add("M01AX24");            // Oxaceprol
		module.ATCOfInterest.add("M01AX25");            // Chondroitin sulfate
		module.ATCOfInterest.add("M01AX68");            // Feprazone, combinations
*/ 
		
		FileSorter.sort(path + "Prescriptions.txt", new String[]{"PatientID", "Date", "ATC"});
		FileSorter.sort(path + "Cohortentrydate.txt", new String[]{"PatientID"});
		module.process(path + "Prescriptions.txt", path + "Cohortentrydate.txt", path + "DrugUtilizationPatient.txt");
	}

	
	protected void runModule(String outputFilename){
		FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID", "Date", "ATC"});
		FileSorter.sort(cohortTime.getResultFilename(), "PatientID");
		process(prescriptions.getResultFilename(), cohortTime.getResultFilename(), outputFilename);
	}

	
	public void process(String sourcePrescriptions, String sourceCohort, String target) {
		PatientFileReader patientsReader = new PatientFileReader(sourceCohort);
		PrescriptionFileReader prescriptionsReader = new PrescriptionFileReader(sourcePrescriptions);
		Iterator<Patient> patientIterator;
		Iterator<Prescription> prescriptionIterator;
		Iterator<ATCCode> atcIterator;
		ATCCode atcCode = null; 
		Patient patient = null;
		Prescription prescription = null;
		int patientComparison = -1;
		Data patientRecord = null;
		WriteCSVFile outputFile = new WriteCSVFile(target);
		boolean headerWritten = false;
		int patientsSkippedATC = 0;

		if (switchDefinition.compareTo("Prescription") == 0) {
			switchDef = switchPrescription;
			System.out.println("SwitchDefintion = Prescription");
		}
		else {
			switchDef = switchATC;
			System.out.println("SwitchDefintion = ATC");
		}

		if (startFollowup.compareTo("Patient start") == 0) {
			startFoll = startPatientStart;
			System.out.println("startFollowup = Patient start");
		}
		else {
			startFoll = startFirstPrescription;
			System.out.println("startFollowup = First prescription");
		}
		
		patientIterator = patientsReader.iterator();
		prescriptionIterator = prescriptionsReader.iterator();
		while (patientIterator.hasNext()) {
			patient = patientIterator.next();
			patientRecord = new Data(patient,switchDef,startFoll);
			
			//if (patient.patientID.compareTo("005975760") == 0) debug = 2;
			
			if (debug > 1) {
				System.out.println(patient.patientID + " " + StringUtilities.daysToSortableDateString(patient.birthdate) + ": " + StringUtilities.daysToSortableDateString(patient.startdate) + "-" + StringUtilities.daysToSortableDateString(patient.enddate));
			}
			
			if (prescription == null) {
				while ((prescription == null) && (prescriptionIterator.hasNext())) {
					prescription = prescriptionIterator.next();
					patientComparison = prescription.patientID.compareTo(patient.patientID);
					if (patientComparison < 0) // Prescriptions patient before Patients patient -> get next prescription
						prescription = null;
				}
			}
			
			while ((prescription != null) && (prescription.patientID.compareTo(patient.patientID) == 0)) {
				// Prescription belongs to current patient
				
				if (debug > 1) System.out.println("  " + prescription.patientID + " " + StringUtilities.daysToSortableDateString(prescription.start) + " " + prescription.getATCCodesAsString() + " " + Long.toString(prescription.duration));
				
				if ((prescription.start >= patient.startdate) && (prescription.start <= patient.enddate)) {
					atcIterator = prescription.atcCodes.iterator();
					atcCode = atcIterator.next();
					if (atcCode != null) {
						if (IsATCOfInterest(atcCode.atc)) {
							patientRecord.addATC(atcCode.atc, prescription.start, prescription.duration, ATCLevel, debug);
						}
						else {
							if (debug > 1) System.out.println("    ATC-code " + atcCode.atc + " not interesting");
						}
					}
					else {
						if (debug > 1) System.out.println("    No ATC-code");
					}
				}
				else {
					if (debug > 1) System.out.println("    Outside cohort period");
				}
				
				// Get next prescription
				if (prescriptionIterator.hasNext())
					prescription = prescriptionIterator.next();
				else
					prescription = null;
			}
			
			// Prescriptions patient after Patients patient -> write current patient and get next patient
			if (!headerWritten) {
				patientRecord.writeHeaderToFile(outputFile,debug);
				headerWritten = true;
			}
			if (!patientRecord.writeToFile(outputFile,debug)) {
				patientsSkippedATC++;
			}
		}
		
		outputFile.close();
		
		System.out.println("Skipped " + Integer.toString(patientsSkippedATC) + " patients:");
		System.out.println("  " + Integer.toString(patientsSkippedATC) + " without drug of interest");
	}
	
	
	private boolean IsATCOfInterest(String atcCode) {
		boolean result = false;
		
		for (String atc : ATCOfInterest) {
			if (atcCode.substring(0, Math.min(atc.length(),ATCLevel)).equals(atc)) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	
	private static class Data {
		private Patient patient = null;
		private int switchDef = switchATC;
		private int startDef = startFirstPrescription;
		private Map<Long, Period> stablePeriods = new HashMap<Long, Period>();
		private Set<String> differentATC = new HashSet<String>();
		private long followUpStart = -1;
		private long followUpEnd = -1;
		private long indexDate = -1;
		private String firstATC = "";
		private int prescriptionsFollowup = 0;
		private int prescriptionsFirstYear = 0;
		//private String firstExposures = "";
		private long exposureTotal = 0;
		private long exposureFirstYear = 0;
		private int differentATCFirstYear = -1;
		private String atcFirstSwitch = "";
		private String atcSecondSwitch = "";
		private String atcThirdSwitch = "";
		private long timeToFirstSwitch = -1;
		private long timeToSecondSwitch = -1;
		private long timeToThirdSwitch = -1;

		
		public Data(Patient pat, int switch_def, int start_def) {
			patient = pat;
			switchDef = switch_def;
			startDef = start_def;
			followUpStart = patient.startdate;
			followUpEnd = patient.enddate;
		}
		
		private static class Period {
			long startDate = -1;
			long endDate = -1;
			private List<String> allATC = new ArrayList<String>();
			private Set<String> differentATC = new HashSet<String>();
			
			public Period Copy() {
				Period copy = new Period();
				copy.startDate = startDate;
				copy.endDate = endDate;
				Iterator<String> allATCIterator = allATC.iterator();
				while (allATCIterator.hasNext()) copy.allATC.add(allATCIterator.next());
				Iterator<String> differentATCIterator = differentATC.iterator();
				while (differentATCIterator.hasNext()) copy.differentATC.add(differentATCIterator.next());
				return copy;
			}
			
			public boolean MergeAble(Period otherPeriod) {
				boolean result = true;
				String ATC;
				String otherATC;
				
				if (result && (endDate != otherPeriod.startDate)) result = false;
				if (result && (differentATC.size() != otherPeriod.differentATC.size())) result = false;
				if (result && (allATC.size() != otherPeriod.allATC.size())) result = false;
				if (result) {
					Iterator<String> differentATCIterator = differentATC.iterator();
					while (result && differentATCIterator.hasNext()) {
						result = otherPeriod.differentATC.contains(differentATCIterator.next());
					}
				}
				if (result) {
					Collections.sort(allATC);
					Collections.sort(otherPeriod.allATC);
					Iterator<String> allATCIterator = allATC.iterator();
					Iterator<String> otherAllATCIterator = otherPeriod.allATC.iterator();
					while (result && allATCIterator.hasNext()) {
						ATC = allATCIterator.next();
						otherATC = otherAllATCIterator.next();
						result = (ATC.compareTo(otherATC) == 0);
						//result = (allATCIterator.next().compareTo(otherAllATCIterator.next()) == 0);
					}
				}
				
				return result;
			}
			
			public void WriteToConsole(String prefix) {
				System.out.print(prefix + StringUtilities.daysToSortableDateString(startDate) + "-" + StringUtilities.daysToSortableDateString(endDate) + " " + Long.toString(endDate - startDate));
				System.out.print(prefix + "  All ATC(" + Integer.toString(allATC.size()) + "):");
				Iterator<String> allATCIterator = allATC.iterator();
				while (allATCIterator.hasNext()) System.out.print(" " + allATCIterator.next());
				System.out.print(prefix + "  Different ATC(" + Integer.toString(differentATC.size()) + "):");
				Iterator<String> differentATCIterator = differentATC.iterator();
				while (differentATCIterator.hasNext()) System.out.print(" " + differentATCIterator.next());
				System.out.println("");
			}
		}
		
		
		public void addATC(String atcCode, long date, long duration, int atcLevel, int debug) {
			boolean ready = false;
			Period period = null;
			Period newPeriod = null;
			List<Long> sortedStartDates = new ArrayList<Long>();
			Iterator<Long> datesSetIterator;
			Iterator<Long> datesIterator;

			String levelATC = atcCode.substring(0, atcLevel);
			if (debug > 1) System.out.println("      addATC: " + atcCode + " (" + levelATC + ") " + StringUtilities.daysToSortableDateString(date) + "-" + StringUtilities.daysToSortableDateString(date + duration) + " " + Long.toString(duration));
			
			if (indexDate == -1) {
				if (startDef == startFirstPrescription) {
					if (date >= patient.startdate) indexDate = date;
				}
				else {
					indexDate = patient.startdate;
				}
			}
			if (debug > 1) {
				System.out.println("        patient.startdate = " + StringUtilities.daysToSortableDateString(patient.startdate));
				if (indexDate != -1) System.out.println("        indexDate = " + StringUtilities.daysToSortableDateString(indexDate));
				System.out.println("        date + duration = " + StringUtilities.daysToSortableDateString(date + duration));
			}
			
			if ((indexDate != -1) && ((date + duration) >= indexDate)) {
				if (date < indexDate) date = indexDate; 
				prescriptionsFollowup++;
				if (date < (indexDate + 366)) {
					prescriptionsFirstYear++;
				}
				else {
					if (differentATCFirstYear == -1) {
						differentATCFirstYear = differentATC.size();
					}
				}
				differentATC.add(levelATC);

				Set<Long> startDates = stablePeriods.keySet();
				datesSetIterator = startDates.iterator();
				sortedStartDates.clear();
				while (datesSetIterator.hasNext()) sortedStartDates.add(datesSetIterator.next());
				Collections.sort(sortedStartDates);
				datesIterator = sortedStartDates.iterator();
				
				while ((!ready) && datesIterator.hasNext()) {
					Long existingDate = datesIterator.next();
					period = stablePeriods.get(existingDate);
					
					switch (switchDef) {
						case 0: // ATC
							if ((period.startDate <= date) && (date < period.endDate)) {
								if (date == period.startDate) {
									if ((date + duration) < period.endDate) {
										if (!period.differentATC.contains(levelATC)) {
											newPeriod = period.Copy();
											newPeriod.startDate = date + duration;
											stablePeriods.put(newPeriod.startDate,newPeriod);
											period.endDate = date + duration;
											period.allATC.add(levelATC);
											period.differentATC.add(levelATC);
										}
										ready = true;
									}
									else {
										if ((date + duration) == period.endDate) {
											if (!period.differentATC.contains(levelATC)) {
												period.allATC.add(levelATC);
												period.differentATC.add(levelATC);
											}
											ready = true;
										}
										else { // (date + duration) > period.endDate
											if (!period.differentATC.contains(levelATC)) {
												period.allATC.add(levelATC);
												period.differentATC.add(levelATC);
											}
											duration = date + duration - period.endDate;
											date = period.endDate;
										}
									}
								}
								else { // date > period.startDate
									if ((date + duration) < period.endDate) {
										if (!period.differentATC.contains(levelATC)) {
											stablePeriods.remove(period.startDate);
											newPeriod = period.Copy();
											newPeriod.endDate = date;
											stablePeriods.put(date,newPeriod);
											newPeriod = period.Copy();
											newPeriod.startDate = date + duration;
											stablePeriods.put(newPeriod.startDate,newPeriod);
											period.startDate = date;
											period.endDate = date + duration;
											period.allATC.add(levelATC);
											period.differentATC.add(levelATC);
											stablePeriods.put(period.startDate, period);
										}
										ready = true;
									}
									else {
										if ((date + duration) == period.endDate) {
											if (!period.differentATC.contains(levelATC)) {
												stablePeriods.remove(period.startDate);
												newPeriod = period.Copy();
												newPeriod.endDate = date;
												stablePeriods.put(newPeriod.startDate,newPeriod);
												period.startDate = date;
												period.endDate = date + duration;
												period.allATC.add(levelATC);
												period.differentATC.add(levelATC);
												stablePeriods.put(period.startDate, period);
											}
											ready = true;
										}
										else { // (date + duration) > period.endDate
											if (!period.differentATC.contains(levelATC)) {
												newPeriod = period.Copy();
												newPeriod.endDate = date;
												stablePeriods.remove(period.startDate);
												stablePeriods.put(newPeriod.startDate,newPeriod);
												period.startDate = date;
												period.allATC.add(levelATC);
												period.differentATC.add(levelATC);
												stablePeriods.put(period.startDate, period);
											}
											duration = date + duration - period.endDate;
											date = period.endDate;
										}
									}
								}
							}
							break;
						case 1: // Prescription
							if ((period.startDate <= date) && (date < period.endDate)) {
								if (date == period.startDate) {
									if ((date + duration) < period.endDate) {
										newPeriod = period.Copy();
										newPeriod.startDate = date + duration;
										stablePeriods.put(newPeriod.startDate,newPeriod);
										period.endDate = date + duration;
										period.allATC.add(levelATC);
										period.differentATC.add(levelATC);
										ready = true;
									}
									else {
										if ((date + duration) == period.endDate) {
											period.allATC.add(levelATC);
											period.differentATC.add(levelATC);
											ready = true;
										}
										else { // (date + duration) > period.endDate
											period.allATC.add(levelATC);
											period.differentATC.add(levelATC);
											duration = date + duration - period.endDate;
											date = period.endDate;
										}
									}
								}
								else { // date > period.startDate
									if ((date + duration) < period.endDate) {
										stablePeriods.remove(period.startDate);
										newPeriod = period.Copy();
										newPeriod.endDate = date;
										stablePeriods.put(date,newPeriod);
										newPeriod = period.Copy();
										newPeriod.startDate = date + duration;
										stablePeriods.put(newPeriod.startDate,newPeriod);
										period.startDate = date;
										period.endDate = date + duration;
										period.allATC.add(levelATC);
										period.differentATC.add(levelATC);
										stablePeriods.put(period.startDate, period);
										ready = true;
									}
									else {
										if ((date + duration) == period.endDate) {
											stablePeriods.remove(period.startDate);
											newPeriod = period.Copy();
											newPeriod.endDate = date;
											stablePeriods.put(newPeriod.startDate,newPeriod);
											period.startDate = date;
											period.endDate = date + duration;
											period.allATC.add(levelATC);
											period.differentATC.add(levelATC);
											stablePeriods.put(period.startDate, period);
											ready = true;
										}
										else { // (date + duration) > period.endDate
											stablePeriods.remove(period.startDate);
											newPeriod = period.Copy();
											newPeriod.endDate = date;
											stablePeriods.put(newPeriod.startDate,newPeriod);
											period.startDate = date;
											period.allATC.add(levelATC);
											period.differentATC.add(levelATC);
											stablePeriods.put(period.startDate, period);
											duration = date + duration - period.endDate;
											date = period.endDate;
										}
									}
								}
							}
							break;
						default:
							break;
					}
				}
				if (!ready) {
					newPeriod = new Period();
					newPeriod.startDate = date;
					newPeriod.endDate = date + duration;
					newPeriod.allATC.add(levelATC);
					newPeriod.differentATC.add(levelATC);
					stablePeriods.put(newPeriod.startDate,newPeriod);
				}
				
				if (debug > 1) MergeStablePeriods(debug);
			}
		}
		
		
		public void MergeStablePeriods(int debug) {
			Period period = null;
			Period nextPeriod = null;
			Iterator<Long> datesSetIterator;
			Iterator<Long> datesIterator;
			List<Long> sortedStartDates = new ArrayList<Long>();

			Set<Long> startDates = stablePeriods.keySet();
			datesSetIterator = startDates.iterator();
			sortedStartDates.clear();
			while (datesSetIterator.hasNext()) sortedStartDates.add(datesSetIterator.next());
			Collections.sort(sortedStartDates);
			datesIterator = sortedStartDates.iterator();
			
			if (debug > 1) {
				System.out.println("        Stable periods before merge:");
				while (datesIterator.hasNext()) {
					Long existingDate = datesIterator.next();
					period = stablePeriods.get(existingDate);
					period.WriteToConsole("          ");
				}
				datesIterator = sortedStartDates.iterator();
			}
			
			while (datesIterator.hasNext()) {
				Long existingDate = datesIterator.next();
				nextPeriod = stablePeriods.get(existingDate);
				
				if (period != null) {
					if (period.MergeAble(nextPeriod)) {
						period.endDate = nextPeriod.endDate;
						stablePeriods.remove(nextPeriod.startDate);
					}
					else {
						period = nextPeriod;
					}
				}
				else {
					period = nextPeriod;
				}
			}
			
			if (debug > 1) {				
				System.out.println("        Stable periods after merge:");
				startDates = stablePeriods.keySet();
				datesSetIterator = startDates.iterator();
				sortedStartDates.clear();
				while (datesSetIterator.hasNext()) sortedStartDates.add(datesSetIterator.next());
				Collections.sort(sortedStartDates);
				datesIterator = sortedStartDates.iterator();
				while (datesIterator.hasNext()) {
					Long existingDate = datesIterator.next();
					period = stablePeriods.get(existingDate);
					period.WriteToConsole("          ");
				}
			}
		}
		
		
		public void writeHeaderToFile(WriteCSVFile outputFile,int debug) {
			List<String> header = new ArrayList<String>();
			
			if (debug > 0) {
				header.add("PatientId");
				header.add("PatientStart");
				header.add("PatientEnd");
			}
			header.add("IndexATC");
			header.add("Year");
			header.add("Age");
			header.add("Gender");
			header.add("FollowupDays");
			header.add("PrescriptionsFollowup");
			header.add("PrescriptionsFirstYear");
			//header.add("IndexExposures");
			header.add("ExposureTotal");
			header.add("ExposureFirstYear");
			header.add("DifferentATCFollowup");
			header.add("DifferentATCFirstYear");
			header.add("ATCFirstSwitch");
			header.add("ATCSecondSwitch");
			header.add("ATCThirdSwitch");
			header.add("TimeToFirstSwitch");
			header.add("TimeToSecondSwitch");
			header.add("TimeToThirdSwitch");
			
			outputFile.write(header);
			
			if (debug > 1) {
				String debugHeader;
				debugHeader = "PatientID";
				debugHeader = debugHeader + ",PatientStart";
				debugHeader = debugHeader + ",PatientEnd";
				debugHeader = debugHeader + ",IndexATC";
				debugHeader = debugHeader + ",Year";
				debugHeader = debugHeader + ",Age";
				debugHeader = debugHeader + ",Gender";
				debugHeader = debugHeader + ",FollowupDays";
				debugHeader = debugHeader + ",PrescriptionsFollowup";
				debugHeader = debugHeader + ",PrescriptionsFirstYear";
				//debugHeader = debugHeader + ",IndexExposures";
				debugHeader = debugHeader + ",ExposureTotal";
				debugHeader = debugHeader + ",ExposureFirstYear";
				debugHeader = debugHeader + ",DifferentATCFollowup";
				debugHeader = debugHeader + ",DifferentATCFirstYear";
				debugHeader = debugHeader + ",ATCFirstSwitch";
				debugHeader = debugHeader + ",ATCSecondSwitch";
				debugHeader = debugHeader + ",ATCThirdSwitch";
				debugHeader = debugHeader + ",TimeToFirstSwitch";
				debugHeader = debugHeader + ",TimeToSecondSwitch";
				debugHeader = debugHeader + ",TimeToThirdSwitch";
				System.out.println(debugHeader);
			}
		}

		
		public boolean writeToFile(WriteCSVFile outputFile,int debug) {
			boolean written = true;
			List<String> line = new ArrayList<String>();
			Set<Long> startDates;
			Iterator<Long> datesSetIterator;
			Iterator<Long> datesIterator;
			List<Long> sortedStartDates = new ArrayList<Long>();
			List<String> sortedATC = new ArrayList<String>();
			Iterator<String> allATCIterator;
			Iterator<String> differentATCIterator;
			Period period = null;
			String atcCode;
			String activeATC;
			boolean firstATCSet = false;
			boolean firstSwitchSet = false;
			boolean secondSwitchSet = false;
			boolean thirdSwitchSet = false;
			long dateLastSwitch = 0L;
			
			MergeStablePeriods(debug);
			
			startDates = stablePeriods.keySet();
			datesSetIterator = startDates.iterator();
			sortedStartDates.clear();
			while (datesSetIterator.hasNext()) sortedStartDates.add(datesSetIterator.next());
			Collections.sort(sortedStartDates);
			datesIterator = sortedStartDates.iterator();
			
			while (datesIterator.hasNext()) {
				period = stablePeriods.get(datesIterator.next());
				
				if (period.endDate >= followUpEnd) exposureTotal = exposureTotal + (followUpEnd - period.startDate);
				else                               exposureTotal = exposureTotal + (period.endDate - period.startDate);
				if (period.startDate < (indexDate + 366)) {
					if (period.endDate >= (indexDate + 366)) exposureFirstYear = exposureFirstYear + ((indexDate + 366) - period.startDate);
					else                                     exposureFirstYear = exposureFirstYear + (period.endDate - period.startDate);
				}

				switch (switchDef) {
					case 0: // ATC
						sortedATC.clear();
						differentATCIterator = period.differentATC.iterator();
						while (differentATCIterator.hasNext()) sortedATC.add(differentATCIterator.next());	
						break;
					case 1: // Prescription
						sortedATC.clear();
						allATCIterator = period.allATC.iterator();
						while (allATCIterator.hasNext()) sortedATC.add(allATCIterator.next());
						break;
					default:
						break;
				}

				activeATC = "";
				Collections.sort(sortedATC);
				differentATCIterator = sortedATC.iterator();
				while (differentATCIterator.hasNext()) {
					atcCode = differentATCIterator.next();

					if (activeATC != "") activeATC = activeATC + "+";
					activeATC = activeATC + atcCode;
				}
				
				if (!firstATCSet) { 
					firstATC        = activeATC; 
					dateLastSwitch = period.startDate; 
					firstATCSet    = true;
				}
				else {
					if (!firstSwitchSet) {
						if (activeATC.compareTo(firstATC) != 0) {
							atcFirstSwitch     = activeATC; 
							timeToFirstSwitch  = period.startDate - dateLastSwitch;
							dateLastSwitch     = period.startDate;
							firstSwitchSet     = true;
						} 
					}
					else {
						if (!secondSwitchSet) {
							if (activeATC.compareTo(atcFirstSwitch) != 0) {
								atcSecondSwitch    = activeATC; 
								timeToSecondSwitch = period.startDate - dateLastSwitch;
								dateLastSwitch     = period.startDate; 
								secondSwitchSet    = true;
							}
						}
						else {
							if (!thirdSwitchSet) {
								if (activeATC.compareTo(atcSecondSwitch) != 0) {
									atcThirdSwitch     = activeATC; 
									timeToThirdSwitch  = period.startDate - dateLastSwitch;
									dateLastSwitch     = period.startDate;
									thirdSwitchSet     = true;
								}
							}
						}
					}
				}
			}
			
			if (firstATC != "") {
				if (debug > 0) {
					line.add(patient.patientID);
					line.add(StringUtilities.daysToSortableDateString(patient.startdate));
					line.add(StringUtilities.daysToSortableDateString(patient.enddate));
				}
				line.add(firstATC);
				line.add(StringUtilities.daysToSortableDateString(followUpStart).substring(0, 4));
				line.add(Integer.toString(patient.AgeOnDate(followUpStart)));
				line.add(patient.gender == Patient.MALE?"M":"F");
				line.add(Long.toString(followUpEnd - followUpStart));
				line.add(Integer.toString(prescriptionsFollowup));
				line.add(Integer.toString(prescriptionsFirstYear));
				//line.add(firstExposures);
				line.add(Long.toString(exposureTotal));
				line.add(Long.toString(exposureFirstYear));
				line.add(Integer.toString(differentATC.size()));
				line.add(Integer.toString(differentATCFirstYear));
				line.add(atcFirstSwitch);
				line.add(atcSecondSwitch);
				line.add(atcThirdSwitch);
				if (timeToFirstSwitch != -1)
					line.add(Long.toString(timeToFirstSwitch));
				else
					line.add("");
				if (timeToSecondSwitch != -1)
					line.add(Long.toString(timeToSecondSwitch));
				else
					line.add("");
				if (timeToThirdSwitch != -1)
					line.add(Long.toString(timeToThirdSwitch));
				else
					line.add("");
				
				outputFile.write(line);
				
				if (debug > 1) {
					String debugLine;
					debugLine = patient.patientID;
					debugLine = debugLine + "," + StringUtilities.daysToSortableDateString(patient.startdate);
					debugLine = debugLine + "," + StringUtilities.daysToSortableDateString(patient.enddate);
					debugLine = debugLine + "," + firstATC;
					debugLine = debugLine + "," + StringUtilities.daysToSortableDateString(followUpStart).substring(0, 4);
					debugLine = debugLine + "," + Integer.toString(patient.AgeOnDate(followUpStart));
					debugLine = debugLine + "," + (patient.gender == Patient.MALE?"M":"F");
					debugLine = debugLine + "," + Long.toString(followUpEnd - followUpStart);
					debugLine = debugLine + "," + Integer.toString(prescriptionsFollowup);
					debugLine = debugLine + "," + Integer.toString(prescriptionsFirstYear);
					//debugLine = debugLine + "," + firstExposures;
					debugLine = debugLine + "," + Long.toString(exposureTotal);
					debugLine = debugLine + "," + Long.toString(exposureFirstYear);
					debugLine = debugLine + "," + Integer.toString(differentATC.size());
					debugLine = debugLine + "," + Integer.toString(differentATCFirstYear);
					debugLine = debugLine + "," + atcFirstSwitch;
					debugLine = debugLine + "," + atcSecondSwitch;
					debugLine = debugLine + "," + atcThirdSwitch;
					if (timeToFirstSwitch != -1)
						debugLine = debugLine + "," + Long.toString(timeToFirstSwitch);
					else
						debugLine = debugLine + "," + "";
					if (timeToSecondSwitch != -1)
						debugLine = debugLine + "," + Long.toString(timeToSecondSwitch);
					else
						debugLine = debugLine + "," + "";
					if (timeToThirdSwitch != -1)
						debugLine = debugLine + "," + Long.toString(timeToThirdSwitch);
					else
						debugLine = debugLine + "," + "";
					System.out.println(debugLine);
				}
			}
			else {
				written = false;
			}
			
			return written;
		}
	}

}
