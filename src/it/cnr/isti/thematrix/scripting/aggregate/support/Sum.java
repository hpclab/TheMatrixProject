package it.cnr.isti.thematrix.scripting.aggregate.support;

import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.utils.DataType;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Sum implements the sum AggregateFunction, only allows INT and FLOAT types.
 *
 * TODO may need an int to float cast in the float version.
 * 
 * @author edoardovacchi
 */
public class Sum extends AggregateFunction {
	/**
	 * FIXME NOTE FROM EDOARDO FOR REFACTORING THE AGGREGATE FUNCTION
	 * IMPLEMENTATION
	 * 
	 * Sarebbe meglio modificare la mappa `aggregates` in modo che il tipo venga
	 * tenuto in considerazione. Ad esempio anziché mettere nella hashmap solo
	 * il nome della funzione puoi fare una chiave NOMEFUNZIONE$TIPO e poi per
	 * interrogarla (riga 96) anziché fare solo get(tuple._1) fai
	 * get(tuple._1+"$"+<tipo-dell-argomento>).
	 */

	/**
	 * FIXME -- test and cleanup of old code
	 * 
	 * Note from Massimo and Emanuele: we now provide the type information
	 * dynamically to the function by using a one parameter constructor, so no
	 * need to replicate entries in the hashmap; the abstract class holds a type
	 * field that tells the implementation the type to expect.
	 */
	
	
	/**
	 * this Integer shadows the superclass accumulator
	 */
	Integer accInt;
	/**
	 * this Float shadows the superclass accumulator
	 */
	Float accFloat;

	@Override
	public void compute(Symbol<?> r) {
		
		// if we get here on a null value, we are still fine.
		if (r.value == null || r.type == DataType.MISSING)
			return;

		if (this.type==DataType.INT) {
			if (r.type != DataType.INT)
		    	  this.computeTypeException(r);
			accInt += ((Symbol<Integer>) r).value;
		}
		else { //  FLOAT
			// TODO we MAY need a cast here
			if (r.type != DataType.FLOAT)
		    	  this.computeTypeException(r);
			accFloat += ((Symbol<Float>) r).value;
		}
	}
	
	@Override
	public Object getResult() {
		if (this.type==DataType.INT)
			return accInt;
		else // it is Float
			return accFloat;
	}

	
	@Override
	public void reset() {
		// I don't think we should have this method defined, anyway we just use it in the constructor
		if (type == DataType.INT) {
			accumulator = new Integer(0); accInt = (Integer) accumulator;
		}
		else {
			accumulator = new Float(0.0); accFloat = (Float) accumulator;
		}
	}

	public Sum(DataType type) {
		super(type);
		if (type!=DataType.INT && type != DataType.FLOAT)		
			throw new RuntimeException("Internal error in AggregateFunction.Sum constructor(DataType): "+type);
		reset();
	}	
}
