package org.erasmusmc.jerboa.dataClasses;

import java.util.ArrayList;
import java.util.List;

public class PatientPrescriptionEvent {
	public Patient patient;
	public List<Prescription> prescriptions = new ArrayList<Prescription>();
	public List<Event> events = new ArrayList<Event>();
}
