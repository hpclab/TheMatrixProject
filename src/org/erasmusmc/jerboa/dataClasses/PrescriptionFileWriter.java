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
