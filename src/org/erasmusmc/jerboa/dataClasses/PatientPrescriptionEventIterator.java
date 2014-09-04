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

/**
 * Class for iterating through patients, together with all their prescriptions and events.
 * @author schuemie
 *
 */
public class PatientPrescriptionEventIterator implements Iterator<PatientPrescriptionEvent>{

	private Iterator<Patient> patientIterator;
	private Iterator<Prescription> prescriptionIterator;
	private Iterator<Event> eventIterator;
	private PatientPrescriptionEvent buffer;
	private Prescription prescription;
	private Event event;
	
	public PatientPrescriptionEventIterator(Iterator<Patient> patientIterator, Iterator<Prescription> prescriptionIterator, Iterator<Event> eventIterator){
		this.patientIterator = patientIterator;
		this.prescriptionIterator = prescriptionIterator;
		this.eventIterator = eventIterator;
		startRead();
	}
	
	private void startRead(){
    if (prescriptionIterator.hasNext())
      prescription = prescriptionIterator.next();
    if (eventIterator.hasNext())
    	event = eventIterator.next();
    readNext();
	}
	
	public PatientPrescriptionEventIterator(String prescriptionsFilename, String patientsFilename, String eventsFilename) {
	  this.patientIterator = new PatientFileReader(patientsFilename).iterator();
		
		if (prescriptionsFilename == null)
			this.prescriptionIterator = new DummyIterator<Prescription>();
		else
			this.prescriptionIterator = new PrescriptionFileReader(prescriptionsFilename).iterator();
		
		if (eventsFilename == null)
			this.eventIterator = new DummyIterator<Event>();
		else
			this.eventIterator = new EventFileReader(eventsFilename).iterator();
		
		startRead();
	}
	
	@Override
	public boolean hasNext() {
		return (buffer != null);
	}

	@Override
	public PatientPrescriptionEvent next() {
		PatientPrescriptionEvent result = buffer;
		readNext();
		return result;
	}

	private void readNext() {
		if (patientIterator.hasNext()){
			PatientPrescriptionEvent oldBuffer = buffer;
			buffer = new PatientPrescriptionEvent();
			buffer.patient = patientIterator.next();
			if (oldBuffer != null && buffer.patient.patientID.equals(oldBuffer.patient.patientID)){ //Same patient (probably different start and end date: keep events and prescriptions
				buffer.events = oldBuffer.events;
				buffer.prescriptions = oldBuffer.prescriptions;
				return;
			}
			
			String patientID = buffer.patient.patientID;
			//Skip to prescription belonging to this patient:
      while (prescription != null && prescription.patientID.compareTo(patientID)<0)
        if  (prescriptionIterator.hasNext())
          prescription = prescriptionIterator.next();  
        else
        	prescription = null;
      
      //Iterate through prescriptions until we hit the next patient:
      while (prescription != null && prescription.patientID.equals(patientID)){
      	buffer.prescriptions.add(prescription);
        if (prescriptionIterator.hasNext())
          prescription = prescriptionIterator.next();
        else  
          break; //out of prescriptions 
      }

      //Skip to event belonging to this patient:
      while (event != null && event.patientID.compareTo(patientID)<0)
      	if  (eventIterator.hasNext())
      		event = eventIterator.next();  
      	else
      		event = null;
      
      //Iterate through events until we hit the next patient:
      while (event != null && event.patientID.equals(patientID)){
      	buffer.events.add(event);
        if (eventIterator.hasNext())
          event = eventIterator.next();
        else  
          break; //out of events 
      }
		} else
			buffer = null;
	}

	@Override
	public void remove() {
		System.err.println("Calling unimplemented remove method in class " + this.getClass().getName());
	}
}
