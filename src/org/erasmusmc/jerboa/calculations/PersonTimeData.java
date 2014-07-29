/**
 * 
 */
package org.erasmusmc.jerboa.calculations;

public class PersonTimeData {
  public int events = 0;
  public long days = 0;
	public PersonTimeData copy() {
		PersonTimeData copy = new PersonTimeData();
		copy.days = days;
		copy.events = events;
		return copy;
	}
	
	public void add(PersonTimeData data){
		events += data.events;
		days += data.days;
	}
}