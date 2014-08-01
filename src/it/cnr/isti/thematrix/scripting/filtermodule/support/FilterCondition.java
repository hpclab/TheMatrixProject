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
 *
 * Represents a boolean condition. Assumes that the arguments are either
 * from one single module or they are parameters.
 * @author edoardovacchi
 */
public interface FilterCondition {

	/**
	 * 
	 * @return true IFF the condition holds
	 */
    @SuppressWarnings(value = "unchecked")
    boolean apply();
    
    /**
     * changes the symbols in this filter to reference the given module
     * assumes that the filter applies to only one dataset at a time;
     * that is, the filter cannot predicate about more than one dataset at once
     * 
     * e.g. MyDatasetX.Value1 > MyDatasetY.ValueB is UNSUPPORTED
     * 
     * @param m a new module with the same schema of the inputs
     */
    public void changeInput(MatrixModule m);
}
