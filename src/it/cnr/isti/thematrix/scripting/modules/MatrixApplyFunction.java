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
package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.FilterModule;
import it.cnr.isti.thematrix.scripting.filtermodule.support.FilterCondition;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.DataType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * in its funcArgs argument, this constructor receives the actual referecens
 * to the attributes of the input module. However, this does not create problems
 * like in {@link FilterModule} because the actual references are re-extracted in
 * the {@link #setup()} routine, which is called in turn by
 *  {@link MatrixApplyFunction#changeInput(MatrixModule)} when necessary
 * 
 * @author edoardovacchi
 */
public class MatrixApplyFunction extends MatrixModule {
    private MatrixModule inputModule;
    private final String functionName;
    private final List<Symbol<?>> funcArgs;
    private final Symbol<?> result;
	private FilterCondition filter;
	
	private Operation<List<Symbol<?>>,Void,Object> function;

	/**
	 * 
	 * 
	 * @param name name of the module 
	 * @param inputTable input module
	 * @param schemaName schema of the input module
	 * @param functionName name of the function 
	 * @param funcArgs a list of references to the attributes of the input modules
	 * @param result a freshly-generated attribute where the result should be written to
	 * @param fc a filter definition
	 */
    public MatrixApplyFunction(
            String name, 
            String inputTable, 
            String schemaName, 
            String functionName, 
            List<Symbol<?>> funcArgs, 
            Symbol<?> result,
            FilterCondition fc) {
        super(name);
        this.inputModule = TheMatrixSys.getModule(inputTable);
        inputModule.schemaMatches(schemaName);
        inputModule.addConsumer(this);
        this.functionName = functionName;
        this.funcArgs = funcArgs;
        this.result = result;
        this.filter = fc;
        LogST.logP(0, "MatrixApplyFunction() "+name+" function "+functionName+" arguments "+funcArgs.toString());
    }
    
    /**
     * this setup function uses the attribute references found in {@link #funcArgs} 
     * to setup the internal pointers to the correct values
     * 
     */
    @Override
    public void setup() {
        this.setSchema(inputModule.getSchema());
        
        
        // instead of copying, we reference the source symbols
        List<Symbol<?>> inputAttributes = inputModule.attributes();
        int nAttrs = inputAttributes.size();
        for (int i = 0; i < nAttrs; i++) {
        	if (inputAttributes.get(i).name.equals(result.name)) {
        		this.put(result.name, result); continue;
        	}
            // re-link this field to inputModule's corresponding reference
            this.put(inputAttributes.get(i).name, inputAttributes.get(i));
        }
        
        

        // each function takes in 2 parameters, a dataset record (i.e., a list of symbols)
        // and a list of literals (unused in ApplyFunction, so it's Void)
        // return type will be Object (might need casting later)
        try {
        	this.function = TheMatrixSys.getFuncTable().get(result.type, functionName.toLowerCase());
        } catch (MissingOperationException ex) {
        	LogST.logP(0,
        			"ERROR: ApplyFunction.setup() for module: "+this.name+" required function "+functionName+"("+result.type+") not found.");
        	throw new UndefinedFunctionException("ApplyFunction "+functionName+"("+result.type+") is undefined" );
        }
    }
    

	@Override
	public void exec() {
		LogST.logP(2,"NO-OP MatrixApplyFunction.exec() called on "+this.name);
	}

	
	/**
	 * invokes {@link #setup()} to re-extract the correct references from the given new input module
	 */
	@Override
	public void changeInput(MatrixModule m) {
//		if (inited) throw new Error("MatrixFilter.changeInput() not allowed after reset() " + this.name);
		inputModule = m;
		setup();
	}
	
    public String toString() {
        return String.format(
           "ApplyFunction named '%s'\n with parameters:\n  %s\n  %s := %s(%s) \n\n",
            name,
         inputModule.name, result.name, functionName, funcArgs
         
       );
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasMore() {
        return this.inputModule.hasMore();
    }

    @Override
    public void next() {
        this.inputModule.next();

        // if the filter applies, overwrite the column with the result 
        // of function application
        if (filter.apply()) {
        	result.setValue(this.function.apply(funcArgs, null));
        }
    }
    
}
