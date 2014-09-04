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
import java.util.List;

import org.erasmusmc.jerboa.dataClasses.CaseOrControlFileWriter;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.utilities.FileSorter;

public class CaseCrossoverModule extends CaseControlModule{
	
	//public JerboaModule prescriptions;
	//public JerboaModule patients;
	//public JerboaModule events;
	
	/**
	 * Defines the time points relative to the index date when a 'control' should be sampled. Time point should be
	 * specified in number of days (e.g. "-31").
	 */
	public List<String> controlSampleTimePoint = new ArrayList<String>();

	
	private static final long serialVersionUID = 6857450981403361634L;
	private List<Long> samplePoints;
	
	public static void main(String[] args){
		CaseCrossoverModule module = new CaseCrossoverModule();
		//String folder = "/home/data/Simulated/CC/";
		String folder = "x:/study5/";
		module.outputYear = true;
		module.outputMonth = true;
		module.outputGender = true;
		module.outputAge = true;
		module.outputChronicDiseaseScore = true;
		module.outputDrugCount = true;
		module.outputDaysSinceUse = false;
		module.outputDaysOfUse = false;
		module.timeWindow.add("-30;0;current");
		module.controlSampleTimePoint.add("-31");
		module.controlSampleTimePoint.add("-61");
		module.controlSampleTimePoint.add("-92");
		module.controlSampleTimePoint.add("-122");

		module.process(folder + "Prescriptions.txt",folder + "Cohortentrydate.txt",folder + "Events.txt", folder + "CaseCrossover.txt");
	}
	
	@Override
	protected void runModule(String outputFilename) {
	  FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID","Date"});
		FileSorter.sort(patients.getResultFilename(), new String[]{"PatientID","Startdate"});
	  FileSorter.sort(events.getResultFilename(), new String[]{"PatientID","Date"});
	  process(prescriptions.getResultFilename(), patients.getResultFilename(), events.getResultFilename(), outputFilename);
	}
	
	private void parseControlSampleTimePoints() {
		samplePoints = new ArrayList<Long>();
		for (String timePoint : controlSampleTimePoint)
			samplePoints.add(Long.parseLong(timePoint));
	}

	public void process(String sourcePrescriptions, String sourcePatients, String sourceEvents, String target) {
		copySettingsToParent();
		parseControlSampleTimePoints();
		super.process(sourcePrescriptions,sourcePatients, sourceEvents, null, target);
	}

	/**
	 * Since a case crossover is just a case control with no controls, we're reusing the casecontrol module
	 */
	private void copySettingsToParent() {
		super.controlsPerCase = 0;
	}
	
	/**
	 * Overriding this routine so control time periods are written as separate controls
	 */
	protected void writeCaseOrControl(CaseOrControlFileWriter out,CasePattern casePattern, PatientPrescriptionEvent patientPrescriptionEvent, boolean isCase) {
		super.writeCaseOrControl(out, casePattern, patientPrescriptionEvent, null, true);
		for (long timePoint : samplePoints){
			Event dummyEvent = new Event(casePattern.event);
			dummyEvent.date = casePattern.event.date + timePoint;
			if (dummyEvent.date > patientPrescriptionEvent.patient.startdate && dummyEvent.date <= patientPrescriptionEvent.patient.enddate){
			  CasePattern dummyCasePattern = new CasePattern(dummyEvent, patientPrescriptionEvent, casePattern.id);  
				super.writeCaseOrControl(out, dummyCasePattern, patientPrescriptionEvent, null, false);
			}
		}
	}
}