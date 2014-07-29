package org.erasmusmc.jerboa.dataClasses;

import org.erasmusmc.utilities.StringUtilities;

public class Patient {
  public String patientID;
  public long birthdate;
  public byte gender;
  public long startdate;
  public long enddate;
  
  public static byte MALE = 1;
  public static byte FEMALE = 2;
  public static byte UNKNOWN_GENDER = 3;

	
  
	public int AgeOnDate(long date) {
		int age = -1;
		
		if ((birthdate != -1) && (date != -1)) {
			String birthDateString = StringUtilities.daysToSortableDateString(birthdate);
			int birthYear = Integer.parseInt(birthDateString.substring(0, 4));
			int birthMonth = Integer.parseInt(birthDateString.substring(4,6));
			int birthDay = Integer.parseInt(birthDateString.substring(6,8));

			String dateString = StringUtilities.daysToSortableDateString(date);
			int dateYear = Integer.parseInt(dateString.substring(0, 4));
			int dateMonth = Integer.parseInt(dateString.substring(4,6));
			int dateDay = Integer.parseInt(dateString.substring(6,8));
			
			age = dateYear - birthYear;
			if (birthMonth > dateMonth) {
				age = age - 1;
			}
			else {
				if (birthMonth == dateMonth) {
					if (birthDay > dateDay) {
						age = age - 1;
					}
				}
			}
		}
		
		return age;
	}	
}
