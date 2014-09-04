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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MergedData extends Prescription{
  public List<Event> events = new ArrayList<Event>(0);
  public String ageRange;
  public byte gender;
  public boolean outsideCohortTime = false;
  public int year = -1;
  public int month = -1;
  public int week = -1;
  public MergedData(Prescription prescription){
    super(prescription); 
  }
  public Set<String> precedingEventTypes = new HashSet<String>(0);
  
  public MergedData(MergedData mergedData){
    super(mergedData); 
    this.events = new ArrayList<Event>(mergedData.events);
    this.ageRange = mergedData.ageRange;
    this.gender = mergedData.gender;
    this.precedingEventTypes = new HashSet<String>(mergedData.precedingEventTypes);
    this.outsideCohortTime = mergedData.outsideCohortTime;
    this.year = mergedData.year;
    this.month = mergedData.month;
    this.week = mergedData.week;
  }
  
  public MergedData(){
  }
}
