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
