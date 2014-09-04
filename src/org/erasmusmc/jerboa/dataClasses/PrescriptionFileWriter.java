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
package org.erasmusmc.jerboa.dataClasses;

import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class PrescriptionFileWriter {
	private WriteCSVFile file;
	public PrescriptionFileWriter(String filename){
		file = new WriteCSVFile(filename);
		file.write(generateHeader());
	}

	private List<String> generateHeader() {
		List<String> headers = new ArrayList<String>();
		headers.add("PatientID");
		headers.add("ATC");
		headers.add("Date");
		headers.add("Duration");
		return headers;
	}

	public void write(Prescription prescription){
		List<String> columns = new ArrayList<String>();
		columns.add(prescription.patientID);
		columns.add(prescription.getATCCodesAsString());
		if (prescription.start == ConstantValues.UNKNOWN_DATE)
			columns.add("");
		else
			columns.add(StringUtilities.daysToSortableDateString(prescription.start));
		if (prescription.duration == 0)
			columns.add("");
		else
			columns.add(Long.toString(prescription.duration));
		file.write(columns);
	}

	public void flush(){
		file.flush();
	}

	public void close(){
		file.close();
	}
}
