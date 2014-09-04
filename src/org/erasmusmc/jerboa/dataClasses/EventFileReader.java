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

import java.util.Iterator;
import java.util.List;

public class EventFileReader implements Iterable<Event> {
	//public static Set<String> allowedEventTypes = getAllowedEventTypes();
  private String filename;

  public EventFileReader(String filename) {
    this.filename = filename;
  }

  /*
	private static Set<String> getAllowedEventTypes() {
  	Set<String> types = new HashSet<String>();
  	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(EventFileReader.class.getResourceAsStream("EventTypes.txt")));
  	try {
  		while (bufferedReader.ready()){
  			types.add(bufferedReader.readLine());
  		}
  	} catch (IOException e) {
  		e.printStackTrace();
  	}
  	return types;
  }
*/
	public Iterator<Event> iterator() {
    return new EventIterator(filename);
  }

  private class EventIterator extends InputFileIterator<Event> {
    private int date;
    private int patientID;
    private int eventtype;
    private int code;
    
    public EventIterator(String filename) {
      super(filename);
    }
    
    public void processHeader(List<String> row){
      patientID = findIndex("patientID", row);
      date = findIndex("Date", row);
      eventtype = findIndex("EventType", row);
      code = findIndexOptional("Code", row);
    }

    public Event row2object(List<String> columns) throws Exception{
      Event event = new Event();
      event.date = InputFileUtilities.convertToDate(columns.get(date),false);
      event.patientID = columns.get(patientID);
      event.eventType = columns.get(eventtype).toUpperCase();
      //if (!allowedEventTypes.contains(event.eventType) && !StringUtilities.isNumber(event.eventType))
      //	throw new RuntimeException(" Unknown event type: '" + event.eventType + "' ");
      if (code != -1)
      	event.code = columns.get(code);
      return event;
    }
  }
}
