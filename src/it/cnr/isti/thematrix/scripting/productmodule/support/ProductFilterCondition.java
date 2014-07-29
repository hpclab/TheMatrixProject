/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.productmodule.support;

import it.cnr.isti.thematrix.scripting.filtermodule.support.FilterCondition;
import it.cnr.isti.thematrix.scripting.sys.DatasetRecord;
import it.cnr.isti.thematrix.scripting.sys.Literal;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Operation;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author edoardovacchi
 */
public class ProductFilterCondition implements FilterCondition {
 
    public final List<?> operand;
    public final Operation<Object,Object,Object> op;
    public DatasetRecord input;

    public ProductFilterCondition(List<Symbol<?>> operand, Operation<Object,Object,Object> op) {
        this.operand = operand;
        this.op = op;
    }
    
    @Override
    public String toString() {
        return String.format("FILTER:: %s(%s)", op, operand);
    }
    
    // it is safe to assume here that Operation is boolean
    @SuppressWarnings("unchecked")
    @Override
    public boolean apply() 
    {    	
    	List<Symbol<?>> new_operands = new ArrayList<Symbol<?>>(operand.size());
    	for (Object o: operand)
    	{
    		Symbol<?> s = (Symbol<?>) o;
    		if (s.getClass() == Literal.class) // if it is literal, leave it alone
    			new_operands.add(s);
    		else // if it is a field, then update it
    			new_operands.add(input.get(s.name));
    		
    	}
    	 	
        return (Boolean) op.apply(new_operands, null);
    }
    
    public void setDatasetRecord(DatasetRecord dr)
    {
    	this.input = dr;
    }
    
	@Override
	public void changeInput(MatrixModule m) 
	{
		// this does nothing. TODO: throw an exception or warning?
	}

}
