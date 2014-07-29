package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileWriter;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.utilities.FileSorter;

/**
 * Processes a prescriptions file to create exposure episodes. Possible transformations include merging repeat prescriptions, closing gaps between 
 * prescriptions, and adding wash-out time after prescriptions.
 * @author schuemie
 *
 */
public class ExposureDefinitionModule extends JerboaModule  {

	public JerboaModule prescriptions;

	/**
	 * If a new prescription occurs within this fraction of the original prescription duration after the 
	 * prescription ended, the new and old prescription are assumed to be repeats, and are merged.<BR>
	 * default = 0
	 */
	public double maxGapFraction = 0;

	/**
	 * If a new prescription occurs within this number of days after the 
	 * prescription ended, the new and old prescription are assumed to be repeats, and are merged.<BR>
	 * default = 0 
	 */
	public int maxGapDays = 0;

	/**
	 * Prolong each duration with this number of days.<BR>
	 * default = 0 
	 */
	public int addToDurationDays = 0;

	/**
	 * Prolong each duration with this fraction of the duration.<BR>
	 * default = 0 
	 */
	public double addToDurationFraction = 0;

	/**
	 * When true, patients are assumed to stockpile, and two overlapping prescriptions of the same drug are assumed to be a single episode with a length
	 * equal to the sum of the separate prescriptions.<BR>
	 * default = false
	 */
	public boolean assumeStockpiling = false;

	/**
	 * Exclude the first day of a prescription.<BR>
	 * default = true
	 */
	public boolean excludeStartDate = true;

	/**
	 * Only include exposure when no other exposure is taking place.<BR>
	 * default = false
	 */
	public boolean monoTherapyOnly = false;

	/**
	 * Only include the first prescription of an ATC code
	 */
	public boolean firstPrescriptionOnly = false;

	private static final long serialVersionUID = -4422411384718432005L;
	private int countNew;
	private float sumDurationNew;
	private int countOriginal;
	private float sumDurationOriginal;


	protected void runModule(String outputFilename){
		FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID","Date"});
		process(prescriptions.getResultFilename(), outputFilename);
	}

	public void process(String source, String target) {
		PrescriptionFileReader in = new PrescriptionFileReader(source);
		PrescriptionFileWriter out = new PrescriptionFileWriter(target);
		countOriginal = 0;
		sumDurationOriginal = 0;
		countNew = 0;
		sumDurationNew = 0;
		String patientID = "";
		List<Prescription> patientPrescriptions = new ArrayList<Prescription>();
		for (Prescription prescription : in){
			if (!prescription.patientID.equals(patientID)){
				if (patientID.compareTo(prescription.patientID) > 0)
					System.err.println("MergeRepeatPrescriptions: Input file not sorted by patientID! ");
				processPatient(patientPrescriptions, out);
				patientPrescriptions.clear();
				patientID = prescription.patientID;
			}
			patientPrescriptions.add(prescription);
			countOriginal++;
			sumDurationOriginal += prescription.duration;
		}
		processPatient(patientPrescriptions, out);
		out.close();
		System.out.println("Original size: " + countOriginal + " prescriptions, after merging: " + countNew + " prescriptions");
		System.out.println("Original average duration: " + sumDurationOriginal/countOriginal + " days , after merging: " + sumDurationNew/countNew + " days");
	}


	private void processPatient(List<Prescription> prescriptions, PrescriptionFileWriter out){
		ProgressHandler.reportProgress();
		List<Prescription> outputPrescriptions = new ArrayList<Prescription>();
		if (firstPrescriptionOnly){
			Set<ATCCode> seenATCCodes = new HashSet<ATCCode>();
			for (Prescription prescription : prescriptions){
				if (seenATCCodes.add(prescription.atcCodes.iterator().next())){
					prescription.duration += Math.round(addToDurationFraction * prescription.duration) + addToDurationDays;
					outputPrescriptions.add(prescription);
				}
			}
		} else {
			List<CustomPrescription> runningPrescriptions = new ArrayList<CustomPrescription>();

			for (Prescription prescription : prescriptions){   
				CustomPrescription customPrescription = new CustomPrescription(prescription);
				customPrescription.scanDate = customPrescription.getOriginalEnd() + Math.round((maxGapFraction * prescription.duration) + maxGapDays);
				customPrescription.duration += Math.round(addToDurationFraction * prescription.duration) + addToDurationDays; 
				mergeAndOutputPrescriptions(customPrescription, runningPrescriptions, outputPrescriptions);   
				runningPrescriptions.add(customPrescription);
			}
			mergeAndOutputPrescriptions(null, runningPrescriptions, outputPrescriptions);
		}
		if (monoTherapyOnly)
			outputPrescriptions = reduceToMonoTherapy(outputPrescriptions);

		//Output prescriptions:
		for (Prescription prescription : outputPrescriptions){
			if (excludeStartDate){
				prescription.start++;
				prescription.duration--;
			}
			if (prescription.duration >= 0){
				out.write(prescription);
				countNew++;
				sumDurationNew += prescription.duration;
			}
		}
	}

	private List<Prescription> reduceToMonoTherapy(List<Prescription> prescriptions) {
		Collections.sort(prescriptions, new Comparator<Prescription>() {

			@Override
			public int compare(Prescription o1, Prescription o2) {
				if (o1.start < o2.start)
					return -1;
				else if (o1.start > o2.start)
					return 1;
				else 
					return 0;				
			}
		});
		List<Prescription> outputPrescriptions = new ArrayList<Prescription>();
		List<Prescription> runningPrescriptions = new ArrayList<Prescription>();
		for (Prescription prescription : prescriptions){
			checkForMonoTherapy(runningPrescriptions, outputPrescriptions, prescription.start);

			// cleanup:
			Iterator<Prescription> iterator = runningPrescriptions.iterator();
			while (iterator.hasNext())
				if (iterator.next().getEnd() < prescription.start)
					iterator.remove();

			runningPrescriptions.add(prescription);
		}
		checkForMonoTherapy(runningPrescriptions, outputPrescriptions, Long.MAX_VALUE);
		return outputPrescriptions;
	}

	private void checkForMonoTherapy(List<Prescription> runningPrescriptions, List<Prescription> outputPrescriptions, long date){
		long lastEnd = Long.MIN_VALUE;

		Prescription monoTherapy = null;
		for (Prescription runningPrescription : runningPrescriptions)
			if (runningPrescription.getEnd() > lastEnd){
				lastEnd = runningPrescription.getEnd();			
				monoTherapy = runningPrescription;
			}

		long secondLastEnd = Long.MIN_VALUE;
		for (Prescription runningPrescription : runningPrescriptions)
			if (runningPrescription != monoTherapy && runningPrescription.getEnd() > secondLastEnd)
				secondLastEnd = runningPrescription.getEnd();			

		if (lastEnd != secondLastEnd){ //It was monotherapy for a while
			Prescription runningPrescription = new Prescription(monoTherapy);
			long runningEnd = runningPrescription.getEnd();
			if (secondLastEnd > runningPrescription.start){ //First part of the prescription was not mono
				runningPrescription.start = secondLastEnd;
				runningPrescription.duration = runningEnd - runningPrescription.start;
			}

			if (runningEnd > date) //Last part of the prescripition was not mono
				runningPrescription.duration = date - runningPrescription.start;


			if (runningPrescription.duration > 0) //Some or all of the running prescription was mono: save
				outputPrescriptions.add(runningPrescription);
		}
	}

	private void mergeAndOutputPrescriptions(CustomPrescription newPrescription, List<CustomPrescription> runningPrescriptions, List<Prescription> outputPrescriptions) {
		Iterator<CustomPrescription> prescriptionIterator = runningPrescriptions.iterator();
		while (prescriptionIterator.hasNext()){
			CustomPrescription prescription = prescriptionIterator.next();
			if (newPrescription == null || prescription.scanDate < newPrescription.start){
				outputPrescriptions.add(prescription);
				prescriptionIterator.remove();
			} else
				if (prescription.atcCodes.equals(newPrescription.atcCodes)) 
					if (prescription.start <= newPrescription.scanDate && prescription.scanDate >= newPrescription.start){ //some overlap
						long tempEnd = newPrescription.getEnd();
						long tempOriginalEnd = newPrescription.getOriginalEnd();
						long tempScan = newPrescription.scanDate;
						newPrescription.start = Math.min(prescription.start, newPrescription.start);
						if (assumeStockpiling && (prescription.getOriginalEnd() > newPrescription.start)){
							newPrescription.duration = prescription.originalDuration + newPrescription.duration;
							newPrescription.originalDuration = prescription.originalDuration + newPrescription.originalDuration;
							newPrescription.scanDate = newPrescription.getOriginalEnd() + Math.round((maxGapFraction * prescription.duration) + maxGapDays);
						} else {
							newPrescription.duration = Math.max(tempEnd, prescription.getEnd()) - newPrescription.start;
							newPrescription.originalDuration = Math.max(tempOriginalEnd, prescription.getOriginalEnd()) - newPrescription.start;
							newPrescription.scanDate = Math.max(tempScan, prescription.scanDate);
						}
						newPrescription.repeats+= prescription.repeats;
						prescriptionIterator.remove();
					}
		}
	}

	private static class CustomPrescription extends Prescription {
		long scanDate;
		long originalDuration;
		int repeats = 1;
		public long getOriginalEnd(){
			return start + originalDuration;
		}
		public CustomPrescription(Prescription prescription){
			super(prescription);
			originalDuration = prescription.duration;
		}
	}
}