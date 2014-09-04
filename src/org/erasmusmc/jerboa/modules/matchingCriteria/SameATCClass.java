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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.erasmusmc.jerboa.dataClasses.PatientPrescriptionEvent;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.utilities.StringUtilities;

public class SameATCClass implements MatchingCriterium {

	public int startWindow = -1;
	public int endWindow = 0;
	public int atcLevel = 4;
	
	public String extractCriterium(long eventDate, PatientPrescriptionEvent patientPrescriptionEvent) {
		Set<ATCCode> atcCodes = CriteriaUtilities.getAllATCCodesFromWindow(eventDate, patientPrescriptionEvent,startWindow,endWindow);
		Set<String> classes = new HashSet<String>();
		for (ATCCode atcCode : atcCodes)
			if (atcCode.atc.length() >= atcLevel)
				classes.add(atcCode.atc.substring(0,atcLevel));
		if (classes.size() == 0)
			return "";
		else if (classes.size() == 1)
			return classes.iterator().next();
		else {
		  List<String> sortedClasses = new ArrayList<String>(classes);
		  Collections.sort(sortedClasses);
		  return StringUtilities.join(sortedClasses, "+");
		}
	}
}
