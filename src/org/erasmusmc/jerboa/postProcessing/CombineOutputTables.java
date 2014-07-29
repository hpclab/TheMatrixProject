package org.erasmusmc.jerboa.postProcessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erasmusmc.jerboa.ATC2Name;
import org.erasmusmc.jerboa.modules.RelativeRiskModule;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

public class CombineOutputTables {

	private Map<Signal, Data> signal2data = new HashMap<Signal, Data>();
	private List<String> columnNames = new ArrayList<String>();
	private ATC2Name atc2Name = new ATC2Name(RelativeRiskModule.class.getResourceAsStream("ATC_2008.xml"));
	private Set<Signal> limitSet = null;

	public static void main(String[] args){
		String folder = "/home/schuemie/Research/SignalGenerationCompare/Pharmo - Gold/"; 
		CombineOutputTables tables = new CombineOutputTables();
		tables.add(folder + "Jerboa/LGPSPharmo.csv", new String[]{"Exposure","Events","RRmh","CI95down","CI95up","p","Exact.p"}, new String[]{"Exposure","Events","RR(MH)","CI95down(MH)","CI95up(MH)","p(MH)","p(Exact)"});
		tables.add(folder+"LEOPARD/LEOPARDPharmo.csv", new String[]{"LEOPARDP"}, new String[]{"p(LEOPARD)"});
		tables.add(folder+"LEOPARD/LEOPARD4Pharmo.csv", new String[]{"LEOPARDP"}, new String[]{"p(LEOPARD4)"});
		tables.add(folder+"WP2ValidationSetUpdatedLimitPharmo.csv", new String[]{"POSITIVE"}, new String[]{"WP2 validation"});
		tables.write(folder + "combinedList.csv");
	}

	public void add(String filename, String[] sourceNames, String[] targetNames){
		System.out.println("Adding " + filename);
		if (sourceNames.length != targetNames.length)
			throw new RuntimeException("source names does not match target names length");
		for (String targetName : targetNames)
			columnNames.add(targetName);
		Iterator<List<String>> iterator = new ReadCSVFile(filename).iterator();
		List<String> header = iterator.next();
		int eventTypeCol = -1;
		int atcCol = -1;
		int[] sourceCols = new int[sourceNames.length];
		for (int i = 0; i < header.size(); i++){
			String colName = header.get(i);
			if (colName.equals("EventType"))
				eventTypeCol = i;
			if (colName.equals("ATC"))
				atcCol = i;
			for (int j = 0; j < sourceNames.length; j++)
				if (colName.equals(sourceNames[j]))
					sourceCols[j] = i;
		}
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			String atc;
			if (atcCol == -1)
				atc = "";
			else atc = cells.get(atcCol).trim();
			String eventType = cells.get(eventTypeCol).trim();
			Data data = signal2data.get(new Signal(atc,eventType));
			if (data == null){
				data = new Data();
				signal2data.put(new Signal(atc,eventType), data);
			}
			for (int i = 0; i < sourceCols.length; i++)
				data.put(targetNames[i], cells.get(sourceCols[i]));				
		}
	}

	/**
	 * Limits the output to the signals specified in this file
	 * @param filename
	 */
	public void limitSet(String filename){
		limitSet = new HashSet<CombineOutputTables.Signal>();
		Iterator<List<String>> iterator = new ReadCSVFile(filename).iterator();
		List<String> header = iterator.next();
		int eventTypeCol = -1;
		int atcCol = -1;
		for (int i = 0; i < header.size(); i++){
			String colName = header.get(i);
			if (colName.equals("EventType"))
				eventTypeCol = i;
			if (colName.equals("ATC"))
				atcCol = i;
		}
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			String atc;
			if (atcCol == -1)
				atc = "";
			else atc = cells.get(atcCol).trim();
			String eventType = cells.get(eventTypeCol).trim();
			limitSet.add(new Signal(atc,eventType));
		}
	}

	public void write(String filename){
		WriteCSVFile out = new WriteCSVFile(filename);
		out.write(generateHeader());
		for (Signal signal : signal2data.keySet())
			if (limitSet == null || limitSet.contains(signal)){
				Data data = signal2data.get(signal);
				if (signal.atc.length() > 4){
					Data level4Data = signal2data.get(new Signal(signal.atc.substring(0,4),signal.eventType));
					if (level4Data != null)
						data.putAll(level4Data);
				}
				Data level0Data = signal2data.get(new Signal("",signal.eventType));
				if (level0Data != null)
					data.putAll(level0Data);
				List<String> cells = new ArrayList<String>();
				cells.add(signal.atc);
				cells.add(atc2Name.getName(signal.atc));
				cells.add(signal.eventType);
				for (String columnName : columnNames){
					String value = data.get(columnName);
					if (value == null)
						cells.add("");
					else
						cells.add(value);
				}
				out.write(cells);
			}
		out.close();
	}

	private List<String> generateHeader() {
		List<String> header = new ArrayList<String>();
		header.add("ATC");
		header.add("Name");
		header.add("EventType");
		header.addAll(columnNames);
		return header;
	}

	private static class Signal implements Comparable<Signal>{
		String atc;
		String eventType;
		public Signal(String atc, String eventType){
			this.atc = atc;
			this.eventType = eventType;
		}

		public int hashCode(){
			return atc.hashCode() + eventType.hashCode();
		}

		public boolean equals(Object other){
			if (other instanceof Signal){
				Signal otherSignal = (Signal)other;
				return (otherSignal.atc.equals(atc) && otherSignal.eventType.equals(eventType));
			} else
				return false;
		}
		@Override
		public int compareTo(Signal arg0) {
			int result = atc.compareTo(arg0.atc);
			if (result == 0)
				return eventType.compareTo(arg0.eventType);
			else
				return result;
		}
	}
	private static class Data extends HashMap<String, String>{
		private static final long serialVersionUID = -5157999938108960929L;
	}

}
