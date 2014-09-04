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
package org.erasmusmc.jerboa.dataClasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.erasmusmc.jerboa.dataClasses.CaseOrControl.DrugStats;
import org.erasmusmc.jerboa.dataClasses.CaseOrControl.WindowStats;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class CaseOrControlFileWriter  {
	private WriteCSVFile file;
	private Map<String, Integer> variable2column = new HashMap<String, Integer>();
	private Map<String, Integer> window2column = new HashMap<String, Integer>();
	private int nrOfColumns;
	private boolean outputDaysSinceUse;
	private boolean outputDaysOfUse;
	private boolean outputMainPrescription;
  public static boolean outputPatientID = false; //temporary for debugging


	public CaseOrControlFileWriter(String filename, List<String> variables, List<String> windows, boolean outputDaysSinceUse, boolean outputDaysOfUse, boolean outputMainPrescription){
		file = new WriteCSVFile(filename);
		this.outputDaysSinceUse = outputDaysSinceUse;
		this.outputDaysOfUse = outputDaysOfUse;
		this.outputMainPrescription = outputMainPrescription;
		writeHeader(variables, windows);
	}

	public void write(CaseOrControl caseOrControl){
		List<String> columns = new ArrayList<String>(nrOfColumns);
		for (int i = 0; i < nrOfColumns; i++)
			columns.add("");	
		if (outputPatientID)
		  columns.set(nrOfColumns-1,caseOrControl.patientID);
		columns.set(0, Integer.toString(caseOrControl.caseSetID));
		columns.set(1, caseOrControl.eventType);
		columns.set(2, (caseOrControl.isCase?"1":"0"));
		if (outputMainPrescription){
			ATCCode code = caseOrControl.mainPrescription.atcCodes.iterator().next();
			columns.set(3, code.atc);
			columns.set(4, Long.toString(caseOrControl.mainPrescription.duration));
			columns.set(5, code.dose==null?"":code.dose);
		}
		
		for (Map.Entry<String, String> entry : caseOrControl.variables.entrySet()){
			Integer column = variable2column.get(entry.getKey());
			if (column == null)
				throw new RuntimeException("Unknown variable in case or control: " + entry.getKey());
			columns.set(column, entry.getValue());
		}

		for (Map.Entry<String, WindowStats> entry : caseOrControl.window2stats.entrySet()){
			Integer column = window2column.get(entry.getKey());
			if (column == null)
				throw new RuntimeException("Unknown window in case or control: " + entry.getKey());

			if (outputDaysOfUse){
				StringBuilder use = new StringBuilder();
				for (Map.Entry<String, DrugStats> drugEntry : entry.getValue().entrySet()){
					if (use.length() != 0)
						use.append('+');
					use.append(drugEntry.getKey());
					use.append(':');
					use.append(drugEntry.getValue().daysUsed);
				}
				columns.set(column++, use.toString());
			}

			if (outputDaysSinceUse){
				StringBuilder daysSinceUse = new StringBuilder();
				for (Map.Entry<String, DrugStats> drugEntry : entry.getValue().entrySet()){
					if (daysSinceUse.length() != 0)
						daysSinceUse.append('+');
					daysSinceUse.append(drugEntry.getKey());
					daysSinceUse.append(':');
					daysSinceUse.append(drugEntry.getValue().daysSinceUse);
				}
				columns.set(column, daysSinceUse.toString());
			} 

			if (!outputDaysOfUse && !outputDaysSinceUse){
				columns.set(column, StringUtilities.join(entry.getValue().keySet(), "+"));    		
			}
		}


		file.write(columns);
	}

	private void writeHeader(List<String> variables, List<String> windows) {
		List<String> headers = new ArrayList<String>();
		headers.add("CaseSetID");
		headers.add("EventType");
		headers.add("IsCase");
		if (outputMainPrescription){
		  headers.add("ATC");
		  headers.add("Duration");
		  headers.add("Dose");
		}
		for (String variable : variables){
			variable2column.put(variable, headers.size());
			headers.add(variable);
		}

		for (String window : windows){
			window2column.put(window, headers.size());
			if (outputDaysOfUse)
				headers.add(window + "_DaysOfUse");
			if (outputDaysSinceUse)
				headers.add(window + "_DaysSinceUse");
			if (!outputDaysOfUse && !outputDaysSinceUse)
				headers.add(window);
		}
		if (outputPatientID)
		  headers.add("PatientID");
		file.write(headers);
		nrOfColumns = headers.size();
	}

	public void flush(){
		file.flush();
	}

	public void close(){
		file.close();
	}
}
