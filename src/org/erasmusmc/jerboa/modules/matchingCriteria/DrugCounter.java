package org.erasmusmc.jerboa.modules.matchingCriteria;

import java.util.Set;

import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;

public class DrugCounter implements MatchingCriterium {

	public int startWindow = -365;
	public int endWindow = -30;
	
	public String extractCriterium(long eventDate, PatientPrescriptionEvent patientPrescriptionEvent) {
		Set<ATCCode> atcCodes = CriteriaUtilities.getAllATCCodesFromWindow(eventDate, patientPrescriptionEvent,startWindow,endWindow);
		return Integer.toString(atcCodes.size());
	}
}
