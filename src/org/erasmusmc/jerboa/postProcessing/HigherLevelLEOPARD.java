package org.erasmusmc.jerboa.postProcessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.erasmusmc.jerboa.modules.EventProfilingModule;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

public class HigherLevelLEOPARD {

	public static int atcLevel = 4;
	
	public static void main(String[] args){
		String folder = "/home/data/Pooled/Gold/FixLEOPARD/";
		//deletedLinesFile = folder + "deleted.csv";
		process(folder+"PooledStartProfiles.csv", folder+"PooledStartProfilesLevel4.csv");
	}
	
	public static void process(String source, String target) {
		//Load file
		Map<String, int[]> drugEvent2dataPoints = new HashMap<String, int[]>();
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next();
		int atcCol = header.indexOf("ATC");
		int eventTypeCol = header.indexOf("EventType");
		int beginCol = -1;
		int endCol = 0;
		for (int i = 0; i < header.size(); i++)
			if (header.get(i).startsWith("Datapoint")){
				if (beginCol == -1)
					beginCol = i;
				endCol = i;
			}
		int nrOfDataCols = endCol - beginCol + 1;
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			if (cells.get(beginCol).equals(""))
				continue;
			String atc = cells.get(atcCol);
			if (atc.length() >= atcLevel){
				String drugEvent = atc.substring(0,atcLevel) + "\t" + cells.get(eventTypeCol);
				int[] dataPoints = drugEvent2dataPoints.get(drugEvent);
				if (dataPoints == null){
					dataPoints = new int[nrOfDataCols];
					for (int i = 0; i < nrOfDataCols; i++)
						dataPoints[i] = 0;
					drugEvent2dataPoints.put(drugEvent, dataPoints);
				}
				for (int i = beginCol; i <= endCol; i++)
					dataPoints[i-beginCol] += Integer.parseInt(cells.get(i));
			}
		}
    
		//Write file
		WriteCSVFile out = new WriteCSVFile(target);
		List<String> newHeader = new ArrayList<String>();
		newHeader.add("ATC");
		newHeader.add("EventType");
		for (int i = beginCol; i <= endCol; i++)
			newHeader.add(header.get(i));
		newHeader.add("P(Level " + atcLevel + ")");
		out.write(newHeader);
		for (String drugEvent : drugEvent2dataPoints.keySet()){
			int[] dataPoints = drugEvent2dataPoints.get(drugEvent);
			String[] parts = drugEvent.split("\t");
			List<String> cells = new ArrayList<String>();
			cells.add(parts[0]);
			cells.add(parts[1]);
			for (int i = 0; i < nrOfDataCols; i++)
				cells.add(Integer.toString(dataPoints[i]));
			cells.add(Double.toString(EventProfilingModule.calculateP(dataPoints)));
			out.write(cells);
		}
		out.close();
	}

}
