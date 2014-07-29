package org.erasmusmc.jerboa.modules.matchingCriteria;

import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;

public interface MatchingCriterium {
	
  public String extractCriterium(long eventDate, PatientPrescriptionEvent patientPrescriptionEvent);
  
}
