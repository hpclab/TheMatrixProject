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

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.utils.DataType;

/**
 * Min implements the minimum AggregateFunction, defined on INT, FLOAT, DATE and
 * BOOLEAN types only. Exploits the comparable interface of the object in the
 * accumulator. Null values are ignored after issuing a warning in the logs.
 * 
 * TODO Value casting is currently unsupported. May need an int to float cast in
 * the float version. May need proper casting in the case of boolean arguments.
 * 
 * @author edoardovacchi
 */
public class Min extends AggregateFunction {
	/**
	 * we assume that the Symbol reports a DataType matching the object it contains
	 */
	@Override
	public void compute(Symbol<?> r) {
		Object o = r.value;

		// Semantic: if values is null, skip
		// if value is of incorrect type, break havoc
		if (o == null || r.type == DataType.MISSING) {
			LogST.logP(1, "** WARNING **: null value when computing "
					+ this.getClass().getName());
			return;
		} else if (r.type != this.type) // for now we do not cast values
	    	  this.computeTypeException(r);

		if (accumulator == null) {
			accumulator = o;
			return;
		} else {
			Comparable<Object> val = (Comparable<Object>) o;
			if (val.compareTo(accumulator) < 0)
				accumulator = val;
		}
	}

	public Min(DataType type) {
		super(type);
		if (type != DataType.INT && type != DataType.FLOAT
				&& type != DataType.BOOLEAN && type != DataType.DATE)
			throw new RuntimeException(
					"Internal error in AggregateFunction.Min constructor(DataType): "
							+ type);

	}

}
