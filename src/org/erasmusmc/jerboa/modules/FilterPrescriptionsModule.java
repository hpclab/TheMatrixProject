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
import java.util.Iterator;
import java.util.List;

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.DummyIterator;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEventIterator;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileWriter;
import org.erasmusmc.utilities.FileSorter;

/**
 * Filters the input prescription file for prescriptions with the given ATC codes.
 * @author schuemie
 *
 */
public class FilterPrescriptionsModule extends JerboaModule {

	public JerboaModule prescriptions;

	public JerboaModule patients;

	public JerboaModule events;

	/**
	 * The (partial) ATC codes that are retained in the prescriptions table.
	 */
	public List<String> atcCodes = new ArrayList<String>();

	/**
	 * The (partial) ATC codes that should be excluded in the prescription table
	 */
	public List<String> excludedAtcCodes = new ArrayList<String>();

	/**
	 * Remove prescriptions that fall outside of patient time? Requires patients file to be specified.<BR>
	 * default = false
	 */
	public boolean limitPrescriptionsToPatientTime = false;

	/**
	 * Remove prescriptions that belong to patients without events? Requires patients and events file to be specified.<BR>
	 * default = false 
	 */
	public boolean limitPrescriptionsToPatientsWithEvents = false;

	/**
	 * Remove prescriptions that do not have a correct 7-digit ATC code.<BR>
	 * default = false
	 */
	public boolean limitToLegalATCCodes = false;

	private static final long serialVersionUID = 8344452432173524236L;
	private int originalCount;
	private int remainingCount;
	private int illegalCount;
	private List<String> atcSet;
	private List<String> excludedAtcSet;

	protected void runModule(String outputFilename){
		if (limitPrescriptionsToPatientsWithEvents)
			FileSorter.sort(events.getResultFilename(), new String[]{"PatientID"});

		if (limitPrescriptionsToPatientTime || limitPrescriptionsToPatientsWithEvents){
			FileSorter.sort(patients.getResultFilename(), new String[]{"PatientID", "StartDate"});
			FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID", "Date"});
		}

		process(prescriptions.getResultFilename(), (patients == null?null:patients.getResultFilename()), (events == null?null:events.getResultFilename()), outputFilename);
	}

	private void process(String sourcePrescriptions, String sourcePatients, String sourceEvents, String target) {
		ProgressHandler.reportProgress();
		//Put allowed atcCodes in a lowercase set:
		atcSet = new ArrayList<String>(atcCodes.size());
		for (String type : atcCodes)
			atcSet.add(type.toLowerCase().trim());

		excludedAtcSet = new ArrayList<String>(excludedAtcCodes.size());
		for (String type : excludedAtcCodes)
			excludedAtcSet.add(type.toLowerCase().trim());

		originalCount = 0;
		remainingCount = 0;
		illegalCount = 0;

		PrescriptionFileWriter out = new PrescriptionFileWriter(target);
		if (limitPrescriptionsToPatientTime || limitPrescriptionsToPatientsWithEvents){
			if (sourcePatients == null)
				throw new RuntimeException("Set  to filter on patients, but patient file not specified");
			if (limitPrescriptionsToPatientsWithEvents && (sourceEvents == null))
				throw new RuntimeException("Set  to filter on patients with events, but events file not specified");

			Iterator<Patient> patientIterator = new PatientFileReader(sourcePatients).iterator();
			Iterator<Prescription> prescriptionIterator = new PrescriptionFileReader(sourcePrescriptions).iterator();
			Iterator<Event> eventIterator = sourceEvents == null?new DummyIterator<Event>():new EventFileReader(sourceEvents).iterator();
			Iterator<PatientPrescriptionEvent> iterator = new PatientPrescriptionEventIterator(patientIterator, prescriptionIterator, eventIterator);
			while (iterator.hasNext())
				processPatient(iterator.next(), out);
		} else {
			for (Prescription prescription : new PrescriptionFileReader(sourcePrescriptions)){
				originalCount++;
				if (keep(prescription)){
					out.write(prescription);
					remainingCount++;
				}
			}
		}
		out.close();
		if (limitToLegalATCCodes)
			System.out.println("Removed " + illegalCount + " prescriptions with illegal ATC codes");
		System.out.println("Original number of prescriptions: " + originalCount + ", after filtering: " + remainingCount);

		//Dereference objects:
		atcSet = null;
		excludedAtcSet = null;
	}

	private boolean keep(Prescription prescription) {
		String atc = prescription.atcCodes.iterator().next().atc;
		if (atcCodes.size() != 0 && !contains(atcSet,atc.toLowerCase()))
			return false;
		else if (excludedAtcCodes.size() != 0 && contains(excludedAtcSet,atc.toLowerCase()))
			return false;
		else if (limitToLegalATCCodes && !isLegalATC(atc)){
			illegalCount++;
			return false;
		} else
			return true;
	}

	private void processPatient(PatientPrescriptionEvent data, PrescriptionFileWriter out) {
		originalCount += data.prescriptions.size();

		if (limitPrescriptionsToPatientsWithEvents && data.events.size() == 0)
			return;

		if (limitPrescriptionsToPatientTime){
			Patient patient = data.patient;
			for (Prescription prescription : data.prescriptions){
				if (prescription.getEnd() > patient.startdate){
					if (prescription.start < patient.startdate){ 
						long end = prescription.getEnd();				
						prescription.start = patient.startdate;
						prescription.duration = end - prescription.start;
					} else if (prescription.start >= patient.enddate)
						break;
					if (prescription.getEnd() > patient.enddate)
						prescription.duration = patient.enddate - prescription.start;
					
					if (keep(prescription)){
						out.write(prescription);
						remainingCount++;
					}
				}
			}
		} else {
			for (Prescription prescription : data.prescriptions)
				if (keep(prescription)){
					out.write(prescription);
					remainingCount++;
				}
		}
	}

	private boolean contains(List<String> allowedATCs, String atc) {
		for (String allowedATC : allowedATCs)
			if (atc.startsWith(allowedATC))
				return true;
		return false;
	}

	private boolean isLegalATC(String atc) {
		if (atc.length() == 7)
			if (Character.isLetter(atc.charAt(0)) && legalStart(atc.charAt(0)))
				if (Character.isDigit(atc.charAt(1)))
					if (Character.isDigit(atc.charAt(2)))
						if (Character.isLetter(atc.charAt(3)))
							if (Character.isLetter(atc.charAt(4)))
								if (Character.isDigit(atc.charAt(5)))
									if (Character.isDigit(atc.charAt(6)))
										return true;
		return false;
	}


	private boolean legalStart(char ch) {
		return (ch == 'A' || ch == 'B' || ch == 'C' || ch == 'D' || ch == 'G' || ch == 'H' || ch == 'J' || ch == 'L' || ch == 'M' || ch == 'N' || ch == 'P' || ch == 'R' || ch == 'S' || ch == 'V');
	}
}
