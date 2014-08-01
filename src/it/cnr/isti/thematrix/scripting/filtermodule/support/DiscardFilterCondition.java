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
package it.cnr.isti.thematrix.scripting.filtermodule.support;

import it.cnr.isti.thematrix.scripting.sys.MatrixModule;

/**
 * inverts the semantics of a filter
 * @author edoardovacchi
 */
public class DiscardFilterCondition implements FilterCondition {

	private FilterCondition fc;
	public DiscardFilterCondition(FilterCondition fc) {
		this.fc = fc;
	}
    @SuppressWarnings(value = "unchecked")
	public boolean apply() {
    	return !fc.apply();
    }
    
    public String toString() {
    	return "DISCARD -- " + fc.toString(); 
    }
	@Override
	public void changeInput(MatrixModule m) {
		fc.changeInput(m);
	}
	public FilterCondition getFilterCondition() {
		return fc;
	}
    
}
