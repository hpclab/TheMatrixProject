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

import junit.framework.TestCase;

import org.erasmusmc.utilities.StringUtilities;

public class TestDates extends TestCase{

	/**
	 * @param args
	 * @throws DataFormatException 
	 */
	public void testDates() throws DataFormatException {
		doTest("19691230");
		doTest("19691231");
		doTest("19700101");
		doTest("19700102");
	}

	public void doTest(String dataString) throws DataFormatException{
		long days = StringUtilities.sortableTimeStringToDays(dataString);
		String reconverted = StringUtilities.daysToSortableDateString(days);
		assertEquals(dataString, reconverted);
	}
}
