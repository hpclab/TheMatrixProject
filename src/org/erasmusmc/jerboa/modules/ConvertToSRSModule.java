package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEventIterator;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.utilities.FileSorter;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Converts longitudinal data into SRS-like data.
 * @author schuemie
 *
 */
public class ConvertToSRSModule extends JerboaModule{

	public JerboaModule prescriptions;
	public JerboaModule patients;
	public JerboaModule events;
	
	/**
	 * Specifies the way in which the longitudinal data is converted to SRS format. There are currently three options:
	 * <UL>
	 * <LI>PATIENT</LI>
	 * <LI>SRS</LI>
	 * <LI>MSRS</LI>
	 * </UL>
	 * default = PATIENT
	 */
	public String method = "PATIENT";
	
	public static String SRS = "SRS";
	public static String MSRS = "MSRS";
	public static String PATIENT = "PATIENT";
	
	private static final long serialVersionUID = 1561967305951500543L;
	private CountingSet<String> atcEventTypes;
	private CountingSet<String> atcs;
	private CountingSet<String> eventTypes;
	private int totalCount;

	public static void main(String[] args){
		String folder = "/data/OSIM/Sim10/Thread_1/";
		ConvertToSRSModule module = new ConvertToSRSModule();
		//FileSorter.sort(folder+"Events.txt", new String[]{"PatientID", "Date"});
		module.method = SRS;
		module.process(folder+"Mergerepeats.txt", folder+"Patients.txt",folder+"Keepfirstevents.txt",folder+"GPSPrep2.txt");
	}

	@Override
	protected void runModule(String outputFilename){
		FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID","Date"});
		FileSorter.sort(patients.getResultFilename(), "PatientID");
		FileSorter.sort(events.getResultFilename(), new String[]{"PatientID", "Date"});
		process(prescriptions.getResultFilename(), patients.getResultFilename(), events.getResultFilename(), outputFilename);
	}

	public void process(String sourcePrescriptions, String sourcePatients, String sourceEvents, String target) {
		readData(sourcePrescriptions, sourcePatients, sourceEvents);
		writeData(target);
	}

	private void readData(String sourcePrescriptions, String sourcePatients, String sourceEvents) {
		atcEventTypes = new CountingSet<String>();
		atcs = new CountingSet<String>();
		eventTypes = new CountingSet<String>();
		totalCount = 0;
		PatientFileReader patientReader = new PatientFileReader(sourcePatients);
		PrescriptionFileReader prescriptionReader = new PrescriptionFileReader(sourcePrescriptions);
		EventFileReader eventReader = new EventFileReader(sourceEvents);
		PatientPrescriptionEventIterator iterator = new PatientPrescriptionEventIterator(patientReader.iterator(), prescriptionReader.iterator(), eventReader.iterator());
		while (iterator.hasNext()){
		  ProgressHandler.reportProgress();
			processPatient(iterator.next());
		}
	}

	public void processPatient(PatientPrescriptionEvent patientPrescriptionEvent) {
		filterOnCohortTime(patientPrescriptionEvent);
		if (method.toUpperCase().equals(PATIENT)){
			Set<String> patAtcs = new HashSet<String>();
			Set<String> patEvents = new HashSet<String>();
			Set<String> patAtcEvents = new HashSet<String>();
			
			for (Event event : patientPrescriptionEvent.events)
				patEvents.add(event.eventType);

			for (Prescription prescription : patientPrescriptionEvent.prescriptions)
				patAtcs.add(prescription.getATCCodesAsString());

			for (Event event : patientPrescriptionEvent.events)
				for (Prescription prescription : patientPrescriptionEvent.prescriptions)
					if (event.date > prescription.start && event.date <= prescription.getEnd())
						patAtcEvents.add(prescription.getATCCodesAsString() + "\t" + event.eventType);

			for (String atc : patAtcs)
				atcs.add(atc);
			for (String eventType : patEvents)
				eventTypes.add(eventType);
			for (String atcEvent : patAtcEvents)
				atcEventTypes.add(atcEvent);
			totalCount++;	
			
		} else if (method.toUpperCase().equals(SRS)){			
			for (Event event : patientPrescriptionEvent.events)
				for (Prescription prescription : patientPrescriptionEvent.prescriptions)
					if (event.date > prescription.start && event.date <= prescription.getEnd()){
						atcEventTypes.add(prescription.getATCCodesAsString() + "\t" + event.eventType);
						atcs.add(prescription.getATCCodesAsString());
						eventTypes.add(event.eventType);
						totalCount++;
					}
		} else if (method.toUpperCase().equals(MSRS)){
			for (Prescription prescription : patientPrescriptionEvent.prescriptions){
				boolean hasEvent = false;
			  for (Event event : patientPrescriptionEvent.events)
					if (event.date > prescription.start && event.date <= prescription.getEnd()){
						atcEventTypes.add(prescription.getATCCodesAsString() + "\t" + event.eventType);
						atcs.add(prescription.getATCCodesAsString());
						eventTypes.add(event.eventType);
						totalCount++;
						hasEvent = true;
					}
			  if (!hasEvent){
			  	atcs.add(prescription.getATCCodesAsString());
			  	totalCount++;
			  }
			}			  
			for (Event event : patientPrescriptionEvent.events){
				boolean hasPrescription = false;
				for (Prescription prescription : patientPrescriptionEvent.prescriptions)
					if (event.date > prescription.start && event.date <= prescription.getEnd())
				    hasPrescription = true;
				if (!hasPrescription){
					eventTypes.add(event.eventType);
				  totalCount++;
				}
			}
		}
	}

	private void filterOnCohortTime(PatientPrescriptionEvent patientPrescriptionEvent) {
		Patient patient = patientPrescriptionEvent.patient;
		Iterator<Event> eventIterator = patientPrescriptionEvent.events.iterator();
		while (eventIterator.hasNext()){
		  Event event = eventIterator.next();
		  if (event.date < patient.startdate || event.date >= patient.startdate)
		  	eventIterator.remove();
		}

		Iterator<Prescription> prescriptionIterator = patientPrescriptionEvent.prescriptions.iterator();
		while (prescriptionIterator.hasNext()){
		  Prescription prescription = prescriptionIterator.next();
		  if (prescription.getEnd() < patient.startdate || prescription.start >= patient.startdate)
		  	prescriptionIterator.remove();
		  else {
		  	long end = prescription.getEnd();
		  	prescription.start = Math.max(prescription.start, patient.startdate);
		  	prescription.duration = Math.min(end, patient.enddate) - prescription.start;
		  	if (prescription.duration == 0)
		  		prescriptionIterator.remove();
		  }
		}
	}

	private void writeData(String target) {
		WriteCSVFile out = new WriteCSVFile(target);
		out.write(generateHeader());
		for (String atc : atcs)
			for (String eventType : eventTypes){
				List<String> cols = new ArrayList<String>(3);
				cols.add(atc);
				cols.add(eventType);
				int w00 = atcEventTypes.getCount(atc+"\t"+eventType);
				int w01 = atcs.getCount(atc) - w00;
				int w10 = eventTypes.getCount(eventType) - w00;
				int w11 = totalCount - w00 - w01 - w10;
				//double expected = (double)w10 * ((double)(w00+w01)/(double)(w10+w11)); //Expected based on non-exposed
				double expected = (double)(w00+w01)*(double)(w00+w10)/(double)(w00+w01+w10+w11);
				cols.add(Integer.toString(w00));
				cols.add(Double.toString(expected));
				cols.add(Integer.toString(w00));
				cols.add(Integer.toString(w01));
				cols.add(Integer.toString(w10));
				cols.add(Integer.toString(w11));
				out.write(cols);
			}
		out.close();
	}

	private List<String> generateHeader() {
		List<String> header = new ArrayList<String>();
		header.add("ATC");
		header.add("EventType");
		header.add("Events");
		header.add("expected");
		header.add("w00");
		header.add("w01");
		header.add("w10");
		header.add("w11");
		return header;
	}
}