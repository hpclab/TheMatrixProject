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
