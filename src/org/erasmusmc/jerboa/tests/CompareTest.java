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

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.MergedDataFileReader;

public class CompareTest {

	public static String filename1 = "/home/data/IPCI/Jerboa2.3Test/Exposurecoding.txt";
	public static String filename2 = "/home/data/IPCI/Jerboa2.3Test/ShortcutTest/MergebyATC.txt";
  public static String target = "";
  public static String disease = "ARF";
	
	public static void main(String[] args) {
		CountingSet<String> counts1 = patientCount(filename1);
		CountingSet<String> counts2 = patientCount(filename2);
		for (String patient : counts1)
			if (counts1.getCount(patient) != counts2.getCount(patient))
				System.out.println(patient + ": " + counts1.getCount(patient)+ " " + counts2.getCount(patient));


	}
	
	private static CountingSet<String> patientCount(String filename){
		CountingSet<String> counts = new CountingSet<String>();
		for (MergedData md : new MergedDataFileReader(filename)){
			if (!md.outsideCohortTime && !md.precedingEventTypes.contains(disease) && md.atcCodes.size() ==0)
				counts.add(md.patientID+":"+md.ageRange, (int)md.duration);
		}
		return counts;
	}
	


}
