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

import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
/**
 * Bean class for PROC_OUTPAT.csv
 * @author marco d
 */
public class ProcOutpatBean {
	private String PROC_COD;
	private String TYPE_OUTPAT;
	
	    
    final protected static CellProcessor[] processors = new CellProcessor[] { 
            new NotNull(), // PROC_COD
            new NotNull(), // TYPE_OUTPAT
    };
    
   
	/**
	 * Default empty constructor;
	 */
	public ProcOutpatBean(){}
	
	public ProcOutpatBean(String proc_code, String  type_outpat ){
		PROC_COD=proc_code;
		TYPE_OUTPAT=type_outpat;

	}
	public String getPROC_COD() {
		return PROC_COD;
	}

	public void setPROC_COD(String proc_code) {
		PROC_COD = proc_code;
	}
	public String getTYPE_OUTPAT() {
		return TYPE_OUTPAT;
	}

	public void setTYPE_OUTPAT(String atc) {
		this.TYPE_OUTPAT = atc;
	}

	@Override
	public String toString() {
		return "ProcOutpatBean [PRODUCT_CODE=" + PROC_COD + ", TYPE_OUTPAT=" + TYPE_OUTPAT+"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((TYPE_OUTPAT == null) ? 0 : TYPE_OUTPAT.hashCode());
		result = prime * result
				+ ((PROC_COD == null) ? 0 : PROC_COD.hashCode());
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
		ProcOutpatBean other = (ProcOutpatBean) obj;
		if (TYPE_OUTPAT == null) {
			if (other.TYPE_OUTPAT != null)
				return false;
		} else if (!TYPE_OUTPAT.equals(other.TYPE_OUTPAT))
			return false;
		if (PROC_COD == null) {
			if (other.PROC_COD != null)
				return false;
		} else if (!PROC_COD.equals(other.PROC_COD))
			return false;
		return true;
	}
	

}
