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
