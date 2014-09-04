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
