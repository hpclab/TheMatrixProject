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
