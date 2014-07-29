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
