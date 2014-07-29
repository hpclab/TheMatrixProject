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
