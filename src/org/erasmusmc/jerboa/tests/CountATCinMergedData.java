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

import java.util.HashSet;
import java.util.Set;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.MergedDataFileReader;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;

public class CountATCinMergedData {
	public static String targetDrug = "";
	public static String targetEvent = "BE";

	public static void main(String[] args){
		targetDrug = null;
		CountingSet<String> patientExposureCount1 = process("/home/data/IPCI/Mergedata.txt");
		targetDrug = "";
		CountingSet<String> patientExposureCount2 = process("/home/data/IPCI/Test/MergebyATC.txt");
		compare(patientExposureCount1,patientExposureCount2);
	}

	private static void compare(CountingSet<String> patientExposureCount1,CountingSet<String> patientExposureCount2) {
		Set<String> allPIDs = new HashSet<String>(patientExposureCount1);
		allPIDs.addAll(patientExposureCount2);
		for (String patientID : allPIDs){
			int count1 = patientExposureCount1.getCount(patientID);
			int count2 = patientExposureCount2.getCount(patientID);
			if (count1 != count2)
				System.out.println("Patient " + patientID + ": " + count1 + " != " + count2);
		}

	}

	public static CountingSet<String> process(String filename){
		System.out.println("Analyzing " + filename);
		CountingSet<String> patientExposureCount = new CountingSet<String>();
		MergedDataFileReader in = new MergedDataFileReader(filename);
		int eventCount = 0;
		long exposureCount = 0;
		for (MergedData data : in){
			if (data.outsideCohortTime || data.precedingEventTypes.contains(targetEvent))
				continue;
			if (targetDrug == null){
				exposureCount += data.duration;
				patientExposureCount.add(data.patientID, (int)data.duration);
			} else {
				if (data.atcCodes.size() == 0 && targetDrug.length() == 0) 
					data.atcCodes.add(new ATCCode(""));
				for (ATCCode atc : data.atcCodes)
					if (atc.atc.equals(targetDrug)){
						exposureCount += data.duration;
						patientExposureCount.add(data.patientID, (int)data.duration);
						for (Event event : data.events){
							if (event.eventType.equals(targetEvent)){
								eventCount++;
								//System.out.println(event.patientID + "\t" + StringUtilities.daysToSortableDateString(event.date));
							}
						}
					}
			}
			}
			System.out.println("Events: "+eventCount+", exposure: "+exposureCount);
			return patientExposureCount;
		}
	}
