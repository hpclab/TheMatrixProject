package org.erasmusmc.jerboa.tests;

import java.util.zip.DataFormatException;

import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileWriter;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileWriter;
import org.erasmusmc.utilities.StringUtilities;

public class UtilitiesForTests {
	public static String tempFolder = "/home/temp/";

	public static String generatePatientsFile() {
		String filename = tempFolder+"Patients.txt";
		PatientFileWriter out = new PatientFileWriter(filename);
		out.write(generatePatient("1",Patient.MALE,"19750805","20030601","20080201"));
		out.write(generatePatient("1",Patient.MALE,"19750805","20080301","20100101"));
		out.write(generatePatient("2",Patient.FEMALE,"19790902","20020615","20070304"));
		out.write(generatePatient("3",Patient.MALE,"20000506","20000607","20050701"));
		out.close();
		return filename;
	}
	
	public static String generatePrescriptionsFile() {
		String filename = tempFolder+"Prescriptions.txt";
		PrescriptionFileWriter out = new PrescriptionFileWriter(filename);
		out.write(generatePrescription("1","M01AB01","20050301",14));
		out.write(generatePrescription("1","M01AB01","20050310",14));
		out.write(generatePrescription("1","M01AB01","20060401",14));
		out.write(generatePrescription("1","M01AB01","20060501",14));
		out.write(generatePrescription("1","M01AB02","20060504",14));
		out.close();
		return filename;
	}

	public static Prescription generatePrescription(String patientID, String atc, String start, int duration) {
		Prescription prescription = new Prescription();
		prescription.patientID = patientID;
		prescription.atcCodes.add(new ATCCode(atc));
		try {
			prescription.start = StringUtilities.sortableTimeStringToDays(start);
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		prescription.duration = duration;
		return prescription;
	}
	
	public static Event generateEvent(String patientID, String eventType, String date) {
		Event event = new Event();
		event.patientID = patientID;
		event.eventType = eventType;
		try {
			event.date = StringUtilities.sortableTimeStringToDays(date);
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		return event;
	}

	private static Patient generatePatient(String patientID, byte gender,	String dob, String start, String end){
		Patient patient = new Patient();
		try {
			patient.patientID = patientID;
			patient.gender = gender;
			patient.birthdate = StringUtilities.sortableTimeStringToDays(dob);
			patient.startdate = StringUtilities.sortableTimeStringToDays(start);
			patient.enddate = StringUtilities.sortableTimeStringToDays(end);
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		return patient;
	}

}
