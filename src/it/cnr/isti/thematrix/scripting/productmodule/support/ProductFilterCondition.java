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
