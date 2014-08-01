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

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * FIXME why no javadoc?
 * 
 * FIXME rename the class as ProductCodeStaticsBean 
 * 
 * @author marcodistefano, emanuele
 *
 */
public class RecordBean 
{
	
	private String PRODUCT_CODE;
	private String ATC;
	private Double duration_of_box;
	
	//TODO: verify the Optional field
    final protected static CellProcessor[] processors = new CellProcessor[] { 
            new NotNull(), // product_code
            new NotNull(), // atc
            new Optional(new ParseDouble()), // duration_of_box
    };
    
   
	/**
	 * Default empty constructor;
	 */
	public RecordBean(){}
	
	public RecordBean(String product_code, String  atc, double duration ){
		PRODUCT_CODE=product_code;
		ATC=atc;
		duration_of_box= duration;

	}
	public String getPRODUCT_CODE() {
		return PRODUCT_CODE;
	}

	public void setPRODUCT_CODE(String product_code) {
		PRODUCT_CODE = product_code;
	}
	public String getAtc() {
		return ATC;
	}

	public void setATC(String atc) {
		this.ATC = atc;
	}
	
	public Double getDuration_of_box() {
		return duration_of_box;
	}

	public void setDuration_of_box(Double duration_of_box) {
		this.duration_of_box = duration_of_box;
	}

	@Override
	public String toString() {
		return "RecordBean [PRODUCT_CODE=" + PRODUCT_CODE + ", ATC=" + ATC
				+ ", duration_of_box=" + duration_of_box + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ATC == null) ? 0 : ATC.hashCode());
		result = prime * result
				+ ((PRODUCT_CODE == null) ? 0 : PRODUCT_CODE.hashCode());
		long temp;
		temp = Double.doubleToLongBits(duration_of_box);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		RecordBean other = (RecordBean) obj;
		if (ATC == null) {
			if (other.ATC != null)
				return false;
		} else if (!ATC.equals(other.ATC))
			return false;
		if (PRODUCT_CODE == null) {
			if (other.PRODUCT_CODE != null)
				return false;
		} else if (!PRODUCT_CODE.equals(other.PRODUCT_CODE))
			return false;
		if (Double.doubleToLongBits(duration_of_box) != Double
				.doubleToLongBits(other.duration_of_box))
			return false;
		return true;
	}
	

	
}