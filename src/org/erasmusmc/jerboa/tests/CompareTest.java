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
