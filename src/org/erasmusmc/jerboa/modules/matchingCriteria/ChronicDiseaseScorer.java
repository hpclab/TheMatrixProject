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

import java.util.Set;

import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;

public class ChronicDiseaseScorer implements MatchingCriterium {

	public int startWindow = -365;
	public int endWindow = -30;

	public String extractCriterium(long eventDate, PatientPrescriptionEvent patientPrescriptionEvent) {
		Set<ATCCode> atcCodes = CriteriaUtilities.getAllATCCodesFromWindow(eventDate, patientPrescriptionEvent,startWindow,endWindow);
		return severityScore(atcCodes);
	}

	private String severityScore(Set<ATCCode> atcCodes) {
		int score = 0;
		//Heart disease:
		int classes = 0;
		if (hasCodeStartingWith(atcCodes, "B01") || hasCodeStartingWith(atcCodes, "B02"))
			classes ++;
		if (hasCodeStartingWith(atcCodes, "C01") || hasCodeStartingWith(atcCodes, "C09"))
			classes ++;
		if (hasCodeStartingWith(atcCodes, "C03C"))
			classes ++;      
		if (classes > 0)
			score += 2 + classes;

		//Respiratory illness:
		classes = 0;
		if (hasCodeStartingWith(atcCodes, "R03A"))
			classes ++;
		if (hasCodeStartingWith(atcCodes, "R03B"))
			classes ++;
		if (hasCodeStartingWith(atcCodes, "R03C"))
			classes ++;     
		if (hasCodeStartingWith(atcCodes, "R03D"))
			classes ++;     
		if (hasCodeStartingWith(atcCodes, "R01AD"))
			classes ++;     
		if (classes == 1)
			score += 2;
		if (classes > 1)
			score += 3;

		//Asthma, rheumatism
		if (hasCodeStartingWith(atcCodes, "H02AB"))
			score += 3;

		//Rheumatoid arthritis
		if (hasCodeStartingWith(atcCodes, "M01C"))
			score += 3;     

		//Cancer
		if (hasCodeStartingWith(atcCodes, "L01"))
			score += 3;  

		//Parkinson�s disease
		if (hasCodeStartingWith(atcCodes, "N04"))
			score += 3;  

		//Hypertension
		if (hasCodeStartingWith(atcCodes, "C02") || hasCodeStartingWith(atcCodes, "C08"))
			score += 2;
		else if (hasCodeStartingWith(atcCodes, "C07") || hasCodeStartingWith(atcCodes, "C03"))
			score += 1;

		//Diabetes
		if (hasCodeStartingWith(atcCodes, "A10"))
			score += 2;

		//Epilepsy
		if (hasCodeStartingWith(atcCodes, "N03A"))
			score += 2;

		//Acne
		if (hasCodeStartingWith(atcCodes, "D10A")) //Definition: at least two prescriptions. But info no longer available at this level
			score += 1;

		//Ulcers
		if (hasCodeStartingWith(atcCodes, "A02B"))
			score += 1;

		//Glaucoma
		if (hasCodeStartingWith(atcCodes, "S01E"))
			score += 1;  

		//Gout, hyperuricemia
		if (hasCodeStartingWith(atcCodes, "M04A"))
			score += 1;  

		//High cholesterol
		if (hasCodeStartingWith(atcCodes, "C10"))
			score += 1;  

		//Migraines
		if (hasCodeStartingWith(atcCodes, "N02CA"))
			score += 1;  

		//Tuberculosis
		if (hasCodeStartingWith(atcCodes, "J04A"))
			score += 1;  

		return Integer.toString(score);
		/*
			//Bin scores:
			if (score == 0)
				return "0";
			if (score < 4)
				return "1-3";
			if (score < 8)
				return "4-8";
			return "9-";
		 */
	}

	private boolean hasCodeStartingWith(Set<ATCCode> atcCodes, String prefix){
		for (ATCCode atc : atcCodes){
			if (atc.atc.startsWith(prefix))
				return true;
		}
		return false;
	}


}
