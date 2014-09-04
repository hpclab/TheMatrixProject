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
package org.erasmusmc.jerboa.postProcessing;

public class Signal implements Comparable<Signal>{
	public String atc;
	public String eventType;

	public Signal(String atc, String eventType) {
		this.atc = atc;
		this.eventType = eventType;
	}

	public int hashCode(){
		return (atc+eventType).hashCode();
	}
	
	public String toString(){
		return atc+"_"+eventType;
	}
	
	public boolean equals(Object object){
		if (object instanceof Signal){
			Signal other = (Signal)object;
			return (other.atc.equals(atc) && other.eventType.equals(eventType));
		} else
			return false;
	}

	@Override
	public int compareTo(Signal arg0) {
		int result = this.atc.compareTo(arg0.atc);
		if (result == 0)
			return this.eventType.compareTo(arg0.eventType);
		else
			return result;
	}
}
