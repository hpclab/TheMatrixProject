package org.erasmusmc.jerboa.postProcessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

public class CombineRRsWithLEOPARD {
	
	public static boolean filter = true;
	public static double threshold = 0.5;
	private static int atcLevel;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String folder = "C:/home/data/Pooled/Gold/";
		filter = true;
		process(folder+"SignalList.csv",folder+"SCCSresults.csv",folder+"SignalListSCCS.csv");
		//process(folder+"Correlations.csv",folder+"PrescriptionstartprofilingLegalATCs.txt",folder+"SignalsCorrelationsLEOPARD.csv");
	}
	
	public static void process(String sourceRR, String sourceLEOPARD, String target){
		Map<String, List<String>> drugEvent2LEOPARD = loadLEOPARD(sourceLEOPARD);
		int atcCol;
		int eventTypeCol;
		Iterator<List<String>> iterator = new ReadCSVFile(sourceRR).iterator();
		WriteCSVFile out = new WriteCSVFile(target);
		List<String> header = iterator.next();
		atcCol = header.indexOf("ATC");
		eventTypeCol = header.indexOf("EventType");
		header.add("CI95Down(SCCS)");
		header.add("CI95Up(SCCS)");
		header.add("p(SCCS)");
		header.add("RR(SCCS)");
		out.write(header);
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			String atc = cells.get(atcCol);
			atc = atc.substring(0,Math.min(atc.length(), atcLevel));
			String drugEvent = atc + "\t" + cells.get(eventTypeCol);
			List<String> leopard = drugEvent2LEOPARD.get(drugEvent);
			if (leopard == null){
				cells.add("");
				cells.add("");
				cells.add("");
				cells.add("");
			} else
			  cells.addAll(leopard);
			//if (!filter || leopard.equals("") || Double.parseDouble(leopard) >= threshold)
			  out.write(cells);
		}
		out.close();
	}

	private static Map<String, List<String>> loadLEOPARD(String filename) {
		int atcCol;
		int eventTypeCol;

		atcLevel = 0;
		Map<String, List<String>> drugEvent2LEOPARD = new HashMap<String, List<String>>();
		Iterator<List<String>> iterator = new ReadCSVFile(filename).iterator();
		List<String> header = iterator.next();
		atcCol = header.indexOf("ATC");
		eventTypeCol = header.indexOf("EventType");
		int leopardCol1 = header.indexOf("CI95Down");
		int leopardCol2 = header.indexOf("CI95Up");
		int leopardCol3 = header.indexOf("p");
		int leopardCol4 = header.indexOf("ExpBeta");
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			String atc = cells.get(atcCol);
			String eventType = cells.get(eventTypeCol);
			List<String> data = new ArrayList<String>();
			data.add(cells.get(leopardCol1));
			data.add(cells.get(leopardCol2));
			data.add(cells.get(leopardCol3));
			data.add(cells.get(leopardCol4));
			drugEvent2LEOPARD.put(atc + "\t" + eventType, data);
			atcLevel = Math.max(atcLevel, atc.length());
		}
		System.out.println("ATC level = " + atcLevel);
		return drugEvent2LEOPARD;
	}

}
