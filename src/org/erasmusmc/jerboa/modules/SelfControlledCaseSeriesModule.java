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
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.MergedDataFileReader;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.userInterface.VirtualTable;
import org.erasmusmc.utilities.FileShuffler;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;


/**
 * Creates output for a Self-Controlled Case Series analysis.
 * @author schuemie
 *
 */
public class SelfControlledCaseSeriesModule extends JerboaModule{

	public JerboaModule mergedData;

	/**
	 * Output only those patients that have at least one event.<BR>
	 * default = true
	 */
	public boolean keepOnlyCases = true;
	
	/**
	 * If true, the patient IDs are replaced with consecutive numbers in the output file<BR>
	 * default = true;
	 */
	public boolean replacePatientIDs = true;
	
	/**
	 * Shuffle the output to reduce identifyability.<BR>
	 * default = false
	 */
	public boolean shuffleOutput = false;
	
	/**
	 * Add a column with preceding event types in the output file. Allow later censoring of time after event.<BR>
	 * default = true
	 */
	public boolean includePrecedingEventTypes = true;
	
	private boolean hasWeek;
	private boolean hasMonth;
	private boolean hasYear;
	private int newPatientID;
	private boolean headerWritten;
	private static final long serialVersionUID = -7108222834597740095L;
	
	public static void main(String[] args){
		String folder = "x:/study5/";
		SelfControlledCaseSeriesModule module = new SelfControlledCaseSeriesModule();
		module.keepOnlyCases = true;
		module.shuffleOutput = false;
		module.process(folder+"Mergedata.txt", folder+"SCCS.csv");
	}

	protected void runModule(String outputFilename){
		if (mergedData.isVirtual()){
			@SuppressWarnings("unchecked")
			VirtualTable<MergedData> virtualTable = (VirtualTable<MergedData>) mergedData;
			process(virtualTable.getIterator(), outputFilename);
		} else {
			FileSorter.sort(mergedData.getResultFilename(), "PatientID");
			process(mergedData.getResultFilename(),outputFilename);
		}
	}

	public void process(String source, String target) {
		Iterator<MergedData> mergedDateIterator = new MergedDataFileReader(source).iterator();
		process(mergedDateIterator, target);
	}

	public void process(Iterator<MergedData> mergedDateIterator, String target) {
		WriteCSVFile out = new WriteCSVFile(target);
		List<MergedData> patientData = new ArrayList<MergedData>();
		String oldPatient = "";
		newPatientID = 1;
		headerWritten = false;
		while (mergedDateIterator.hasNext()){
		  MergedData data = mergedDateIterator.next();
			if (!data.patientID.equals(oldPatient)){
				processPatient(out, patientData);
				patientData.clear();
				oldPatient = data.patientID;
			}
			if (!data.outsideCohortTime)
			  patientData.add(data);
			ProgressHandler.reportProgress();
		}
		processPatient(out, patientData);
		out.close();
		if (shuffleOutput)
			FileShuffler.shuffle(target);
	}

	private void processPatient(WriteCSVFile out, List<MergedData> patientData) {
		if (!keepOnlyCases || hasEvent(patientData)){
			String patientID = Integer.toString(newPatientID++);
			for (MergedData data : patientData){
				if (!headerWritten)
					writeHeader(out, data);
				
				List<String> row = new ArrayList<String>();
				if (replacePatientIDs)
				  row.add(patientID);
				else
					row.add(data.patientID);
				if (hasYear)
				  row.add(Integer.toString(data.year));
				if (hasMonth)
				  row.add(Integer.toString(data.month));
				if (hasWeek)
				  row.add(Integer.toString(data.week));
				row.add(Long.toString(data.duration));
				row.add(StringUtilities.join(data.atcCodes, "+"));
				row.add((data.gender == Patient.MALE)?"M":"F");
				row.add(data.ageRange);
				row.add(toString(data.events));
				if (includePrecedingEventTypes)
				  row.add(StringUtilities.join(data.precedingEventTypes,"+"));

				out.write(row);
			}
			out.flush();
		}
	}

	private void writeHeader(WriteCSVFile out, MergedData data) {
		hasMonth = (data.month != -1);
		hasYear = (data.year != -1);	
		hasWeek = (data.week != -1);	
		out.write(createHeader());
		headerWritten = true;
	}

	private String toString(List<Event> events) {
		StringBuilder sb = new StringBuilder();
		for (Event event : events){
			if (sb.length() != 0)
				sb.append('+');
			sb.append(event.eventType);
		}
		return sb.toString();
	}

	private boolean hasEvent(List<MergedData> patientData) {
		for (MergedData data : patientData)
			for (Event event : data.events)
				if (!data.precedingEventTypes.contains(event.eventType))
					return true;

		return false;
	}

	private List<String> createHeader() {
		List<String> columns = new ArrayList<String>();
		columns.add("PatientID");
		if (hasYear)
		  columns.add("Year");
		if (hasMonth)
		  columns.add("Month");
		if (hasWeek)
		  columns.add("Week");
		columns.add("Duration");
		columns.add("ATC");
		columns.add("Gender");
		columns.add("AgeRange");
		columns.add("Events");
		if (includePrecedingEventTypes)
			columns.add("PrecedingEventTypes");
		return columns;
	}
}
