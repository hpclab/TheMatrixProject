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
package it.cnr.isti.thematrix.scripting.aggregate.support;

import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.utils.DataType;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Count implements the AggregateFunction counting data rows. It is defined on
 * any type and counts the number of values provided, including null and MISSING
 * values.
 * 
 * TODO a separate version that ignores null and MISSING values (e.g.
 * CountValues) should be provided
 * 
 * @author edoardovacchi
 */
public class Count extends AggregateFunction {

	int count = 0;

	@Override
	public void compute(Symbol<?> r) {
		count++;
	}

	@Override
	public Object getResult() {
		return count;
	}

	@Override
	public void reset() {
		count = 0;
	}

	/**
	 * Count does not really need the argument type (it computes and returns an
	 * int regardless of its arguments' type.
	 * 
	 * @param type
	 */
	public Count(DataType type) {
		super(type);
	}

}
