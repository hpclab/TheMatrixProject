package it.cnr.isti.thematrix.scripting.aggregate.support;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.aggregate.support.AggregateFunction;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.utils.DataType;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * StDev implements the standard deviation AggregateFunction, it only allows INT
 * and FLOAT types, counts all non-null, non MISSING elements, returns a Float
 * average. This class redefines its own internal accumulators as Doubles, and
 * overrides all parent's class methods. Returns missing if count of seen
 * elements is zero.
 * 
 * FIXME class doesn't handle int and floats (see Sum.java) and has int
 * accumulators
 * 
 * FIXME class is not updated to the new model for AggregateFunction, where the
 * return type is specified in the constructor
 * 
 * @author edoardovacchi, massimo
 */
public class StDev extends AggregateFunction {

	/**
	 * accumulator for the squared values
	 */
	Double accSq = 0.0;
	/**
	 * accumulator for the values
	 */
	Double acc = 0.0;
	
	/**
	 * seen value count; 0 means the series was empty.
	 */
	int count = 0;

	@Override
	public void compute(Symbol<?> r) {
		double dValue;
		
	   	/**
    	 * semantics: only count non missing values
    	 */
    	if (r.value == null || r.type == DataType.MISSING)
			return;
    	/******************************/

    	// we have one more element in the average
		count++;

	   	/******************************/
		if (this.type==DataType.INT) {
			if (r.type != DataType.INT)
				this.computeTypeException(r);
			dValue = ((Integer) r.value).doubleValue();
		}
		else { //  FLOAT
			if (r.type != DataType.FLOAT)
				this.computeTypeException(r);
			dValue = ((Symbol<Float>) r).value.doubleValue();
		}
		acc += dValue;
		accSq += dValue*dValue; 
	}

	@Override
	public Object getResult() {
		double avg = acc / count;
		return new Float(Math.sqrt(accSq / count - avg * avg));
	}

	@Override
	public void reset() {
		accSq = new Double (0.0);
		acc = new Double(0.0);
		count = 0;
	}

	/**
	 * Here we only need to know the type of the arguments we receive (return is
	 * a Float anyway)
	 * 
	 * @param type
	 */
	public StDev(DataType type) {
		super(type);
		if (type!=DataType.INT && type != DataType.FLOAT)		
			throw new RuntimeException("Internal error in AggregateFunction.StDev constructor(DataType): "+type);
		reset();
	}

}
