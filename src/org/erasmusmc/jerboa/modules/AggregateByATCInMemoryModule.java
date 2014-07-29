package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.collections.SparseHighDimensionalMap;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.MergedDataFileReader;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.userInterface.VirtualTable;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Module for quickly generating aggregated tables. Does not require sorted input file. Can also consume a virtual table. Does not generate subject counts
 * @author schuemie
 *
 */
public class AggregateByATCInMemoryModule extends JerboaModule{

	public JerboaModule mergedData;
	
  /**	
   * Create aggregates for each calendar year.<BR>
   * Warning: the input table should be split by calendar year (i.e it should not contain episodes spanning
   * two years).<BR>
   * default = false 
   */
  public boolean includeYear = true;

	private SparseHighDimensionalMap<String, Data> dataMap;
	private static int defaultNumberOfDims = 4; //ATC, Age, Gender, PrecedingEventTypes
	private static final long serialVersionUID = 7151787555079000259L;
	
	public static void main(String[] args){
		String folder = "x:/EUADR Study5/";
		AggregateByATCInMemoryModule module = new AggregateByATCInMemoryModule();
		module.process(folder+"MergebyATC.txt", folder+"test.txt");
	}
	
  protected void runModule(String outputFilename){
		if (mergedData.isVirtual()){
			@SuppressWarnings("unchecked")
			VirtualTable<MergedData> virtualTable = (VirtualTable<MergedData>) mergedData;
			process(virtualTable.getIterator(), outputFilename);
		} else {
			process(mergedData.getResultFilename(),outputFilename);
		}
  }

	public void process(String source, String target) {
		Iterator<MergedData> mergedDateIterator = new MergedDataFileReader(source).iterator();
		process(mergedDateIterator, target);
	}

	public void process(Iterator<MergedData> mergedDateIterator, String target) {
		readData(mergedDateIterator);
		writeData(target);
		
		//Dereference data objects:
		dataMap = null;
	}
	
	private void readData(Iterator<MergedData> iterator) {
		dataMap = new SparseHighDimensionalMap<String, Data>(defaultNumberOfDims+(includeYear?1:0));
		while (iterator.hasNext()){
			MergedData mergedData = iterator.next();
			String[] key;
			if (includeYear){
				if (mergedData.year == -1)
					throw new RuntimeException("Aggregating per year, but input file not split by year.");
				key = new String[]{mergedData.getATCCodesAsString(),mergedData.ageRange, (mergedData.gender==Patient.MALE?"M":(mergedData.gender==Patient.FEMALE?"F":"")),StringUtilities.join(mergedData.precedingEventTypes,"+"),Integer.toString(mergedData.year)};
			} else
				key = new String[]{mergedData.getATCCodesAsString(),mergedData.ageRange, (mergedData.gender==Patient.MALE?"M":(mergedData.gender==Patient.FEMALE?"F":"")),StringUtilities.join(mergedData.precedingEventTypes,"+")};
			Data data = dataMap.get(key);
			if (data == null){
				data = new Data();
				dataMap.put(data, key);
			}
			data.patientTime += mergedData.duration;
			for (Event event : mergedData.events)
			  data.eventCounts.add(event.eventType);
		}
	}
	
	private void writeData(String target) {
		WriteCSVFile out = new WriteCSVFile(target);
		out.write(generateHeader());
		Iterator<SparseHighDimensionalMap.Entry<String, Data>> iterator = dataMap.iterator();
		while (iterator.hasNext()){
			SparseHighDimensionalMap.Entry<String, Data> entry = iterator.next();
			List<String> row = new ArrayList<String>();
			for (String key : entry.keys)
				row.add(key);
			row.add(Long.toString(entry.value.patientTime));
			row.add(eventsToString(entry.value.eventCounts));
			out.write(row);
		}		
		out.close();
	}
	
  private List<String> generateHeader() {
    List<String> header = new ArrayList<String>();
    header.add("ATC");
    header.add("AgeRange");
    header.add("Gender");
    header.add("PrecedingEventTypes");
    if (includeYear)
    	header.add("Year");
    header.add("Days");
    header.add("Events");
    return header;
  }
  
	public String eventsToString(CountingSet<String> counts) {
		StringBuilder sb = new StringBuilder();
		for (String eventType : counts){
			if (sb.length() != 0)
				sb.append("+");
			sb.append(eventType);
			sb.append(":");
			sb.append(counts.getCount(eventType));
		}
		return sb.toString();	
	}

	private class Data {
		long patientTime;
		CountingSet<String> eventCounts = new CountingSet<String>(0);
	}
}
