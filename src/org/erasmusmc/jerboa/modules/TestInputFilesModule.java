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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Event;
import org.erasmusmc.jerboa.dataClasses.EventFileReader;
import org.erasmusmc.jerboa.dataClasses.InputFileException;
import org.erasmusmc.jerboa.dataClasses.InputFileExceptionViewer;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.utilities.StringUtilities;

/**
 * Module for testing whether the input files adhere to the defined format.
 * @author schuemie
 *
 */
public class TestInputFilesModule extends JerboaModule {
	
	public JerboaModule prescriptions;
	public JerboaModule patients;
	public JerboaModule events;
	public JerboaModule exposure;
	
  private static final long serialVersionUID = 6926189745238596979L;
  private FileTestPanel prescriptionsPanel;
  private FileTestPanel patientsPanel;
  private FileTestPanel eventsPanel;
  //private FileTestPanel exposurePanel;
 
  
  protected JPanel createParameterPanel(){
    JPanel panel = super.createParameterPanel();
    panel.setAlignmentX(LEFT_ALIGNMENT);
    JPanel gridPanel = new JPanel();
    gridPanel.setLayout(new GridBagLayout());
    gridPanel.setBackground(Color.WHITE);
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 0.33;
    c.fill = GridBagConstraints.HORIZONTAL;
    
    prescriptionsPanel = new FileTestPanel("Prescriptions file");
    c.gridx = 0;
    gridPanel.add(prescriptionsPanel, c);
    
    patientsPanel = new FileTestPanel("Patients file");
    c.gridx = 1;
    gridPanel.add(patientsPanel, c);
    
    eventsPanel = new FileTestPanel("Events file");
    c.gridx = 2;
    gridPanel.add(eventsPanel, c);
    /*
    exposurePanel = new FileTestPanel("Exposure file");
    c.gridx = 3;
    gridPanel.add(exposurePanel, c);
    */
    panel.add(gridPanel);
    return panel;
  }
  
  protected void runModule(String outputFilename){
    process(prescriptions==null?null:prescriptions.getResultFilename(), patients==null?null:patients.getResultFilename(), events==null?null:events.getResultFilename(), exposure==null?null:exposure.getResultFilename());
  }
  
  private void testIfExists(String filename){
    if (filename == null)
        throw new InputFileException(0, filename,"File not found");
  }

  private void process(String sourcePrescriptions, String sourcePatients, String sourceEvents, String sourceExposure) {
    if (prescriptionsPanel == null || prescriptionsPanel.isChecked())
      testPresciptionsFile(sourcePrescriptions);
    if (patientsPanel == null || patientsPanel.isChecked())
      testPatientsFile(sourcePatients);
    if (eventsPanel == null || eventsPanel.isChecked())
      testEventsFile(sourceEvents);
    //if (exposurePanel == null || exposurePanel.isChecked())
    //  testExposureFile(sourceExposure);
  }
  
  private void testEventsFile(String sourceEvents) {
    System.out.println("Testing events file");
    try{
    	testIfExists(sourceEvents);
      CountingSet<String> eventTypes = new CountingSet<String>();
      long firstEventDate = Long.MAX_VALUE;
      long lastEventDate = Long.MIN_VALUE;
      int count = 0;
      for (Event event : new EventFileReader(sourceEvents)){
        ProgressHandler.reportProgress();
        eventTypes.add(event.eventType);
        if (event.date < firstEventDate)
          firstEventDate = event.date;
        if (event.date > lastEventDate)
          lastEventDate = event.date;
        count++;
      }
      if (eventsPanel != null)
        eventsPanel.setOK();
      
      System.out.println("Events file statistics:");
      System.out.println("- number of events: " + count);
      for (String eventType : eventTypes)
        System.out.println("- events of type "+eventType+": " + eventTypes.getCount(eventType));
      System.out.println("- first event date: " + StringUtilities.daysToSortableDateString(firstEventDate));
      System.out.println("- last event date: " + StringUtilities.daysToSortableDateString(lastEventDate));
    } catch (Exception e){
      if (eventsPanel != null)
        eventsPanel.setError(e);
      
      System.err.println(e.getLocalizedMessage());
    }
  }
  
  private void testPatientsFile(String sourcePatients) {
    System.out.println("Testing patients file");
    try{
    	testIfExists(sourcePatients);
      long firstStartDate = Long.MAX_VALUE;
      long lastEndDate = Long.MIN_VALUE;
      int count = 0;
      long sumPatientTime = 0;
      int maleCount = 0;
      for (Patient patient : new PatientFileReader(sourcePatients)){
        ProgressHandler.reportProgress();
        if (patient.startdate < firstStartDate)
          firstStartDate = patient.startdate;
        if (patient.enddate > lastEndDate)
          lastEndDate = patient.enddate;
        count++;
        sumPatientTime += patient.enddate - patient.startdate;
        if (patient.gender == Patient.MALE)
          maleCount++;
      }
      if (patientsPanel != null)
        patientsPanel.setOK();
      System.out.println("Patients file statistics:");
      System.out.println("- number of patients: " + count);
      System.out.println("- number of female patients: " + (count-maleCount) + " (" + (100*(count-maleCount)/count) + "%)" );
      System.out.println("- number of male patients: " + maleCount + " (" + (100*maleCount/count) + "%)");
      System.out.println("- first start date: " + StringUtilities.daysToSortableDateString(firstStartDate));
      System.out.println("- last end date: " + StringUtilities.daysToSortableDateString(lastEndDate));
      System.out.println("- average time per patient: " + (sumPatientTime / count) + " days");
    } catch (Exception e){
      if (patientsPanel != null)
        patientsPanel.setError(e);
      System.err.println(e.getLocalizedMessage());
    }
  }
  
  private void testPresciptionsFile(String sourcePrescriptions) {
    System.out.println("Testing prescriptions file");
    try{
    	testIfExists(sourcePrescriptions);
      long firstStartDate = Long.MAX_VALUE;
      long lastStartDate = Long.MIN_VALUE;
      int count = 0;
      int countZeroDuration = 0;
      long sumDuration = 0;
      Set<String> atcCodes = new HashSet<String>(); 
      for (Prescription prescription : new PrescriptionFileReader(sourcePrescriptions)){
        ProgressHandler.reportProgress();
        if (prescription.start < firstStartDate)
          firstStartDate = prescription.start;
        if (prescription.start > lastStartDate)
          lastStartDate = prescription.start;
        count++;
        if (prescription.duration == 0)
          countZeroDuration++;
        sumDuration+= prescription.duration;
        atcCodes.add(prescription.getATCCodesAsString());
      }
      if (prescriptionsPanel != null)
        prescriptionsPanel.setOK();
      System.out.println("Prescription file statistics:");
      System.out.println("- number of prescriptions: " + count);
      System.out.println("- number of prescriptions with zero duration: " + countZeroDuration);
      System.out.println("- first start date: " + StringUtilities.daysToSortableDateString(firstStartDate));
      System.out.println("- last start date: " + StringUtilities.daysToSortableDateString(lastStartDate));
      System.out.println("- average prescription duration: " + ((count == 0)?"0":(sumDuration / count)) + " days");
      System.out.println("- unique ATC codes: " + atcCodes.size());
      
    } catch (Exception e){
      if (prescriptionsPanel != null)
        prescriptionsPanel.setError(e);
      System.err.println(e.getLocalizedMessage());
    }
  }
  /*
  
  private void testExposureFile(String sourceExposure) {
    System.out.println("Testing exposure file");
    try{
    	testIfExists(sourceExposure);
      long firstStartDate = Long.MAX_VALUE;
      long lastStartDate = Long.MIN_VALUE;
      int count = 0; 
      for (Exposure exposure : new ExposureFileReader(sourceExposure)){
        ProgressHandler.reportProgress();
        if (exposure.start < firstStartDate)
          firstStartDate = exposure.start;
        if (exposure.start > lastStartDate)
          lastStartDate = exposure.start;
        count++;
      }
      if (exposurePanel != null)
        exposurePanel.setOK();
      System.out.println("Exposures file statistics:");
      System.out.println("- number of exposures: " + count);
      System.out.println("- first start date: " + StringUtilities.daysToSortableDateString(firstStartDate));
      System.out.println("- last start date: " + StringUtilities.daysToSortableDateString(lastStartDate));
      
    } catch (Exception e){
      if (exposurePanel != null)
        exposurePanel.setError(e);
      System.err.println(e.getLocalizedMessage());
    }
  }
  */
  private class FileTestPanel extends JPanel {
    private static final long serialVersionUID = 674606331289094896L;
    private JCheckBox checkBox;
    private JLabel label;
    private JButton button;
    private InputFileException inputFileException;
    
    public FileTestPanel(String title){
      super();
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      setBorder(BorderFactory.createTitledBorder(title));
      setBackground(Color.WHITE);
      
      checkBox = new JCheckBox("Test this file");
      checkBox.setBackground(Color.WHITE);
      checkBox.setSelected(true);
      add(checkBox);
      
      add(Box.createVerticalStrut(5));
      
      label = new JLabel();
      label.setBackground(Color.WHITE);
      add(label);
      
      add(Box.createVerticalStrut(5));
      
      button = new JButton("Show");
      button.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          handleButtonClick();
        }
      });
      add(button);
      
      setUntested();
    }
    
    public boolean isChecked(){
      return checkBox.isSelected();
    }
    
    public void setUntested(){
      label.setForeground(Color.BLACK);
      label.setText("Result: untested");
      button.setEnabled(false);
    }
    
    public void setOK(){
      label.setText("OK");
      label.setForeground(Color.GREEN);
      button.setEnabled(false);
    }
    
    public void setError(Exception exception){
    	if (exception instanceof InputFileException){
    		InputFileException inputFileException = (InputFileException)exception;
    	  if (inputFileException.getFilename() == null){
          label.setText("FILE NOT FOUND");
          label.setForeground(Color.RED);
          button.setEnabled(false);
    	  } else {
          label.setText("ERROR");
          label.setForeground(Color.RED);
          button.setEnabled(true);
          this.inputFileException = inputFileException;
    	  }
    	} else {
        label.setText("ERROR");
        button.setEnabled(false);
    	}
    }  
    
    private void handleButtonClick(){
      InputFileExceptionViewer viewer = new InputFileExceptionViewer(inputFileException);
      viewer.setVisible(true);
    }
  }
}

