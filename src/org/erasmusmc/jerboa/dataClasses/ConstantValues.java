package org.erasmusmc.jerboa.dataClasses;

import java.util.zip.DataFormatException;

import org.erasmusmc.utilities.StringUtilities;

public class ConstantValues {
  public static long UNKNOWN_DATE = getUnknownDate();
  public static long UNKNOWN_DURATION = 0;
  
	private static long getUnknownDate() {
		try {
			return StringUtilities.sortableTimeStringToDays("18000101");
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
