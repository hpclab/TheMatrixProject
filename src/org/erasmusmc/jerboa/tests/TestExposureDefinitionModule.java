package org.erasmusmc.jerboa.tests;

import java.util.Iterator;
import java.util.zip.DataFormatException;

import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.jerboa.modules.ExposureDefinitionModule;
import org.erasmusmc.utilities.StringUtilities;

import junit.framework.TestCase;

public class TestExposureDefinitionModule extends TestCase {
	public void testRepeatPrescriptions() throws DataFormatException{
		String prescriptionsFile = UtilitiesForTests.generatePrescriptionsFile();
		String targetFile = UtilitiesForTests.tempFolder + "Exposure.txt";
		ExposureDefinitionModule module = new ExposureDefinitionModule();
		module.excludeStartDate = true;
		module.assumeStockpiling = false;
		module.addToDurationDays = 0;
		module.addToDurationFraction = 0;
		module.maxGapDays = 0;
		module.maxGapFraction = 0;
		module.monoTherapyOnly = false;
		module.process(prescriptionsFile, targetFile);
		Iterator<Prescription> iterator = new PrescriptionFileReader(targetFile).iterator();
		Prescription prescription;
		prescription = iterator.next();
		assertEquals(prescription.duration,22);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20050302"));

		prescription = iterator.next();
		assertEquals(prescription.duration,13);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20060402"));

		prescription = iterator.next();
		assertEquals(prescription.duration,13);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20060502"));
	}
	
	public void testMaxGap() throws DataFormatException{
		String prescriptionsFile = UtilitiesForTests.generatePrescriptionsFile();
		String targetFile = UtilitiesForTests.tempFolder + "Exposure.txt";
		ExposureDefinitionModule module = new ExposureDefinitionModule();
		module.excludeStartDate = true;
		module.assumeStockpiling = false;
		module.addToDurationDays = 0;
		module.addToDurationFraction = 0;
		module.maxGapDays = 30;
		module.maxGapFraction = 0;
		module.monoTherapyOnly = false;
		module.process(prescriptionsFile, targetFile);
		Iterator<Prescription> iterator = new PrescriptionFileReader(targetFile).iterator();
		Prescription prescription;
		prescription = iterator.next();
		assertEquals(prescription.duration,22);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20050302"));

		prescription = iterator.next();
		assertEquals(prescription.duration,43);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20060402"));
	}
	
	public void testAddToDuration() throws DataFormatException{
		String prescriptionsFile = UtilitiesForTests.generatePrescriptionsFile();
		String targetFile = UtilitiesForTests.tempFolder + "Exposure.txt";
		ExposureDefinitionModule module = new ExposureDefinitionModule();
		module.excludeStartDate = true;
		module.assumeStockpiling = false;
		module.addToDurationDays = 0;
		module.addToDurationFraction = .20;
		module.maxGapDays = 0;
		module.maxGapFraction = 0;
		module.monoTherapyOnly = false;
		module.process(prescriptionsFile, targetFile);
		Iterator<Prescription> iterator = new PrescriptionFileReader(targetFile).iterator();
		Prescription prescription;
		prescription = iterator.next();
		assertEquals(prescription.duration,25);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20050302"));

		prescription = iterator.next();
		assertEquals(prescription.duration,16);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20060402"));

		prescription = iterator.next();
		assertEquals(prescription.duration,16);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20060502"));
	}
	
	public void testStockpiling() throws DataFormatException{
		String prescriptionsFile = UtilitiesForTests.generatePrescriptionsFile();
		String targetFile = UtilitiesForTests.tempFolder + "Exposure.txt";
		ExposureDefinitionModule module = new ExposureDefinitionModule();
		module.excludeStartDate = true;
		module.assumeStockpiling = true;
		module.addToDurationDays = 0;
		module.addToDurationFraction = 0;
		module.maxGapDays = 0;
		module.maxGapFraction = 0;
		module.monoTherapyOnly = false;
		module.process(prescriptionsFile, targetFile);
		Iterator<Prescription> iterator = new PrescriptionFileReader(targetFile).iterator();
		Prescription prescription;
		prescription = iterator.next();
		assertEquals(prescription.duration,27);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20050302"));

		prescription = iterator.next();
		assertEquals(prescription.duration,13);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20060402"));

		prescription = iterator.next();
		assertEquals(prescription.duration,13);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20060502"));
	}
	
	public void testMonoTherapy() throws DataFormatException{
		String prescriptionsFile = UtilitiesForTests.generatePrescriptionsFile();
		String targetFile = UtilitiesForTests.tempFolder + "Exposure.txt";
		ExposureDefinitionModule module = new ExposureDefinitionModule();
		module.excludeStartDate = true;
		module.assumeStockpiling = false;
		module.addToDurationDays = 0;
		module.addToDurationFraction = 0;
		module.maxGapDays = 0;
		module.maxGapFraction = 0;
		module.monoTherapyOnly = true;
		module.process(prescriptionsFile, targetFile);
		Iterator<Prescription> iterator = new PrescriptionFileReader(targetFile).iterator();
		Prescription prescription;
		prescription = iterator.next();
		assertEquals(prescription.duration,22);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20050302"));

		prescription = iterator.next();
		assertEquals(prescription.duration,13);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20060402"));

		prescription = iterator.next();
		assertEquals(prescription.duration,2);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20060502"));
		
		prescription = iterator.next();
		assertEquals(prescription.duration,2);
		assertEquals(prescription.start,StringUtilities.sortableTimeStringToDays("20060516"));
	}
	
}
