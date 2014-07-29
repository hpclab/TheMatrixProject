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
