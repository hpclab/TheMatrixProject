package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.jerboa.dataClasses.ExtendedPrescription;
import org.erasmusmc.jerboa.dataClasses.ExtendedPrescriptionFileReader;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileWriter;
import org.erasmusmc.utilities.FileSorter;

/**
 * Calculates the duration of presciptions using various methods
 * @author schuemie
 *
 */
public class DurationCalculationModule extends JerboaModule {


	public JerboaModule prescriptions;
	
	/**
	 * Specifiy the type of calculation used:<BR>
	 * 0 : keep original duration from the duration field<BR>
	 * 1 : duration = TotalUnits/UnitsPerDay<BR>
	 * 2 : duration = DDDTotal <BR>
	 * 3 : duration = DDDTotal. If next prescription is closeby, assume duration = start of next - end<BR>
	 * default = 0
	 */
	public int durationCalculationMethod = 0;
	
	private int count;
	private int missingCount;
	private static final long serialVersionUID = -981878557000338666L;
	private static final int RIDICULOUS_DURATION = 9999;
	
	public static void main(String[] args){
		DurationCalculationModule module = new DurationCalculationModule();
		module.durationCalculationMethod = 3;
		module.process("/home/data/simulated/extendedprescriptions.txt", "/home/data/simulated/prescriptionsWithDurations.txt");
	}
	
	@Override
	protected void runModule(String outputFilename) {
		FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID","Date"});
		process(prescriptions.getResultFilename(), outputFilename);
	}

	public void process(String sourcePrescriptions, String targetPrescriptions) {
		count = 0;
		missingCount = 0;
		String oldPatientID = "";
		PrescriptionFileWriter out = new PrescriptionFileWriter(targetPrescriptions);
		List<ExtendedPrescription> prescriptions = new ArrayList<ExtendedPrescription>();
		for (ExtendedPrescription prescription : new ExtendedPrescriptionFileReader(sourcePrescriptions)){
			if (!prescription.patientID.equals(oldPatientID)){
				processPatient(prescriptions, out);
				prescriptions.clear();
				oldPatientID = prescription.patientID;
			}
			prescriptions.add(prescription);
		}
		processPatient(prescriptions, out);
		out.close();		
		System.out.println("Of the " + count + " prescriptions, " + missingCount + " did not have enough data to compute the duration, and were removed");
	}

	private void processPatient(List<ExtendedPrescription> prescriptions,	PrescriptionFileWriter out) {
	  for (int i = 0; i < prescriptions.size(); i++){
	  	ExtendedPrescription prescription = prescriptions.get(i);
	  	Prescription simplePrescription = new Prescription(prescription);
	  	if (durationCalculationMethod == 0){ //Method 0
	  		out.write(simplePrescription);
	  		count++;
	  	} else if (durationCalculationMethod == 1){ //Method 1
	  		if (prescription.totalUnits != -1 && prescription.unitsPerDay != -1 && prescription.unitsPerDay != 0){
	  			simplePrescription.duration = Math.round(prescription.totalUnits / prescription.unitsPerDay);
	  			if (simplePrescription.duration < RIDICULOUS_DURATION){
	  				out.write(simplePrescription);
	  				count++;
	  			}
	  		} else 			
	  		  missingCount++;
	  	} else if (durationCalculationMethod == 2){ //Method 2
	  		if (prescription.dddTotal != -1){
	  			simplePrescription.duration = Math.round(prescription.dddTotal);
	  			out.write(simplePrescription);
	  			count++;
	  		} else
	  			missingCount++;
	  	}  else if (durationCalculationMethod == 3){ //Method 3
	  		if (prescription.dddTotal != -1){
	  			ExtendedPrescription nextPrescription = findNext(prescription.atcCodes.iterator().next().atc, i+1, prescriptions);
	  			if (nextPrescription == null){
		  			simplePrescription.duration = Math.round(prescription.dddTotal);
		  			out.write(simplePrescription);	  				  				
	  			} else {
	  				if (nextPrescription.start != prescription.start){
	  				  double a = prescription.dddTotal / (double)(nextPrescription.start - prescription.start);
	  				  if (a > 0.7 && a < 1.5)
	  				  	simplePrescription.duration = nextPrescription.start - prescription.start;
	  				  else
	  		  			simplePrescription.duration = Math.round(prescription.dddTotal);
			  			out.write(simplePrescription);	  				  					  				    	
	  				} else
	  					missingCount++;
	  			}
	  		} else
	  			missingCount++;
	  	}
	  }
	}

	private ExtendedPrescription findNext(String atc, int start, List<ExtendedPrescription> prescriptions) {
		for (int i = start; i < prescriptions.size(); i++){
			if (prescriptions.get(i).atcCodes.iterator().next().atc.equals(atc))
				return prescriptions.get(i);
		}
		return null;
	}

}
