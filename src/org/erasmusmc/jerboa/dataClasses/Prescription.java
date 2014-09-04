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

import java.util.HashSet;
import java.util.Set;

import org.erasmusmc.utilities.StringUtilities;

public class Prescription {	
  public long start;
  public long duration;
  public Set<ATCCode> atcCodes = new HashSet<ATCCode>(1);
  public String patientID;
  
  public static void main(String[] args){
  	ATCCode code = new ATCCode("ABCD_123:VS");
  	System.out.println(code);
  	System.out.println(code.atc);
  	System.out.println(code.dose);
  	System.out.println(code.exposureCode);
  	System.out.println();
  	
  	code = new ATCCode("ABCD:VS");
  	System.out.println(code);
  	System.out.println(code.atc);
  	System.out.println(code.dose);
  	System.out.println(code.exposureCode);
  	System.out.println();
  	
  	code = new ATCCode("ABCD_123");
  	System.out.println(code);
  	System.out.println(code.atc);
  	System.out.println(code.dose);
  	System.out.println(code.exposureCode);
   	System.out.println();
  	
  	code = new ATCCode("ABCD");
  	System.out.println(code);
  	System.out.println(code.atc);
  	System.out.println(code.dose);
  	System.out.println(code.exposureCode);
  	System.out.println();
  	
  	Prescription prescription = new Prescription();
  	prescription.setATCCodes("ABC_123:VS+DEF_123:S");
  	System.out.println(prescription.atcCodes.toString());
  	System.out.println(prescription.getATCCodesAsString());
  }
  

  public long getEnd() {
    return start + duration;
  }

  public Prescription() {
  }

  public Prescription(Prescription prescription) {
    this.atcCodes = new HashSet<ATCCode>(prescription.atcCodes);
    this.start = prescription.start;
    this.patientID = prescription.patientID;
    this.duration = prescription.duration;
  }
  
  public void resetATCCodes(){
  	atcCodes = new HashSet<ATCCode>(1);
  }
  
  public void setATCCodes(String string){
  	resetATCCodes();
  	for (String atcCode : string.split("\\+"))
  		atcCodes.add(new ATCCode(atcCode));
  }
  
  public String getATCCodesAsString(){
  	return StringUtilities.joinSorted(atcCodes, "+");
  }
  
  public static class ATCCode implements Comparable<ATCCode>{
  	public String atc;
  	public String exposureCode;
  	public String dose;
  	
  	public ATCCode(ATCCode other){
  		atc = other.atc;
  		exposureCode = other.exposureCode;
  		dose = other.dose;
  	}
  	
  	public ATCCode(String string){
  		int start = 0;
  		boolean isDose = false;
  		for (int i = 0; i < string.length(); i++){
  			char ch = string.charAt(i);
  			if (ch == '_'){
  				atc = string.substring(0,i);
  				start = i+1;
  				isDose = true;
  			} else if (ch == ':') {
  				if (atc == null)
  					atc = string.substring(0,i);
  				else
  					dose = string.substring(start,i);  				
  				start = i+1;
  				isDose = false;
  			}
  		}
  		if (atc == null)
  			atc = string;
  		else if (isDose)
  			dose = string.substring(start);
  		else 
  			exposureCode = string.substring(start);
  	}
  	
  	public ATCCode() {
			// TODO Auto-generated constructor stub
		}

		public String toString(){
  		if (exposureCode == null && dose == null)
  			return atc;
  		else if (exposureCode == null)
  				return atc+"_"+dose;
  			else if (dose == null)
  				return atc+":"+exposureCode;
  			else return atc+"_"+dose+":"+exposureCode;
  	}  		
  	
  	public int hashCode(){
  		return toString().hashCode();
  	}
  	
  	public boolean equals(Object other){
  		//if (!(other instanceof ATCCode))
  		//	throw new RuntimeException("Illegal compare!");
  		return ((other instanceof ATCCode) && 
  				((atc != null && atc.equals(((ATCCode)other).atc)) || (atc == null && ((ATCCode)other).atc == null)) && 
  				((dose != null && dose.equals(((ATCCode)other).dose)) || (dose == null && ((ATCCode)other).dose == null)) &&
  				((exposureCode != null && exposureCode.equals(((ATCCode)other).exposureCode)) || (exposureCode == null && ((ATCCode)other).exposureCode == null)));
  	}
  	
  	@Override
  	public int compareTo(ATCCode arg0) {
  		return toString().compareTo(arg0.toString());
  	}
  }


}