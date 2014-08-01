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

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.Literal;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.MissingSymbolException;
import it.cnr.isti.thematrix.scripting.sys.Operation;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.SymbolMissing;

/**
 * Implements an instance of a boolean condition (a predicate) on two given values
 * @author edoardovacchi
 */
public class SingleFilterCondition implements FilterCondition {
    private Symbol<?> operand1;
    private Symbol<?> operand2;
    
    public final Operation<Object,Object,Object> op;

    public SingleFilterCondition(Symbol<?> operand1, Symbol<?> operand2, Operation<Object,Object,Object> op) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.op = op;
    }
    
    public void changeInput(MatrixModule m) {
    	Symbol<?> r = m.get(operand1.name);
    	operand1 = r;
 
//		if (SymbolMissing.class == operand2.getClass()) {
//			LogST.logP(0,
//					"SingleFilterCondition.changeInput() Ignoring empty symbol " + m.name + " : " + operand2.toString());
//		} else 
    	if ( Literal.class !=  operand2.getClass()) {
			/**
			 * if it's a literal we do not need to act on it 
			 * 
			 * FIXME can/should it be the same of operand1?
			 */
			try {
				Symbol<?> r2 = m.get(operand2.name);
				operand2 = r2;

			} catch (MissingSymbolException ex) {
				/**
				 * if it's missing we log and continue; actually we should be able to distinguish the initialization, when
				 * it's safe, and the execution, when it may be harmful
				 */
				LogST.logP(0,"SingleFilterCondition.changeInput() Ignoring missing key in " + m.name + " : '" + operand2.toString()
						+ "'. Expecting constant or variable.");
				LogST.logException(ex);
			} catch (NullPointerException e) {
				/**
				 * something odd happened in the symbol tables
				 */
				LogST.logP(0,"SingleFilterCondition.changeInput() NullPointer Exception in "+ m.name 
						+ " : '" + operand2 != null? (operand2.toString()) : " operand2  is null");
				LogST.logException(e);
			}
    	}; 
    	// else throw new Error("SingleFilterCondition.changeInput() : unexpected Symbol<?>");
    	
//    	try {
//    		Symbol<?> r2 = m.get(operand2.name);
//    		operand2 = r2;
//    	} catch (MissingSymbolException ex) {
//        	System.err.println("Ignoring missing key in "+m.name+" : '"+operand2.name+"'. Assuming constant or variable.");
//    	}
    }
     
    @Override
    public String toString() {
        return String.format("FILTER:: %s %s %s", operand1, op, operand2);
    }
    
    // it is safe to assume here that Operation is boolean
    @SuppressWarnings("unchecked")
    @Override
    public boolean apply() {
        return (Boolean) op.apply(operand1.value, operand2.value);
    }
}
