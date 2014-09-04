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
package org.erasmusmc.jerboa;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.erasmusmc.utilities.ReadCSVFile;

/**
 * Uses table downloaded from:
 * http://www.whocc.no/atc_ddd_alterations__cumulative_/atc_alterations/
 * @author schuemie
 *
 */
public class ATC2NewATC {
	
	private int previousCol;
	private int newCol;
	private int yearCol;
	private Map<String, NewCodeAndYear> atc2newCodeAndYear;
	
	public static void main(String[] args){
		ATC2NewATC atc2NewATC = new ATC2NewATC("/home/schuemie/ATCremapping.csv");
		System.out.println(atc2NewATC.getNewATC("G01AF01") + " should be P01AB01");
	}
	


	public ATC2NewATC(InputStream stream){
  	init(new ReadCSVFile(stream));
  }
  
  public ATC2NewATC(String filename){
  	init(new ReadCSVFile(filename));
  }
  
  /**
   * Returns null if ATC has not changed
   * @param atc
   * @return
   */
  public String getNewATC(String atc) {
  	NewCodeAndYear newCodeAndYear = atc2newCodeAndYear.get(atc);
  	if (newCodeAndYear == null)
  		return null;
  	else return newCodeAndYear.atc;
	}
  
  /**
   * Returns null if ATC has not changed
   * @param atc
   * @return
   */
  public Integer getYearOfChange(String atc) {
  	NewCodeAndYear newCodeAndYear = atc2newCodeAndYear.get(atc);
  	if (newCodeAndYear == null)
  		return null;
  	else return newCodeAndYear.year;
	}

  
  private void init(ReadCSVFile in){
  	Iterator<List<String>> iterator = in.iterator();
  	atc2newCodeAndYear = new HashMap<String, NewCodeAndYear>();
  	if (iterator.hasNext())
  	  processHeader(iterator.next());
  	while (iterator.hasNext())
  		processRow(iterator.next());
  }

	private void processRow(List<String> cells) {
		String previousATC = extractATC(cells.get(previousCol));
		String newATC = extractATC(cells.get(newCol));
		if (previousATC.length() != newATC.length()) //Do not allow mappings from one level to another
			return;
		String year;
		if (yearCol == -1) 
			year = "0";
		else
			year = cells.get(yearCol);
		atc2newCodeAndYear.put(previousATC, new NewCodeAndYear(newATC, Integer.parseInt(year)));
	}
	
	private String extractATC(String string){
		for (int i =0; i < string.length(); i++)
			if (!Character.isLetterOrDigit(string.charAt(i)))
				return string.substring(0,i);
		return string;
	}
	
	private void processHeader(List<String> cells) {
		for (int i = 0; i < cells.size(); i++){
			if (cells.get(i).toLowerCase().contains("previous"))
				previousCol = i;
			if (cells.get(i).toLowerCase().contains("new atc"))
				newCol = i;
			if (cells.get(i).toLowerCase().contains("year"))
				yearCol = i;
		}
	}

	private class NewCodeAndYear {
		public String atc;
		public int year;
		public NewCodeAndYear(String atc, int year){
			this.atc = atc;
			this.year = year;
		}
	}

}
