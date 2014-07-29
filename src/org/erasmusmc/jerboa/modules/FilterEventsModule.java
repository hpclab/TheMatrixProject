package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.EventFileWriter;

/**
 * Filters the input event file for events of the event types specified in the types parameter.
 * @author schuemie
 *
 */
public class FilterEventsModule extends JerboaModule {

	public JerboaModule events;
	
	/**
	 * The event types that are retained in the events table.
	 */
	public List<String> types = new ArrayList<String>();
	
	/**
	 * Flag that specifies if the types list contains complete
	 * event types or only the start of event types.
	 * default = false i.e. complete events
	 */
	public boolean startsWith = false;
	
	private static final long serialVersionUID = 8344452432173524236L;

	protected void runModule(String outputFilename){
		process(events.getResultFilename(), outputFilename);
	}

	private void process(String source, String target) {
		boolean eventTypeFound = false;
		
		ProgressHandler.reportProgress();
		//Put allowed types in a lowercase set:
		Set<String> typesSet = new HashSet<String>(types.size());
		for (String type : types)
			typesSet.add(type.toLowerCase()); 

		//Go through events file:
		int originalCount = 0;
		int remainingCount = 0;
		EventFileWriter out = new EventFileWriter(target);
		for (Event event : new EventFileReader(source)){
			originalCount++;
			if (startsWith) {
				eventTypeFound = false;
				Iterator<String> typesIterator = typesSet.iterator();
				while ((!eventTypeFound) && typesIterator.hasNext()) {
					String type = typesIterator.next();
					System.out.print(event.eventType.toLowerCase().substring(0, type.length()) + " ? " + type.toLowerCase() + " ");
					if (event.eventType.substring(0, type.length()).compareToIgnoreCase(type) == 0) {
						out.write(event);
						remainingCount++;
						System.out.println("ACCEPTED");
					}
					else {
						System.out.println("REJECTED");
					}
				}
			}
			else {
				if (typesSet.contains(event.eventType.toLowerCase())){
					out.write(event);
					remainingCount++;
				}
			}
		}
		out.close();
		System.out.println("Original number of events: " + originalCount + ", after filtering: " + remainingCount);
	}
}
