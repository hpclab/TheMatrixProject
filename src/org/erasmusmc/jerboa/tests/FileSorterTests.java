package org.erasmusmc.jerboa.tests;

import java.util.Iterator;

import org.erasmusmc.utilities.ReadTextFile;

public class FileSorterTests {

	public static String file = "X:/Study5/Prescriptions - Copy V2.txt";
	public static void main(String[] args) {
		System.out.println("Total:"+Runtime.getRuntime().totalMemory());
		System.out.println("Max:"+Runtime.getRuntime().maxMemory());
		System.out.println("Free:"+Runtime.getRuntime().freeMemory());
		System.out.println("Available:"+(Runtime.getRuntime().freeMemory() + Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()));
		
		long start = System.currentTimeMillis();
		//FileSorter.sort(file, "Date");
		//ASCIIStringFileSorter.sort(file, new String[]{"PatientID", "Date"});
		//ASCIIStringFileSorterV2.sort(file, new String[]{"PatientID", "Date"});
		System.out.println(System.currentTimeMillis()-start);
		
		Iterator<String> iterator1 = new ReadTextFile("X:/Study5/Prescriptions - Copy.txt").iterator();
		Iterator<String> iterator2 = new ReadTextFile("X:/Study5/Prescriptions - Copy V2.txt").iterator();
		while (iterator1.hasNext() || iterator2.hasNext()){
			String line1 = iterator1.next();
			String line2 = iterator2.next();
			if (!line1.equals(line2))
				throw new RuntimeException(line1 +" != " + line2);
		}
		
	}

}
