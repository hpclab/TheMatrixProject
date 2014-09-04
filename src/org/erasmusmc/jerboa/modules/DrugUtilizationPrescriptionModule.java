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
package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEventIterator;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Calculates the drug utilization on prescription level.
 * @author bmosseveld
 *
 */
public class DrugUtilizationPrescriptionModule extends JerboaModule {
	
	private static final long serialVersionUID = -2477861828681861373L;

	public JerboaModule events;
	public JerboaModule cohortTime;
	public JerboaModule prescriptions;
	
	/**
	 * The list of ATC-codes of interest.
	 */
	public List<String> ATCOfInterest = new ArrayList<String>();
	
	/**
	 * The list of prior ATC-codes of interest.
	 */
	public List<String> priorATCOfInterest = new ArrayList<String>();
	
	/**
	 * The list of concomitant ATC-codes of interest.
	 */
	public List<String> concomitantATCOfInterest = new ArrayList<String>();
	
	/**
	 * The list of prior events of interest.
	 */
	public List<String> priorEventsOfInterest = new ArrayList<String>();
	
	/**
	 * The period (in days) to check for history.<BR>
	 * default = 365
	 */
	public int historyWindow = 365;
	
	/**
	 * The ID of the database is stored in this private variable.
	 */
	//DBID private String DBID = "";
	

	public static void main(String[] args) {
		DrugUtilizationPrescriptionModule module = new DrugUtilizationPrescriptionModule();
		String path = "D:\\Temp\\SOS DUS\\SOS DUS Jerboa Test Set\\Test Prescription Level\\";
		
		// NSAIDs
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

		module.priorATCOfInterest.add("A02BC");         // PPI
		module.priorATCOfInterest.add("A02BB01");       // Misoprostol
		module.priorATCOfInterest.add("A02BA");         // H2RA
		module.priorATCOfInterest.add("M01AH");         // Coxibs
		module.priorATCOfInterest.add("B01AC");         // Platelet inhibitors
		module.priorATCOfInterest.add("B01AA");         // Vitamin K antagonist
		module.priorATCOfInterest.add("H02");           // Corticosteroids
		module.priorATCOfInterest.add("C03");           // Diuretics
		module.priorATCOfInterest.add("C07");           // Beta blockers
		module.priorATCOfInterest.add("C08");           // CCBs
		module.priorATCOfInterest.add("C09");           // ACE/ARB
		module.priorATCOfInterest.add("A10");           // Insulin/Antidiabetics
		module.priorATCOfInterest.add("C10");           // Lipid lowering drugs

		module.concomitantATCOfInterest.add("A02BC");   // PPI
		module.concomitantATCOfInterest.add("A02BA");   // H2RA

		module.priorEventsOfInterest.add("UGIB");       // Upper GI Bleeding
		module.priorEventsOfInterest.add("MI");        // Acute MI
		module.priorEventsOfInterest.add("STRO");       // Stroke
		module.priorEventsOfInterest.add("HF");         // Heart failure
		
		FileSorter.sort(path + "Prescriptions.txt", new String[]{"PatientID", "Date", "ATC"});
		FileSorter.sort(path + "Events.txt", new String[]{"PatientID", "Date"});
		FileSorter.sort(path + "Cohortentrydate.txt", new String[]{"PatientID"});
		//DBID module.process(path + "DatabaseID.txt", path + "Prescriptions.txt", path + "Events.txt", path + "Cohortentrydate.txt", path + "DrugUtilizationPrescription.txt");
		module.process(path + "Prescriptions.txt", path + "Events.txt", path + "Cohortentrydate.txt", path + "DrugUtilizationPrescription.txt");
	}
	
	protected void runModule(String outputFilename){
		FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID", "Date", "ATC"});
		FileSorter.sort(events.getResultFilename(), new String[]{"PatientID", "Date"});
		FileSorter.sort(cohortTime.getResultFilename(), "PatientID");
		process(prescriptions.getResultFilename(), events.getResultFilename(), cohortTime.getResultFilename(), outputFilename);
	}

	public void process(String sourcePrescriptions, String sourceEvents, String sourceCohort, String target) {
		PatientFileReader patientsReader = new PatientFileReader(sourceCohort);
		PrescriptionFileReader prescriptionsReader = new PrescriptionFileReader(sourcePrescriptions);
		EventFileReader eventsReader = new EventFileReader(sourceEvents);
		PatientPrescriptionEvent ppe = null;
		int prescriptionNumber = 0;
		Iterator<Event> eventIterator;
		Iterator<Prescription> prescriptionIterator;
		Iterator<Prescription> prescription2Iterator;
		Iterator<String> atcOfInterestIterator;
		Iterator<String> eventsOfInterestIterator;
		Data data = null;
		ATCCode atcCode = null;
		String atc = "";
			
		// Open the output file
		WriteCSVFile targetFile = new WriteCSVFile(target);
		boolean headerWritten = false;
		  		
		PatientPrescriptionEventIterator ppeIterator = new PatientPrescriptionEventIterator(patientsReader.iterator(),prescriptionsReader.iterator(),eventsReader.iterator());
		while (ppeIterator.hasNext()) {
			ppe = ppeIterator.next();
			//System.out.println("  Patient " + ppe.patient.patientID + " , " + StringUtilities.daysToSortableDateString(ppe.patient.birthdate) + " , " + Byte.toString(ppe.patient.gender) + " , " + StringUtilities.daysToSortableDateString(ppe.patient.startdate) + " , " + StringUtilities.daysToSortableDateString(ppe.patient.enddate));
			//eventIterator = ppe.events.iterator();
			//while (eventIterator.hasNext()) {
			//	Event event = eventIterator.next();
			//	System.out.println("    Event       : " + event.patientID + " , " + StringUtilities.daysToSortableDateString(event.date) + " , " + event.eventType);
  			//}
			prescriptionIterator = ppe.prescriptions.iterator();
			while (prescriptionIterator.hasNext()) {
				data = null;
				Prescription prescription = prescriptionIterator.next();
				if (((prescription.start + prescription.duration) >= ppe.patient.startdate) &&
					(prescription.start <= ppe.patient.enddate)) {
					//System.out.println("    Prescription: " + prescription.patientID + " , " + StringUtilities.daysToSortableDateString(prescription.start) + " , " + Long.toString(prescription.duration) + " , " + prescription.getATCCodesAsString() + " (" + StringUtilities.daysToSortableDateString(prescription.start) + " - " + StringUtilities.daysToSortableDateString(prescription.start + prescription.duration) + ")");
					atcCode = null;
					atc = "";
					if (!prescription.atcCodes.isEmpty()) {
						Iterator<ATCCode> atcIterator = prescription.atcCodes.iterator();
						if (atcIterator.hasNext()) {
							atcCode = atcIterator.next();
							atc = atcCode.atc;
						}
					}
					if (ATCOfInterest != null) {
						atcOfInterestIterator = ATCOfInterest.iterator();
						boolean atcOK = false;
						while (atcOfInterestIterator.hasNext()) {
							String checkATC = atcOfInterestIterator.next();
							if (checkATC.equalsIgnoreCase(atc.substring(0, checkATC.length()))) {
								atcOK = true;
								break;
							}
						}
						if (atcOK) {
							//System.out.println("      ATC: " + atc + " (" + StringUtilities.daysToSortableDateString(prescription.start - historyWindow) + " <-> " + StringUtilities.daysToSortableDateString(prescription.start) + " - " + StringUtilities.daysToSortableDateString(prescription.start + prescription.duration) + ")");
							prescriptionNumber++;
							String dateString = StringUtilities.daysToSortableDateString(prescription.start);
							
							// Save the basic data of the prescription
							data = new Data();
							data.prescriptionNumber = prescriptionNumber;
							data.atc = atc;
							data.gender = ppe.patient.gender;
							data.age = ppe.patient.AgeOnDate(prescription.start);
							data.year = Integer.parseInt(dateString.substring(0, 4));
							data.month = Integer.parseInt(dateString.substring(4,6));
							data.duration = prescription.duration;
							if (atcCode != null) {
								if (atcCode.dose != null) {
									data.dosage = atcCode.dose;
								}
							}
								
							// Get the history of events of interest
							if (priorEventsOfInterest != null) {
								eventIterator = ppe.events.iterator();
								while (eventIterator.hasNext()) {
									Event event = eventIterator.next();
									if ((event.date >= ppe.patient.startdate) && (event.date <= ppe.patient.enddate)) {
										eventsOfInterestIterator = priorEventsOfInterest.iterator();
										while (eventsOfInterestIterator.hasNext()) {
											String eventOfInterest = eventsOfInterestIterator.next();
											if (event.date < prescription.start) {
											  if (event.eventType.equals(eventOfInterest)) {
													if (!data.historyOfEventAllTime.containsKey(event.eventType)) {
														data.historyOfEventAllTime.put(event.eventType, 1);
													}
													else {
														data.historyOfEventAllTime.put(event.eventType, data.historyOfEventAllTime.get(event.eventType) + 1);
													}
													if (event.date > (prescription.start - historyWindow)) {
														if (!data.historyOfEventWindow.containsKey(event.eventType)) {
															data.historyOfEventWindow.put(event.eventType, 1);
														}
														else {
															data.historyOfEventWindow.put(event.eventType, data.historyOfEventWindow.get(event.eventType) + 1);
														}
													}
												}
											}
											else {
												break;
											}
										}
									}
								}
							}
								
							// Get the history of prior drugs of interest and
							// the concomitant drugs of interest and
							// the previous prescriptions of the drugs of interest and
							// the next prescriptions of the drugs of interest
							if (priorATCOfInterest != null) {
								prescription2Iterator = ppe.prescriptions.iterator();
								while (prescription2Iterator.hasNext()) {
									Prescription prescription2 = prescription2Iterator.next();
									if (((prescription2.start + prescription2.duration) >= ppe.patient.startdate) &&
									    (prescription2.start <= ppe.patient.enddate)) {
										//System.out.println("        Prescription2: " + prescription2.patientID + " , " + StringUtilities.daysToSortableDateString(prescription2.start) + " , " + Long.toString(prescription2.duration) + " , " + prescription2.getATCCodesAsString() + " (" + StringUtilities.daysToSortableDateString(prescription2.start) + " - " + StringUtilities.daysToSortableDateString(prescription2.start + prescription2.duration) + ")");
										
										if (prescription2.start > (prescription.start + prescription.duration + historyWindow))
											break;

										if (prescription2 != prescription) {
											// Get the history of drugs of interest
											//System.out.print("          Check history = ((" + StringUtilities.daysToSortableDateString(prescription2.start + prescription2.duration) + " < " + StringUtilities.daysToSortableDateString(prescription.start) + ") && (" + StringUtilities.daysToSortableDateString(prescription2.start + prescription2.duration) + " > " + StringUtilities.daysToSortableDateString(prescription.start - historyWindow) + "))");
											if (((prescription2.start + prescription2.duration) < prescription.start) &&
											    ((prescription2.start + prescription2.duration) > (prescription.start - historyWindow))) {
												//System.out.println("   = True");
												atcOfInterestIterator = priorATCOfInterest.iterator();
												while (atcOfInterestIterator.hasNext()) {
													String atcOfInterest = atcOfInterestIterator.next();
													if (atcOfInterest.equalsIgnoreCase(prescription2.getATCCodesAsString().substring(0, atcOfInterest.length()))) {
														if (!data.historyOfDrug.containsKey(atcOfInterest)) {
															data.historyOfDrug.put(atcOfInterest, 1);
														}
														else {
															data.historyOfDrug.put(atcOfInterest, data.historyOfDrug.get(atcOfInterest) + 1);
														}
														//System.out.println("                   historyOfDrug = " + atcOfInterest + " (" + prescription2.getATCCodesAsString() + ")");
													}
												}
											}
											else {
												//System.out.println("   = False");
											}

											// Get the concomitant drugs of interest
											//System.out.print("          Check concomitant = ((" + StringUtilities.daysToSortableDateString(prescription2.start) + " < " + StringUtilities.daysToSortableDateString(prescription.start + prescription.duration) + ") && (" + StringUtilities.daysToSortableDateString(prescription2.start + prescription2.duration) + " > " + StringUtilities.daysToSortableDateString(prescription.start) + "))");
											if ((prescription2.start < (prescription.start + prescription.duration)) &&
											    ((prescription2.start + prescription2.duration) > prescription.start)) {
												//System.out.println("   = True");
												atcOfInterestIterator = concomitantATCOfInterest.iterator();
												while (atcOfInterestIterator.hasNext()) {
													String atcOfInterest = atcOfInterestIterator.next();
													if (atcOfInterest.equalsIgnoreCase(prescription2.getATCCodesAsString().substring(0, atcOfInterest.length()))) {
														if (!data.concomitantUseOfDrug.containsKey(atcOfInterest)) {
															data.concomitantUseOfDrug.put(atcOfInterest, 1);
														}
														else {
															data.concomitantUseOfDrug.put(atcOfInterest, data.concomitantUseOfDrug.get(atcOfInterest) + 1);
														}
														//System.out.println("                   concomitantUseOfDrug = " + atcOfInterest + " (" + prescription2.getATCCodesAsString() + ")");
													}
												}
											}
											else {
												//System.out.println("   = False");
											}
											
											// Get the previous drug of interest
											//System.out.print("          Check previous = ((" + StringUtilities.daysToSortableDateString(prescription2.start) + " < " + StringUtilities.daysToSortableDateString(prescription.start) + ") && (" + StringUtilities.daysToSortableDateString(prescription2.start + prescription2.duration) + " > " + StringUtilities.daysToSortableDateString(prescription.start - historyWindow) + "))");
											if ((prescription2.start < prescription.start) &&
											    ((prescription2.start + prescription2.duration) > (prescription.start - historyWindow))) {
												//System.out.println("   = True");
												atcOfInterestIterator = ATCOfInterest.iterator();
												while (atcOfInterestIterator.hasNext()) {
													String atcOfInterest = atcOfInterestIterator.next();
													if (atcOfInterest.equalsIgnoreCase(prescription2.getATCCodesAsString().substring(0, atcOfInterest.length()))) {
														long timeSince = prescription.start - (prescription2.start + prescription2.duration);
														if (timeSince < data.timeSincePreviousDrugOfInterest) {
															data.timeSincePreviousDrugOfInterest = timeSince;
															data.previousDrugOfInterest = prescription2;
															//System.out.println("                       previousDrugOfInterest = " + atcOfInterest + " (" + prescription2.getATCCodesAsString() + ")"); 
														}
													}
												}
											}
											else {
												//System.out.println("   = False");
											}

/*
											// Get the next drug of interest
											//System.out.print("          Check next = ((" + StringUtilities.daysToSortableDateString(prescription2.start + prescription2.duration) + " > " + StringUtilities.daysToSortableDateString(prescription.start + prescription.duration) + ") && (" + StringUtilities.daysToSortableDateString(prescription2.start) + " < " + StringUtilities.daysToSortableDateString(prescription.start + prescription.duration + historyWindow) + "))");
											if (((prescription2.start + prescription2.duration) > (prescription.start + prescription.duration)) &&
											    (prescription2.start  < (prescription.start + prescription.duration + historyWindow))) {
												//System.out.println("   = True");
												atcOfInterestIterator = ATCOfInterest.iterator();
												while (atcOfInterestIterator.hasNext()) {
													String atcOfInterest = atcOfInterestIterator.next();
													if (atcOfInterest.equalsIgnoreCase(prescription2.getATCCodesAsString().substring(0, atcOfInterest.length()))) {
														long timeUntil = prescription2.start - (prescription.start + prescription.duration);
														if (timeUntil < data.timeUntilNextDrugOfInterest) {
															data.timeUntilNextDrugOfInterest = timeUntil;
															data.nextDrugOfInterest = prescription2.getATCCodesAsString();
															//System.out.println("                   nextDrugOfInterest = " + atcOfInterest + " (" + prescription2.getATCCodesAsString() + ")");
														}
													}
												}
											}
											else{
												//System.out.println("   = False");
											}
*/
										}
									}
								}
							}
						}
					}
						
					// Write the record if there is one.
					if (data != null) {
						if (!headerWritten) {
							data.writeHeaderToFile(targetFile);
							headerWritten = true;
						}
						data.writeToFile(targetFile);
					}
				}
			}
		}
			
		targetFile.close();
	}

	
	private class Data {
		int prescriptionNumber = -1;
		String atc = "";
		byte gender = 9;
		int age = -1;
		int year = -1;
		int month = -1;
		long duration = -1;
		String dosage = "";
	  
		String windowDescription = "_" + Integer.toString(historyWindow) + "days";
		Map<String, Integer> historyOfEventWindow = new HashMap<String, Integer>();
		Map<String, Integer> historyOfEventAllTime = new HashMap<String, Integer>();
		Map<String, Integer> historyOfDrug = new HashMap<String, Integer>();
		Map<String, Integer> concomitantUseOfDrug = new HashMap<String, Integer>();
		Prescription previousDrugOfInterest = null;
		long timeSincePreviousDrugOfInterest = Long.MAX_VALUE;
//		String nextDrugOfInterest = "";
//		long timeUntilNextDrugOfInterest = Long.MAX_VALUE;
		
		public void writeHeaderToFile(WriteCSVFile targetFile) {
			//Write the header of the output file
			Iterator<String> eventIterator;
			Iterator<String> drugIterator;
			List<String> headers = new ArrayList<String>();
	    
			headers.add("ATC");
			headers.add("Prescription");
			headers.add("Age");
			headers.add("Gender");
			headers.add("Year");
			headers.add("Month");
			headers.add("Duration");
			headers.add("Dosage");
	    
			eventIterator = priorEventsOfInterest.iterator();
			while (eventIterator.hasNext()) {
				String event = eventIterator.next();
				headers.add("HistoryOfEvent" + windowDescription + "_" + event);
				headers.add("HistoryOfEvent_AllTime_" + event);
			}
	    
			drugIterator = priorATCOfInterest.iterator();
			while (drugIterator.hasNext()) {
				String drug = drugIterator.next();
				headers.add("HistoryOfDrug" + windowDescription + "_" + drug);
			}
	    
			drugIterator = concomitantATCOfInterest.iterator();
			while (drugIterator.hasNext()) {
				String drug = drugIterator.next();
				headers.add("ConcomitantUseOfDrug_" + drug);
			}
	    
			headers.add("PreviousDrugOfInterest");
			headers.add("DurationPreviousDrugOfInterest");
			headers.add("DosagePreviousDrugOfInterest");
			headers.add("TimeSincePreviousDrugOfInterest");
	    
//			headers.add("NextDrugsOfInterest");
//			headers.add("TimeUntilNextDrugOfInterest");
	    
			targetFile.write(headers);
			//targetFile.flush();
		}
		
		public void writeToFile(WriteCSVFile targetFile) {
			// Write the record to the output file.
			Iterator<String> eventIterator;
			Iterator<String> drugIterator;
			List<String> line = new ArrayList<String>();
			String previousAtc = "";
			Long previousDuration = -1L;
			String previousDosage = "";
			ATCCode previousAtcCode = null;
	    
			line.add(atc);
			//DBID line.add(DBID + "_" + Integer.toString(prescriptionNumber));
			line.add(Integer.toString(prescriptionNumber));
			line.add(Integer.toString(age));
			line.add((gender == Patient.MALE)?"M":(gender == Patient.FEMALE)?"F":"");
			line.add(Integer.toString(year));
			line.add(Integer.toString(month));
			line.add(Long.toString(duration));
			line.add(dosage);
	    
			eventIterator = priorEventsOfInterest.iterator();
			while (eventIterator.hasNext()) {
				String event = eventIterator.next();
				if (historyOfEventWindow.containsKey(event))
					line.add(Integer.toString(historyOfEventWindow.get(event)));
				else
					line.add("0");
				if (historyOfEventAllTime.containsKey(event))
					line.add(Integer.toString(historyOfEventAllTime.get(event)));
				else
					line.add("0");
			}
	    
			drugIterator = priorATCOfInterest.iterator();
			while (drugIterator.hasNext()) {
				String drug = drugIterator.next();
				if (historyOfDrug.containsKey(drug))
					line.add(Integer.toString(historyOfDrug.get(drug)));
				else
					line.add("0");
			}
	    
			drugIterator = concomitantATCOfInterest.iterator();
			while (drugIterator.hasNext()) {
				String drug = drugIterator.next();
				if (concomitantUseOfDrug.containsKey(drug))
					line.add(Integer.toString(concomitantUseOfDrug.get(drug)));
				else
					line.add("0");
			}

			if (previousDrugOfInterest != null) {
				previousDuration = previousDrugOfInterest.duration;
				if (!previousDrugOfInterest.atcCodes.isEmpty()) {
					Iterator<ATCCode> atcIterator = previousDrugOfInterest.atcCodes.iterator();
					if (atcIterator.hasNext()) {
						previousAtcCode = atcIterator.next();
						previousAtc = previousAtcCode.atc;
						if (previousAtcCode != null) {
							if (previousAtcCode.dose != null) {
								previousDosage = previousAtcCode.dose;
							}
						}
					}
				}
				line.add(previousAtc);
				if (previousDuration != -1L) {
					line.add(Long.toString(previousDuration));
				}
				else {
					line.add("");
				}
				line.add(previousDosage);
				if (timeSincePreviousDrugOfInterest == Long.MAX_VALUE)
					line.add("");
				else
					line.add(Long.toString(timeSincePreviousDrugOfInterest));
			}
			else {
				line.add("");
				line.add("");
				line.add("");
				line.add("");
			}

/*
			line.add(nextDrugOfInterest);
			if (timeUntilNextDrugOfInterest == Long.MAX_VALUE)
				line.add("");
			else
				line.add(Long.toString(timeUntilNextDrugOfInterest));
*/
	    
			targetFile.write(line);
			//targetFile.flush();
		}
	}

}
