package org.erasmusmc.jerboa.modules.matchingCriteria;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;

public class CriteriaUtilities {
	
  public static List<Prescription> getDrugsInWindow(List<Prescription> prescriptions, long start, long end){
  	List<Prescription> result = new ArrayList<Prescription>();
  	for (Prescription prescription : prescriptions)
  		if (prescription.getEnd() > start && prescription.start < end)
  			result.add(prescription);  			
  	return result;
  }
  
	public static Set<ATCCode> getAllATCCodesFromWindow(long eventDate, PatientPrescriptionEvent patientPrescriptionEvent, int startWindow, int endWindow) {
		Set<ATCCode> atcCodes = new HashSet<ATCCode>();
		for (Prescription prescription : CriteriaUtilities.getDrugsInWindow(patientPrescriptionEvent.prescriptions, eventDate + startWindow, eventDate + endWindow))
			atcCodes.addAll(prescription.atcCodes);
		return atcCodes;
	}
}
