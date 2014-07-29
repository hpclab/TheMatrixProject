package org.erasmusmc.jerboa.postProcessing.poolDBs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.erasmusmc.jerboa.modules.EventProfilingModule;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Pools prescription start profiles (i.e. LEOPARD) from different databases, recomputes the p-value.
 * @author schuemie
 *
 */
public class PoolStartProfiles {
	
	/**
	 * Specifies whether the index date should be ignored
	 */
	public static boolean forceDayZeroToZero = false;
	
	private String outputFile;

	private Map<String, StartProfile> drugEvent2StartProfile = new HashMap<String, StartProfile>();
	private List<String> datapointHeaders = null;
	private List<Integer> datapointIndices = null;


	public PoolStartProfiles(String outputFile){
		this.outputFile = outputFile;
	}

	public void add(String inputFile){
		System.out.println("Adding " + inputFile + " to pooled table");
		int totalEventCountCol = -1;
		int atcCol = -1;
		int eventTypeCol = -1;
		int dayZeroCol = -1;
		Iterator<List<String>> iterator = new ReadCSVFile(inputFile).iterator();
		List<String> header = iterator.next();
		datapointHeaders = new ArrayList<String>();
		datapointIndices = new ArrayList<Integer>();
		for (int i = 0; i < header.size(); i++){
			if (header.get(i).equals("ATC"))
				atcCol = i;
			if (header.get(i).equals("EventType"))
				eventTypeCol = i;
			if (header.get(i).equals("Total Event Count"))
				totalEventCountCol = i;
			if (header.get(i).startsWith("Datapoint")){
				datapointHeaders.add(header.get(i));
				datapointIndices.add(i);
				if (header.get(i).equals("Datapoint (0 days)"))
					dayZeroCol = i;
			}
		}
		if (datapointHeaders.size() != 51)
			System.err.println("Size is " + datapointHeaders.size());
		while(iterator.hasNext()){
			List<String> cells = iterator.next();
			String drugEvent = cells.get(atcCol) + "\t" + cells.get(eventTypeCol);
			StartProfile startProfile = drugEvent2StartProfile.get(drugEvent);
			if (startProfile == null){
				startProfile = new StartProfile(datapointIndices.size());
				drugEvent2StartProfile.put(drugEvent, startProfile);
			}
			startProfile.totalEventCount += Integer.parseInt(cells.get(totalEventCountCol));
			for (int i = 0; i < datapointIndices.size(); i++)
				if (!forceDayZeroToZero || datapointIndices.get(i) != dayZeroCol)
				  startProfile.datapoints[i] += Integer.parseInt(cells.get(datapointIndices.get(i)));
		}
	}

	public void close(){
		WriteCSVFile out = new WriteCSVFile(outputFile);
		List<String> header = new ArrayList<String>();
		header.add("ATC");
		header.add("EventType");
		header.add("Total Event Count");
		header.addAll(datapointHeaders);
		header.add("P");
		out.write(header);
		for (String drugEvent : drugEvent2StartProfile.keySet()){
			List<String> cells = new ArrayList<String>();
			String[] parts = drugEvent.split("\t");
			cells.add(parts[0]);
			cells.add(parts[1]);
			StartProfile startProfile = drugEvent2StartProfile.get(drugEvent);
			cells.add(Integer.toString(startProfile.totalEventCount));
			for (int datapoint : startProfile.datapoints)
				cells.add(Integer.toString(datapoint));
			cells.add(Double.toString(EventProfilingModule.calculateP(startProfile.datapoints)));
			out.write(cells);
		}
		out.close();
	}

	private static class StartProfile {
		int totalEventCount = 0;
		int[] datapoints;
		public StartProfile(int datapointCount){
			datapoints = new int[datapointCount];
			for (int i = 0; i < datapointCount; i++)
				datapoints[i] = 0;
		}
	}
}