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
