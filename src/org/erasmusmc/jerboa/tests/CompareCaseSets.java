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
package org.erasmusmc.jerboa.tests;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.erasmusmc.collections.OneToManyList;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.ReadTextFile;

public class CompareCaseSets {

	public static String folder = "X:/Study5/CaseSets - Silvana/";
	public static String fileSilvana = folder + "subjectindex.txt";
	public static String fileJerboa = folder + "CaseSets.csv";
	
	public static void main(String[] args) {
	  OneToManyList<String, String> case2controlsSilvana = loadFileSilvana();
	  OneToManyList<String, String> case2controlsJerboa = loadFileJerboa();
	  for (String caseId : case2controlsSilvana.keySet()){
	  	List<String> controlsSilvana = case2controlsSilvana.get(caseId);
	  	List<String> controlsJerboa = case2controlsJerboa.get(caseId);
	  	for (String control : controlsSilvana)
	  		if (!controlsJerboa.contains(control))
	  			System.out.println("Missing control " + control + " for case " + caseId + " in Jerboa file");
	  	
	  	for (String control : controlsJerboa)
	  		if (!controlsSilvana.contains(control))
	  			System.out.println("Missing control " + control + " for case " + caseId + " in Silvana file");
	  }
	}

	private static OneToManyList<String, String> loadFileJerboa() {
		OneToManyList<String, String> caseSetID2controls = new OneToManyList<String, String>();
		Map<String,String> case2caseSetID = new HashMap<String, String>();
		Iterator<List<String>> iterator = new ReadCSVFile(fileJerboa).iterator();
		List<String> header = iterator.next();
		int eventTypeCol = header.indexOf("EventType");
		int isCaseCol = header.indexOf("IsCase");
		int patientIDCol = header.indexOf("PatientID");
		int caseSetIDCol = header.indexOf("CaseSetID");
		while(iterator.hasNext()){
			List<String> cols = iterator.next();
			String eventType = cols.get(eventTypeCol);
			if (eventType.equals("AMI")){
				boolean isCase = cols.get(isCaseCol).equals("1");
				String caseSetID = cols.get(caseSetIDCol);
				String patientID = cols.get(patientIDCol);
				if (isCase)
					case2caseSetID.put(patientID, caseSetID);
				else
					caseSetID2controls.put(caseSetID, patientID);
			}
		}
		OneToManyList<String, String> case2controls = new OneToManyList<String, String>();
		for (String casePatientID : case2caseSetID.keySet()){
			String caseSetID = case2caseSetID.get(casePatientID);
			for (String control : caseSetID2controls.get(caseSetID))
			  case2controls.put(casePatientID, control);
		}
		return case2controls;
	}

	private static OneToManyList<String, String> loadFileSilvana() {
		OneToManyList<String, String> case2controls = new OneToManyList<String, String>();
		Iterator<String> iterator = new ReadTextFile(fileSilvana).iterator();
		String header = iterator.next();
		while (iterator.hasNext()){
			String line = iterator.next();
			String[] cols = line.split("\t");
			case2controls.put(cols[0], cols[1]);
		}
		return case2controls;
	}

}
