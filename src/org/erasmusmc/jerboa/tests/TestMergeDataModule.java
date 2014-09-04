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

import java.util.zip.DataFormatException;

import org.erasmusmc.jerboa.dataClasses.MergedData;
import org.erasmusmc.jerboa.modules.MergeDataModule;
import org.erasmusmc.utilities.StringUtilities;

import junit.framework.TestCase;

public class TestMergeDataModule extends TestCase {

	public void testMergeDataModule(){

		MergeDataModule module = new MergeDataModule();
		module.splitByMonth = true;
		assertEquals(6,module.chopDataByMonth(generateTestData1(), 0).get(0).duration);
		assertEquals(7,module.chopDataByMonth(generateTestData1(), 1).get(0).duration);
		module.splitByMonth = false;
		module.splitByYear = true;
		assertEquals(6,module.chopDataByYear(generateTestData1(), 0).get(0).duration);
		assertEquals(7,module.chopDataByYear(generateTestData1(), 1).get(0).duration);

		assertEquals(2000,module.chopDataByYear(generateTestData1(), 0).get(0).year);
		assertEquals(2001,module.chopDataByYear(generateTestData1(), 0).get(1).year);
		
		assertEquals(2000,module.chopDataByYear(generateTestData2(), 0).get(0).year);
	}

	private MergedData generateTestData1() {
		MergedData data = new MergedData();
		try {
			data.start = StringUtilities.sortableTimeStringToDays("20001226");
			data.duration = 100;
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	private MergedData generateTestData2() {
		MergedData data = new MergedData();
		try {
			data.start = StringUtilities.sortableTimeStringToDays("20000126");
			data.duration = 100;
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		return data;
	}

}
