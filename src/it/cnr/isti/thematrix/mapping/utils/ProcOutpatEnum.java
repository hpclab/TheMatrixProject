/*
 * Copyright (c) 2010-2014 "HPCLab at ISTI-CNR"
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.isti.thematrix.mapping.utils;

import it.cnr.isti.thematrix.mapping.utils.WardEnum.TypeWard;
/**
 * procOutpatEnum Class for TYPE_OUTPAT enum
 * TYPE_OUTPAT check in no case sensitive way.
 * 
 * @author marco
 *
 */
public class ProcOutpatEnum {

	public static enum Type_Outpat{
		CLIN, IMAG, LAB, INSTR, PROC;
	}
	
	/**
	 * Method getType_Outpat for get a Type_Outpat type given a string value (no case sensitive)
	 * 
	 * @param tWard
	 * @return Type_Outpat.CLIN or IMAG or LAB or INSTR or PROC
	 */
	public static Type_Outpat getType_Outpat(String tWard){
		return Type_Outpat.valueOf(tWard.toUpperCase());
	}
	
	/**
	 * Method isInEnum to check if the String value is a correct Type_Outpat (no case sensitive)
	 * 
	 * @param value
	 * @return true if parameter is a valid Type_Outpat enum value
	 */
	public static boolean isInEnum(String value) {
		  for (Type_Outpat t : Type_Outpat.values()) {
		    if(t.name().equals(value.toUpperCase())) { return true; }
		  }
		  return false;
	}

}
