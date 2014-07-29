/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.aggregate.support;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.utils.DataType;

/**
 * Base class for aggregate functions. Aggregates are all based on an
 * accumulator that is updated for each row that is processed
 * 
 * @author edoardovacchi
 */
public abstract class AggregateFunction {
	protected Object accumulator = null;
	protected DataType type = DataType.MISSING;

	public abstract void compute(Symbol<?> r);

	public Object getResult() {
		return accumulator;
	}

	public void reset() {
		accumulator = null;
	}

	/**
	 * Empty costructor, that we need to specify explicitly in order to preserve
	 * the legacy behaviour, i.e. that no info about the type is provided to the
	 * implementation class.
	 * 
	 * TODO remove as soon as the new behaviour is tested
	 */
	protected AggregateFunction() {
	}

	/**
	 * Constructor with one parameter, specifying the datatype the concrete
	 * function (the implementation class) should expect. Needed to allow
	 * passing the type information to the implementation classes without
	 * changing the interpreter code. All aggregateF are [F: x, acc -> acc],
	 * where acc is either same type as x or a fixed type (e.g. a n int
	 * counter). Hence this information should be enough.
	 * 
	 * @param argumentType
	 *            the type of the arguments
	 */
	/* protected */ public AggregateFunction(DataType argumentType) {
		this.type = argumentType;
		LogST.logP(1,
				"abstract class AggregateFunction : constructor with parameters was used");
	}
	
	/**
	 * Method used by subclasses' compute() methods to throw error if an unexpected type is received.
	 * 
	 * @param r the symbol seen by the subclass.
	 */
	protected void computeTypeException (Symbol<?> r) {
		LogST.logP(-1, "ERROR : in " + this.getClass().getName()
				+ " compute() unexpected type in symbol " + r.toString());
		throw new RuntimeException("Internal error in "
				+ this.getClass().getName() + " compute() " + r.type);
	
	}
}
