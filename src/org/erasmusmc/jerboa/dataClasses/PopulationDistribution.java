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
