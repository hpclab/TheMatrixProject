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
package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileWriter;

public class ReduceATCLevelModule extends JerboaModule {

	public JerboaModule prescriptions;

	/**
	 * Reduce the ATC codes of the prescriptions to this number of digits.<BR>
	 * default = 4
	 */
	public int maxLevel = 4;

	/**
	 * The (partial) ATC codes that should not be reduced
	 */
	public List<String> excludedAtcCodes = new ArrayList<String>();

	private static final long serialVersionUID = -3863079175264278448L;

	protected void runModule(String outputFilename){
		process(prescriptions.getResultFilename(), outputFilename);
	}

	private void process(String source, String target) {
		List<String> excludedAtcSet = new ArrayList<String>(excludedAtcCodes.size());
		for (String type : excludedAtcCodes)
			excludedAtcSet.add(type.toLowerCase().trim());

		PrescriptionFileWriter out = new PrescriptionFileWriter(target);
		for (Prescription prescription : new PrescriptionFileReader(source)){
			if (!contains(excludedAtcSet,prescription.atcCodes.iterator().next().toString().toLowerCase()) )
				for (ATCCode atcCode : prescription.atcCodes)
					if (atcCode.atc.length() > maxLevel)
						atcCode.atc = atcCode.atc.substring(0,maxLevel);

			out.write(prescription);
		}
		out.close();
	}

	private boolean contains(List<String> atcs, String atc) {
		for (String allowedATC : atcs)
			if (atc.startsWith(allowedATC))
				return true;
		return false;
	}
}
