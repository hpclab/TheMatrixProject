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

import java.util.zip.DataFormatException;

import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.utilities.StringUtilities;

public class CountTotalPatientTime {
  public static long refDate = getRefDate();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String folder = "/home/schuemie/Research/temp/";
		//count(folder+"Cohortentrydate.txt");
		count(folder+"jerboapopulatieinput.txt");

	}

	private static long getRefDate() {
		try {
			return StringUtilities.sortableTimeStringToDays("20000101");
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static void count(String string) {
		long sum = 0;
		int count = 0;
		long sumStart = 0;
		long sumEnd = 0;
		for (Patient patient : new PatientFileReader(string)){
			sum += patient.enddate - patient.startdate;
			count++;
			sumStart += patient.startdate-refDate;
			sumEnd += patient.enddate-refDate;
		}
		System.out.println("Total patient time: " + sum + " days (" + (sum/365.25) + " years)");
		System.out.println("Total number of patients: " + count);
		System.out.println("Mean patient time per patient: " + (sum/count)+ " days (" + (sum/count/365.25) + " years)");
		String avgStart = StringUtilities.daysToSortableDateString(refDate+(sumStart/count));
		String avgEnd = StringUtilities.daysToSortableDateString(refDate+(sumEnd/count));
		System.out.println("Average start date: " + avgStart);
		System.out.println("Average end date: " + avgEnd);
	}

}
