package org.erasmusmc.jerboa.modules.matchingCriteria;

import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;

public class ExternalCaseId implements MatchingCriterium {

	public static String CASESET_PREFIX = "CASESET";
	
	@Override
	public String extractCriterium(long eventDate, PatientPrescriptionEvent patientPrescriptionEvent) {
		for (Prescription presciption : patientPrescriptionEvent.prescriptions) 
			if (eventDate >= presciption.start && eventDate < presciption.getEnd())
				for (ATCCode atcCode : presciption.atcCodes)
					if (atcCode.atc.equals(CASESET_PREFIX))
						return atcCode.dose;
		System.out.println("Warning: No external caseset ID for patient: " +  patientPrescriptionEvent.patient.patientID);
		return null;
	}

}
