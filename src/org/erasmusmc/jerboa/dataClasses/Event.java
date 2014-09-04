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
