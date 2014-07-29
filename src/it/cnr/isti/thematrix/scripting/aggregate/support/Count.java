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
