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
 * Combines concomittant drug use into single episodes. After applying this method, there will be no 
 * overlapping episodes in the prescriptions table.
 * @author schuemie
 *
 */
public class CombineConcomitantModule extends JerboaModule{
  
	public JerboaModule prescriptions;
	
  private static final long serialVersionUID = -4422411384238432005L;
  private static int countNew;
   
  protected void runModule(String outputFilename){
    FileSorter.sort(prescriptions.getResultFilename(), new String[]{"PatientID","Date"});
    process(prescriptions.getResultFilename(), outputFilename);
  }
  
  public static void process(String source, String target) {
    int countOriginal = 0;
    countNew = 0;
    PrescriptionFileReader in = new PrescriptionFileReader(source);
    PrescriptionFileWriter out = new PrescriptionFileWriter(target);
    long oldTime = Long.MIN_VALUE;
    String patientID = "";
    List<Prescription> runningPrescriptions = new ArrayList<Prescription>();
    for (Prescription prescription : in){
      ProgressHandler.reportProgress();
      countOriginal++;
      if (!prescription.patientID.equals(patientID)){
        if (patientID.compareTo(prescription.patientID) > 0)
          System.err.println("MergeRepeatPrescriptions: Input file not sorted by patientID! ");
        patientID = prescription.patientID;
        oldTime = Long.MIN_VALUE;
        OutputFinishedPrescriptions(null, runningPrescriptions, out);
        if (runningPrescriptions.size() != 0)
          System.err.println("Error!");
      }
      
      if (oldTime > prescription.start)
        System.err.println("MedicationReformat: Input file not sorted by date! ");
      oldTime = prescription.start;
      
      CompareToRunningPrescriptions(prescription, runningPrescriptions);
      OutputFinishedPrescriptions(prescription, runningPrescriptions, out);
    }
    OutputFinishedPrescriptions(null, runningPrescriptions, out);
    out.close();
    
    System.out.println("Original size: " + countOriginal + " prescriptions, after recoding: " + countNew + " prescriptions");
  }
  
  private static void OutputFinishedPrescriptions(Prescription newPrescription, List<Prescription> runningPrescriptions, PrescriptionFileWriter outfile) {
    Iterator<Prescription> recipeIterator = runningPrescriptions.iterator();
    while (recipeIterator.hasNext()){
      Prescription runningPrescription = recipeIterator.next();
      if (newPrescription == null || runningPrescription.getEnd() < newPrescription.start){ //Dump it to file
        outfile.write(runningPrescription);
        countNew++;
        recipeIterator.remove();
      }
    }
  }

  private static void CompareToRunningPrescriptions(Prescription newPrescription, List<Prescription> runningPrescriptions) {
    List<Prescription> pieces = new ArrayList<Prescription>(1);
    pieces.add(newPrescription);
    for (Prescription runningPrescription : new ArrayList<Prescription>(runningPrescriptions)){
      for (Prescription partialPrescription : new ArrayList<Prescription>(pieces)){
        if (partialPrescription.start < runningPrescription.getEnd() && partialPrescription.getEnd() > runningPrescription.start){ //there is some overlap
          long startOld = runningPrescription.start;
          long startNew = partialPrescription.start;
          long endOld = runningPrescription.getEnd();
          long endNew = partialPrescription.getEnd();
          if (startNew < startOld){
            Prescription extraPrescription = new Prescription(partialPrescription);
            extraPrescription.duration = startOld - startNew;
            pieces.add(extraPrescription);
          } 
          if (startNew > startOld){
            Prescription extraPrescription = new Prescription(runningPrescription);
            extraPrescription.duration = startNew - startOld;
            runningPrescriptions.add(extraPrescription);
          }
          if (endNew > endOld){
            Prescription extraPrescription = new Prescription(partialPrescription);
            extraPrescription.duration = endNew - endOld;
            extraPrescription.start = endOld;
            pieces.add(extraPrescription);
          } 
          if (endNew < endOld){
            Prescription extraPrescription = new Prescription(runningPrescription);
            extraPrescription.duration = endOld - endNew;
            extraPrescription.start = endNew;
            runningPrescriptions.add(extraPrescription);
          }
          runningPrescription.start = Math.max(startNew, startOld);
          runningPrescription.duration = Math.min(endNew, endOld) - runningPrescription.start;
          runningPrescription.atcCodes.addAll(partialPrescription.atcCodes);            
          pieces.remove(partialPrescription);
        }        
      }
    }

    for (Prescription partialPrescription : pieces){
      if (partialPrescription.duration != 0){
        runningPrescriptions.add(partialPrescription);     
      }
    }   
  }
}
