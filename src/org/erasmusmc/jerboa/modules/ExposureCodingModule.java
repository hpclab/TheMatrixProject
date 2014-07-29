package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.dataClasses.MergedDataFileReader;
import org.erasmusmc.jerboa.dataClasses.MergedDataFileWriter;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.utilities.DateUtilities;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.StringUtilities;

/**
 * This module performs three functions:<BR>
 * <OL>
 * <LI>The exclusive patient time of the input merged data file is converted to non-exclusive patient
 * time (i.e. exposure to each drug is coded separately, independent of any other drugs). The resulting 
 * merged data file will have at most 1 ATC code per row.</LI> 
 * <LI>(optional) Exposure to a drug is split into various exposure episodes according to accumulated 
 * exposure.</LI>
 * <LI>(optional) The time after an exposure is coded separately.</LI>
 * </OL>
 * @author schuemie
 *
 */
public class ExposureCodingModule extends JerboaModule{
  
  public JerboaModule mergedData;
  
  /**
   * Number of days over which exposure to a drug should be accumulated for assigning the exposure codes.<BR>
   * default = 365
   */
  public static long historyLength = 365;
  
  /**
   * default = false
   */
  public boolean enableExposureCoding = false;
  
  /**
   * default = false
   */
  public boolean enablePostExposureCoding = false;
 
  /**
   * The list of exposure codes used when exposure coding is enabled. Each code should be represented as 
   * three semicolon separated values:
   * <OL>
   * <LI>The start number of days of accumulated exposure</LI>
   * <LI>The end number of days of accumulated exposure</LI>
   * <LI>The label of the code</LI>
   * </OL>
   * For example: 0;7;VS
   */
  public List<String> exposureCode = new ArrayList<String>();
  
  /**
   * The list of post exposure codes. See exposureCode for the format. Post exposure code labels should 
   * always start with the letter P.
   */
  public List<String> postExposureCode = new ArrayList<String>();
  
  private List<Code> codes;
  private List<Code> postCodes;
  private static final long serialVersionUID = -4422411384238432005L;
  private long countNew = 0;
  private CountingSet<String> labelcounts;
  
  public static void main(String[] args){
  	String folder = "/home/data/Simulated/";
    //FileSorter.sort("/home/data/ipci/testMerge.txt", new String[]{"PatientID", "Date"});
  	ExposureCodingModule module = new ExposureCodingModule();
  	module.exposureCode.add("0;8;VS");
  	module.exposureCode.add("8;31;S");
  	module.exposureCode.add("31;186;M");
  	module.exposureCode.add("186;99999;L");
  	module.enableExposureCoding = true;
    module.postExposureCode.add("0;30;PX");
    module.enablePostExposureCoding = true;
    module.process(folder + "Mergedata.txt", folder + "/testExp.txt");
  }
  
  protected void runModule(String outputFilename){
  	FileSorter.sort(mergedData.getResultFilename(), new String[]{"PatientID", "Date"});
  	process(mergedData.getResultFilename(), outputFilename);
  }
  
  private List<Code> parseCodes(List<String> codesStrings) {
  	List<Code> codes = new ArrayList<Code>();
  	for (String row : codesStrings){
  		String[] cols = row.split(";");
  		int startDay = Integer.parseInt(cols[0]);
  		int endDay = Integer.parseInt(cols[1]);
  		String label  = cols[2];
  		Code code = new Code(startDay, endDay, label);
  		codes.add(code);
  	}
		return codes;
	}
  
  public void process(String source, String target) {
  	codes = parseCodes(exposureCode);
  	postCodes = parseCodes(postExposureCode);
    MergedDataFileReader in = new MergedDataFileReader(source);
    MergedDataFileWriter out = new MergedDataFileWriter(target);
    labelcounts = new CountingSet<String>();
    int countOriginal = 0;
    countNew = 0;
    String oldPatientID = ""; 
    List<MergedData> history = new ArrayList<MergedData>();
    for (MergedData data : in){

      ProgressHandler.reportProgress();
      countOriginal++;
      if (!data.patientID.equals(oldPatientID)) {
        if (data.patientID.compareTo(oldPatientID) < 0)
          System.err.println("ExposureCoding: Input file not sorted by patientID! ");
        processPatient(history, out);
        history.clear();
        oldPatientID = data.patientID;
      }
      history.add(data);
    }
    processPatient(history, out);
    out.close();
    System.out.println("Original size: " + countOriginal + " prescriptions, after exposure time coding: " + countNew + " prescriptions");
    showLabelCounts(labelcounts);
    
    //Dereference data objects:
    codes = null;
    postCodes = null;
    labelcounts = null;
  }

  private void processPatient(List<MergedData> history, MergedDataFileWriter out) {	
    List<MergedData> mergedHistory = mergeBySingleATC(history);
    if (enableExposureCoding || enablePostExposureCoding)
      sort(mergedHistory);
    
  	if (enablePostExposureCoding)
  		addPostExposureCodes(mergedHistory);
    
    if (enableExposureCoding) {
      for (MergedData data : new ArrayList<MergedData>(mergedHistory)){ //make a copy of history, as it will be destroyed in accumulate()
  			for (MergedData newData : addExposureCodes(data, mergedHistory)){ 
  			  out.write(newData);
  			  countNew++;
  		  }
      }
    } else {
    	for (MergedData data : mergedHistory){
    		out.write(data);
    		countNew++;	
    	}
    }
  }

  private void addPostExposureCodes(List<MergedData> mergedHistory) {
  	List<MergedData> newData = new ArrayList<MergedData>();
		Map<ATCCode, Long> atc2lastExposure = new HashMap<ATCCode, Long>(); 
		MergedData currentBackground = null;
  	for (MergedData data : mergedHistory){
  		if (data.atcCodes.iterator().next().atc.equals("")){
  			
  			//New background. First finish up codes in old background:
  			if (currentBackground != null)
  			  for (Map.Entry<ATCCode, Long> entry : atc2lastExposure.entrySet())
  				  generatePostExposureCode(newData, entry.getKey(), entry.getValue(), currentBackground.getEnd(), currentBackground);
  			
  			//Then, replace background:
  			currentBackground = data;
  		} else {
  		
  			long endDate = data.getEnd();
  			for (ATCCode atc : data.atcCodes){
  				Long lastExposure = atc2lastExposure.get(atc);
  				if (lastExposure != null) //Repeat prescription. Previous postexposure codes stop here:
  					generatePostExposureCode(newData, atc, lastExposure, data.start, currentBackground);

  				atc2lastExposure.put(atc, endDate);
  			}
  		}
		}
		//Finish up codes in old background:
		for (Map.Entry<ATCCode, Long> entry : atc2lastExposure.entrySet())
			generatePostExposureCode(newData, entry.getKey(), entry.getValue(), currentBackground.getEnd(), currentBackground);
		mergedHistory.addAll(newData);
		countLabels(labelcounts, newData);
	}

	private void generatePostExposureCode(List<MergedData> data, ATCCode atc, Long lastExposure, long currentDate, MergedData background) {
		if (background.start == currentDate || lastExposure == currentDate)
			return;
		for (Code code : postCodes){
			long codeStart = lastExposure + code.start;
			long codeEnd = lastExposure + code.end;
			if (codeStart < currentDate && codeEnd > background.start){
				codeStart = Math.max(codeStart, background.start);
				codeEnd = Math.min(codeEnd, currentDate);
				MergedData newData = new MergedData(background);
				newData.start = codeStart;
				newData.duration = codeEnd - codeStart;
				// Keep only events in new episode:
				Iterator<Event> iterator = newData.events.iterator();
				while (iterator.hasNext()){
					Event event = iterator.next();
					if (event.date <= newData.start || event.date > newData.getEnd())
						iterator.remove();
				}
				newData.atcCodes.clear();
				ATCCode newATC = new ATCCode(atc);
				newATC.exposureCode = code.code;
				newData.atcCodes.add(newATC);
				data.add(newData);
			}
		}
	}

	private List<MergedData> addExposureCodes(MergedData data, List<MergedData> mergedHistory) {
  	List<MergedData> newDatas;
  	ATCCode atc = data.atcCodes.iterator().next();
  	if (atc.atc.length() == 0 || atc.exposureCode != null){ //non-exposure and post exposure are not combined with exposure codes
  		newDatas = new ArrayList<MergedData>(1);
  		newDatas.add(data);
  	} else {
  		long accumulatedExposure = accumulate(mergedHistory, data.atcCodes, data.start-historyLength*DateUtilities.day, data.start);
  		newDatas = chopPrescription(data, accumulatedExposure);
  		countLabels(labelcounts, newDatas);
  	}
  	return newDatas;
  }
	
  
  private void sort(List<MergedData> history) {
    Collections.sort(history, new Comparator<MergedData>(){
      public int compare(MergedData o1, MergedData o2) {
        if (o1.start == o2.start)
          return o1.atcCodes.iterator().next().compareTo(o2.atcCodes.iterator().next());
        else if (o1.start < o2.start)
          return -1;
        else 
          return 1;
      }});
  }

  private List<MergedData> mergeBySingleATC(List<MergedData> history) {
    List<MergedData> result = new ArrayList<MergedData>();
    List<MergedData> activeEpisodes = new ArrayList<MergedData>();
    for (MergedData data : history){
			data.atcCodes.add(new ATCCode(""));
      for (ATCCode atc : data.atcCodes){
        MergedData activeEpisode = findActiveEpisode(activeEpisodes, atc, data);
        if (activeEpisode != null) {//add to an active episode
          activeEpisode.duration = data.getEnd() - activeEpisode.start;
          activeEpisode.events.addAll(data.events);
        } else { // add as a new episode
          MergedData newEpisode = new MergedData(data);
          newEpisode.atcCodes.clear();
          newEpisode.atcCodes.add(atc);
          activeEpisodes.add(newEpisode);
        }
      }
      addCompletedEpisodes(result, activeEpisodes, data.start);
    }
    addCompletedEpisodes(result, activeEpisodes, Long.MAX_VALUE);
    return result;
  }

  private void addCompletedEpisodes(List<MergedData> result, List<MergedData> activeEpisodes, long now) {
    Iterator<MergedData> iterator = activeEpisodes.iterator();
    while (iterator.hasNext()){
      MergedData activeEpisode = iterator.next();
      if (activeEpisode.getEnd() < now){
        result.add(activeEpisode);
        iterator.remove();
      }
    }   
  }
  private MergedData findActiveEpisode(List<MergedData> activeEpisodes, ATCCode atc, MergedData data) {
    if (StringUtilities.daysToSortableDateString(data.start).substring(4).equals("0101"))
      return null; // Don't merge over year boundaries if already split on boundaries
    for (MergedData activeEpisode : activeEpisodes)
      if (activeEpisode.getEnd() == data.start && 
          activeEpisode.gender == data.gender && 
          activeEpisode.ageRange.equals(data.ageRange) && 
          activeEpisode.atcCodes.iterator().next().equals(atc) &&
          activeEpisode.precedingEventTypes.equals(data.precedingEventTypes) &&
          activeEpisode.outsideCohortTime == data.outsideCohortTime)
        return activeEpisode;
    return null;
  }
  
  
  
  private long accumulate(List<MergedData> history, Set<ATCCode> atcCodes, long start, long end) {
    long result = 0;
    Iterator<MergedData> iterator = history.iterator();
    while (iterator.hasNext()){
      MergedData prescription = iterator.next();
      if (prescription.getEnd() < start)
        iterator.remove();
      else if (prescription.start < start && prescription.atcCodes.equals(atcCodes)){
        result += Math.min(end, prescription.getEnd()) - Math.max(start, prescription.start);
      }
    }
    return result;
  }


  private void showLabelCounts(CountingSet<String> labelcounts) {
    for (String label : labelcounts){
      int n = labelcounts.getCount(label);
      System.out.println(label + "\t" + n + " (partial) prescriptions");
    }
  }

  private void countLabels(CountingSet<String> labelcounts, List<MergedData> newPrescriptions) {
    for (MergedData recipe : newPrescriptions){
      for (ATCCode atc : recipe.atcCodes)
        if (atc.exposureCode != null)
        labelcounts.add(atc.exposureCode);
    }
  }

  private List<MergedData> chopPrescription(MergedData data, long accumulatedExposure) {
    List<MergedData> result = new ArrayList<MergedData>();
    for (Code code : codes){
      long codeStart = code.start + data.start - accumulatedExposure;
      long codeEnd = code.end + data.start - accumulatedExposure;
      if (codeEnd > data.start && codeStart < data.getEnd()){
        MergedData codedPrescription = new MergedData(data);
        codedPrescription.start = Math.max(codeStart, data.start);
        codedPrescription.duration = Math.min(codeEnd, data.getEnd()) - codedPrescription.start;
        codedPrescription.atcCodes = addCode(code.code, data.atcCodes);
        codedPrescription.events = addEvents(data.events, codeStart, codeEnd);
        result.add(codedPrescription);        
      }
    }
    long total = 0;
    for (MergedData p : result){
      total += p.duration;
    }
    if (total != data.duration)
      System.err.println("Prescription chopping error: " + data.patientID + " start: " + data.start);
    return result;
  }
  
  private List<Event> addEvents(List<Event> events, long start, long end) {
    List<Event> result = new ArrayList<Event>(0);
    for (Event event : events)
      if (event.date > start && event.date <= end)
        result.add(event);
    return result;
  }

  private static class Code{
    long start;
    long end;
    public String code;
    public Code(int startday, int endday, String code){
      start = startday;
      end = endday;
      this.code = code;
    }
  }

  private static Set<ATCCode> addCode(String code, Set<ATCCode> atcCodes) {
  	Set<ATCCode> result = new HashSet<ATCCode>(atcCodes.size());
    for (ATCCode atcCode : atcCodes){
    	ATCCode newCode = new ATCCode(atcCode);
    	newCode.exposureCode = code;
    	result.add(newCode);
    }
    return result;
  }
}
