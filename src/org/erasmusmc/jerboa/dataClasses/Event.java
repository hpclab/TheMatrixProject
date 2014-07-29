package org.erasmusmc.jerboa.dataClasses;

public class Event implements Comparable<Event>{

	public long date;
  
  public String patientID;
  
  /**
   * Generic event type (e.g.: AMI for Acute Miocardial Infarction)
   */
  public String eventType;
  
  /**
   * Specific code used to detect the event (e.g.: an ICD-9)
   */
  public String code;
  
  public int compareTo(Event arg0) {
    if (arg0.date > date)
      return -1;
    if (arg0.date < date)
      return 1;
    else 
      return eventType.compareTo(arg0.eventType); 
  }
  
  public Event(){
  }
  
  public Event(Event event){
  	this.date = event.date;
  	this.code = event.code;
  	this.eventType = event.eventType;
  	this.patientID = event.patientID;
  }
  
}
