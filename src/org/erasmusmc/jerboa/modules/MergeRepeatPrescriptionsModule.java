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
import java.util.Iterator;
import java.util.List;

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileWriter;
import org.erasmusmc.utilities.FileSorter;

/**
 * Merges repeat prescriptions with the same ATC code. A merged prescription will start at the start date
 * of the first prescription, and end at the end date of the last prescription.<BR>
 * Deprecated: from now on, please use the ExposureDefinitionModule.
 * @author schuemie
 *
 */
@Deprecated
public class MergeRepeatPrescriptionsModule extends JerboaModule{
  
	public JerboaModule prescriptions;
	
	/**
	 * If a new prescription occurs within this fraction of the original prescription duration after the 
	 * prescription ended, the new and old prescription are assumed to be repeats, and are merged.<BR>
	 * default = 0
	 */
  public double extentionMultiplier = 0;

	/**
	 * If a new prescription occurs within this number of days after the 
	 * prescription ended, the new and old prescription are assumed to be repeats, and are merged.<BR>
	 * default = 0 
	 */
  public int extentionInDays = 0;
  
  /**
   * Prolong each duration with this number of days.<BR>
   * default = 0 
   */
  public int addToDuration = 0;
  
  private static final long serialVersionUID = -4422411384718432005L;
  private static int countNew;
  private static float sumDurationNew;

  protected void runModule(String outputFilename){
    FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID","Date"});
    process(prescriptions.getResultFilename(), outputFilename);
  }
  
  public void process(String source, String target) {
    PrescriptionFileReader in = new PrescriptionFileReader(source);
    PrescriptionFileWriter out = new PrescriptionFileWriter(target);
    int countOriginal = 0;
    float sumDurationOriginal = 0;
    countNew = 0;
    long oldTime = Long.MIN_VALUE;
    String patientID = "";
    List<CustomPrescription> runningPrescriptions = new ArrayList<CustomPrescription>();
    for (Prescription nextPrescription : in){   
      ProgressHandler.reportProgress();
      countOriginal++;
      if (!nextPrescription.patientID.equals(patientID)){
        if (patientID.compareTo(nextPrescription.patientID) > 0)
          System.err.println("MergeRepeatPrescriptions: Input file not sorted by patientID! ");
        patientID = nextPrescription.patientID;
        oldTime = Long.MIN_VALUE;
        mergeAndOutputPrescriptions(null, runningPrescriptions, out); 
        if (runningPrescriptions.size() != 0)
          System.err.println("Error!");
      }
      
      CustomPrescription prescription = new CustomPrescription(nextPrescription);
      if (oldTime > prescription.start)
        System.err.println("MergeRepeatPrescriptions: Input file not sorted by date! ");
      oldTime = prescription.start;
      sumDurationOriginal += prescription.duration;
      prescription.scanDate = prescription.getEnd() + Math.round(extentionMultiplier * prescription.duration + extentionInDays);

      mergeAndOutputPrescriptions(prescription, runningPrescriptions, out);   
      runningPrescriptions.add(prescription);
    }
    mergeAndOutputPrescriptions(null, runningPrescriptions, out);  
    out.close();
    System.out.println("Original size: " + countOriginal + " prescriptions, after merging: " + countNew + " prescriptions");
    System.out.println("Original average duration: " + sumDurationOriginal/countOriginal + " days , after merging: " + sumDurationNew/countNew + " days");
  }
  
  private void mergeAndOutputPrescriptions(CustomPrescription newPrescription, List<CustomPrescription> runningPrescriptions, PrescriptionFileWriter outfile) {
    Iterator<CustomPrescription> prescriptionIterator = runningPrescriptions.iterator();
    while (prescriptionIterator.hasNext()){
      CustomPrescription prescription = prescriptionIterator.next();
      if (newPrescription == null || prescription.scanDate < newPrescription.start){ 
      	prescription.duration += addToDuration;
        outfile.write(prescription);
        countNew++;
        sumDurationNew += prescription.duration;
        prescriptionIterator.remove();
      } else
        if (prescription.atcCodes.equals(newPrescription.atcCodes)) 
          if (prescription.start <= newPrescription.scanDate && prescription.scanDate >= newPrescription.start){ //some overlap
            long tempEnd = newPrescription.getEnd();
            long tempScan = newPrescription.scanDate;
            newPrescription.start = Math.min(prescription.start, newPrescription.start);
            newPrescription.duration = Math.max(tempEnd, prescription.getEnd()) - newPrescription.start;
            newPrescription.scanDate = Math.max(tempScan, prescription.scanDate);
            newPrescription.repeats+= prescription.repeats;
            prescriptionIterator.remove();
          }
    }
  }
  
  private static class CustomPrescription extends Prescription {
    long scanDate;
    int repeats = 1;
    public CustomPrescription(Prescription prescription){
      super(prescription);
    }
  }

}
