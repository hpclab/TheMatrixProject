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

import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * Bean class for ward.csv
 * @author marco d
 */
public class WardBean {
	private String WARD;
	private String TYPE_WARD;
	
	    
    final protected static CellProcessor[] processors = new CellProcessor[] { 
            new NotNull(), // WARD
            new NotNull(), // TYPE_WARD
    };
    
   
	/**
	 * Default empty constructor;
	 */
	public WardBean(){}
	
	public WardBean(String ward, String  type_ward ){
		WARD=ward;
		TYPE_WARD=type_ward;

	}
	public String getWARD() {
		return WARD;
	}

	public void setWARD(String ward) {
		WARD = ward;
	}
	public String getTYPE_WARD() {
		return TYPE_WARD;
	}

	public void setTYPE_WARD(String type_ward) {
		this.TYPE_WARD = type_ward;
	}

	@Override
	public String toString() {
		return "WardRecord [WARD=" + WARD + ", TYPE_WARD=" + TYPE_WARD+"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((TYPE_WARD == null) ? 0 : TYPE_WARD.hashCode());
		result = prime * result + ((WARD == null) ? 0 : WARD.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WardBean other = (WardBean) obj;
		if (TYPE_WARD == null) {
			if (other.TYPE_WARD != null)
				return false;
		} else if (!TYPE_WARD.equals(other.TYPE_WARD))
			return false;
		if (WARD != other.WARD)
			return false;
		return true;
	}
	
}
