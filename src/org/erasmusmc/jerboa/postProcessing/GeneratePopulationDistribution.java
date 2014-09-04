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
package org.erasmusmc.jerboa.postProcessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.DataFormatException;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class GeneratePopulationDistribution {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String folder = "X:/SOS - IPCI 2/";
		//String folder = "X:/Study5/";
		process(folder+"Patients.txt",folder+"Population distribution.csv");

	}

	private static void process(String source, String target) {
		int bestYear = findBestYear(source);
		CountingSet<Integer> ageCounts = getDistribution(source,bestYear);
		writeAgeCounts(ageCounts, target);
	}

	private static void writeAgeCounts(CountingSet<Integer> ageCounts,String target) {
		WriteCSVFile out = new WriteCSVFile(target);
		List<String> header = new ArrayList<String>();
		header.add("Age");
		header.add("Count");
		out.write(header);
		List<Integer> sortedAges = new ArrayList<Integer>(ageCounts);
		Collections.sort(sortedAges);
		for (Integer age : sortedAges){
			List<String> cells = new ArrayList<String>();
			cells.add(age.toString());
			cells.add(Integer.toString(ageCounts.getCount(age)));
			out.write(cells);
		}
		out.close();
	}

	private static CountingSet<Integer> getDistribution(String source,	int targetYear) {
		CountingSet<Integer> ageCounts = new CountingSet<Integer>();
		long targetDate = 0;
		try {
			targetDate = StringUtilities.sortableTimeStringToDays(Integer.toString(targetYear)+"0101");
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		for (Patient patient : new PatientFileReader(source)){
			if (patient.startdate <= targetDate && patient.enddate > targetDate){
				int year = Integer.parseInt(StringUtilities.daysToSortableDateString(patient.birthdate).substring(0,4));
				int ageInYears = targetYear-year-1;
				if (ageInYears >= 0)
					ageCounts.add(ageInYears);
				
				/*long ageInDays = targetDate-patient.birthdate;
				if (ageInDays > 0){
					int ageInYears2 = (int)(ageInDays/365.25);
					if (ageInYears != ageInYears2){
					  String bd = StringUtilities.daysToSortableDateString(patient.birthdate);
					  System.out.println(ageInYears + "\t" + ageInYears2 + "\t" + bd + "\t" + bd.substring(4,6) + "/" + bd.substring(6,8) + "/" + bd.substring(0,4) + "\t" + targetYear + "\t" + ageInYears);
					}
				}*/
			}
		}
		return ageCounts;
	}

	private static int findBestYear(String source) {
		CountingSet<Integer> yearCount = new CountingSet<Integer>();
		for (Patient patient : new PatientFileReader(source)){
			int startYear = Integer.parseInt(StringUtilities.daysToSortableDateString(patient.startdate).substring(0,4));
			int endYear = Integer.parseInt(StringUtilities.daysToSortableDateString(patient.enddate).substring(0,4));
			for (int year = startYear+1; year <= endYear; year++)
				yearCount.add(year);
		}
		List<Integer> sortedYears = new ArrayList<Integer>(yearCount);
		Collections.sort(sortedYears);
		for (int year : sortedYears)
			System.out.println(year + " : " + yearCount.getCount(year) + " subjects");
		int maxYear = -1;
		int maxSubjects = 0;
		for (int year : yearCount)
			if (yearCount.getCount(year) > maxSubjects){
				maxSubjects = yearCount.getCount(year);
				maxYear = year;
			}
		System.out.println("Best year: " + maxYear);
		return maxYear;
	}

}
