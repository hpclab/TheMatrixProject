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
package org.erasmusmc.jerboa.modules;

import java.util.HashSet;
import java.util.Set;

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.EventFileWriter;
import org.erasmusmc.utilities.FileSorter;

/**
 * For every subject, only the first event of an event type is kept, all the other events are deleted. 
 * Thus, only the incident event occurrences remain.
 * @author schuemie
 *
 */
public class KeepOnlyFirstEventModule extends JerboaModule {

	public JerboaModule events;
	
	private static final long serialVersionUID = 6086331385395712581L;

	protected void runModule(String outputFilename){
		FileSorter.sort(events.getResultFilename(), new String[]{"PatientID","Date"});
		process(events.getResultFilename(), outputFilename);
	}

	public void process(String source, String target) {
		ProgressHandler.reportProgress();
		EventFileWriter out = new EventFileWriter(target);
		String oldPatientID = "";
		Set<String> previousEventTypes = new HashSet<String>();
		int count = 0;
		int deletedCount = 0;
		for (Event event : new EventFileReader(source)){
			count++;
			if (!event.patientID.equals(oldPatientID)){
				previousEventTypes.clear();
				oldPatientID = event.patientID;
			}
			if (previousEventTypes.add(event.eventType)){
				out.write(event);
			} else
				deletedCount++;
		}
		out.close();
		System.out.println("Removed " + deletedCount +" of " + count + " events");
	}
}
