package org.erasmusmc.jerboa.dataClasses;

import java.util.zip.DataFormatException;

import org.erasmusmc.utilities.StringUtilities;

public class InputFileUtilities {
	public static long firstLegalDate = getFirstLegalDate();
	public static long lastLegalDate = getLastLegalDate();
	
	public static long convertToDate(String sortableDateString, boolean allowEmpty) throws Exception{
		if (sortableDateString.length() == 0){
			if (allowEmpty)
			  return ConstantValues.UNKNOWN_DATE;
			else
				throw new RuntimeException(" Missing date ");
		}
		long date = StringUtilities.sortableTimeStringToDays(sortableDateString);
    if (date < firstLegalDate || date > lastLegalDate)
    	throw new RuntimeException(" Illegal date: '" + sortableDateString + "' ");
		return date;
	}
	
  private static long getFirstLegalDate() {
  	try {
			return StringUtilities.sortableTimeStringToDays("18500101");
		} catch (DataFormatException e) {
			e.printStackTrace();
			return -1;
		}
	}
  
  private static long getLastLegalDate() {
  	try {
			return StringUtilities.sortableTimeStringToDays("21000101");
		} catch (DataFormatException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static byte convertToGender(String genderString, boolean allowUnknownGenders) {
    if (genderString.length() == 0){
    	if (allowUnknownGenders)
        return Patient.UNKNOWN_GENDER;
    	else
    		throw new RuntimeException(" Missing gender ");
    }else if (genderString.toLowerCase().charAt(0) == 'm')
    	return Patient.MALE;
    else
    	return Patient.FEMALE;	}
}
