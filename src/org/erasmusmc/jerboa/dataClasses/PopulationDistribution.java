package org.erasmusmc.jerboa.dataClasses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PopulationDistribution {
	private Map<String, Double> age2count = new HashMap<String, Double>();
	private double totalPatientTime = 0;
	
	
  public PopulationDistribution(List<String> table){
  	for (String line : table){
  		String[] cells = line.split(";");
  		String age = cells[0];
  		Double patientTime = Double.parseDouble(cells[1]);
  		age2count.put(age, patientTime);
  		totalPatientTime += patientTime;
  	}
  }
  
  public Set<String> getAgeGroups(){
  	return age2count.keySet();
  }
  
  public double getNormalisedCount(String age){
    Double count = age2count.get(age);
    return count / (double)totalPatientTime;
  }  
  
}
