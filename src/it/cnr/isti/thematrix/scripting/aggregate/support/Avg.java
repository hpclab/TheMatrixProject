package it.cnr.isti.thematrix.scripting.aggregate.support;


import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.aggregate.support.AggregateFunction;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.utils.DataType;


/**
 * Avg implements the averagig AggregateFunction, it only allows INT and FLOAT
 * types, counts all non-null, non MISSING elements, returns a Float average.
 * 
 * TODO it would be better to have the accumulator as a Double and then reduce
 * the precision at the end to return a float
 * 
 * @author edoardovacchi
 */
public class Avg extends AggregateFunction {

	/**
	 * this Float aliases to the superclass accumulator
	 */
	Float accFloat;

	/**
	 * stores the element count for the average
	 */
	int count = 0;

	/**
	 * we assume that the Symbol reports a DataType matching the object it contains
	 */
    @Override
    public void compute(Symbol<?> r) 
    {    	
    	/**
    	 * semantics: only count non missing values
    	 */
    	if (r.value == null || r.type == DataType.MISSING)
			return;
    	/******************************/
    	count++; // we have one more element in the average
    	/******************************/
		if (this.type==DataType.INT) {
			if (r.type != DataType.INT)
				this.computeTypeException(r);
			accFloat += ((Integer) r.value).floatValue();
		}
		else { //  FLOAT
			if (r.type != DataType.FLOAT)
				this.computeTypeException(r);
			accFloat += ((Symbol<Float>) r).value;
		}
    }
    
    @Override
    public Object getResult() {
    	if (count==0) return null; // no data --> return missing
    	else return new Float((accFloat)/count);
    }

    @Override
    public void reset() { accumulator=accFloat=new Float(0.0f); count=0; }

	public Avg(DataType type) {
		super(type); //sets the input type, NOT the accumulator type
		if (type!=DataType.INT && type != DataType.FLOAT)		
			throw new RuntimeException("Internal error in AggregateFunction.Avg constructor(DataType): "+type);
		reset();
	}

    
}
