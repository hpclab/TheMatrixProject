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
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import it.cnr.isti.thematrix.scripting.utils.DateUtil;

import java.util.List;

/**
 *
 * Declares the formal parameters of a script and loads them.
 * 
 * ParametersModule should occur only *once*, as the very first module of a script.
 * A ParametersModule defines the types and names of a script as follows:
 *
 * <code>
 * MyModuleName (ParametersModule)
 * parameters
 *   params = [ { SOMEINT : int }; { SOMESTRING : string }, … ]
 * end
 * </code>
 * 
 * Afterwards, parameters can be referenced within the rest of the script using 
 * $PARAM_NAME. For instance $SOMEINT references the first value
 * 
 * Values can be associated to the parameters from the CLI using 
 * {@link TheMatrixSys#setArguments(List)} or directly, as a list of 
 * {@link Symbol} objects using {@link TheMatrixSys#setParamList(List)}
 *
 *
 * This module should not be referenced by any other module. Trying to
 * iterate over it will result in an exception (since it does not really represent a dataset)
 * 
 * All the work is done in the {@link #setup()} procedure.
 * 
 *
 * @author edoardovacchi
 */
@SuppressWarnings("serial")
public class MatrixParameters extends MatrixModule {
	/**
	 * the list of symbol definitions passed in by the interpreter
	 */
    private final List<Symbol<?>> declaredParameterList;

    public MatrixParameters(String name, List<Symbol<?>> parameterList) { 
        super(name); 
        this.declaredParameterList = parameterList;
        LogST.logP(-1, "ParametersModule created with "+declaredParameterList.size()+" parameter(s), content is"+declaredParameterList.toString());
    }
    
    /**
     * This procedure tries to match the formal parameters in {@link declaredParameterList}
     * with either the list of {@link Symbol} instances in {@link TheMatrixSys#getParamList()}
     * or the String list in {@link TheMatrixSys#getArgumentList()}.
     * 
     * FIXME  The procedure first tries to iterate over the symbol
     * 
     */
    @Override
    public void setup() {
 

        /*
         * gets the list of parameters that were given as input to the script
         */
        List<Symbol<?>> inputParamList = TheMatrixSys.getParamList();

        
        /*
         * if the symbol list is not empty, then tries to match against {@link #declaredParameterList}
         */
        if (!inputParamList.isEmpty()) {
        	
        	/*
        	 * setups the global params: prepares the declared parameters 
        	 * by setting them in the global interpreter namespace
        	 * 
        	 */
            
            if (declaredParameterList.size()!=inputParamList.size()) 
                throw new IllegalArgumentException("Wrong number of arguments given, "+declaredParameterList.size()+" expected, got "+inputParamList.size()+" instead.");
            
            int nParams = declaredParameterList.size();
            
            for (int i = 0; i < nParams; i++) 
            {
            	Symbol<?> dest = declaredParameterList.get(i);
            	System.out.println(dest);
            	Symbol<?> src = inputParamList.get(i);
            	System.out.println(src);
            	
            	if (dest.type != src.type) 
            		throw new IllegalArgumentException(String.format(
            				"type mismatch for param %s; value '%s' given", dest.name, src.value));
            	
                dest.setValue(src.value);
            }
            return;
            
        } else {
        	/*
        	 * otherwise, tries to parse the strings given in {@link TheMatrixSys#getArgumentList()}
        	 * (actual parameters) according to the types of {@link declaredParameterList} (formal parameters). 
        	 * 
        	 * If the actual and formal number does not match, or if the values cannot be parsed correctly
        	 * a (subclass of) RunTimeException will be thrown.
        	 */
        	TheMatrixSys.setParamList(declaredParameterList);
 
        	inputParamList = declaredParameterList;
        	List<String> actualParamList = TheMatrixSys.getArgumentList();

			if (declaredParameterList.size() != actualParamList.size())
				throw new IllegalArgumentException(String.format(
						"Script parameter mismatch : the script expected %s parameters, received %s",
						declaredParameterList.size(),
						actualParamList.size()));

        	for (Symbol<?> s : declaredParameterList) {
        		try {
        			String arg = actualParamList.remove(0);
        			switch (s.type) {
        			case INT:
        				s.setValue(Integer.parseInt(arg));
        				break;
        			case FLOAT:
        				s.setValue(Float.parseFloat(arg));
        				break;
        			case BOOLEAN:
        				s.setValue(Boolean.parseBoolean(arg));
        				break;
        			case STRING:
        				s.setValue(arg);
        				break;
        			case DATE:
        				s.setValue(DateUtil.parse(arg));
        				break;
        			default:
        				throw new IllegalArgumentException(); //RECORD and MISSING are no valid parameter values
        			}
        		} catch (RuntimeException ex) {
					throw new IllegalArgumentException(String.format(
							"Script parameter type mismatch for parameter %s: incompatible value '%s was given\t\t\n",
							s.name, s), ex);
        		}
        	}
        }
        LogST.logP(1,"MatrixParameters.setup() done. " +this.toString());
    }
    

	@Override
	public void exec() {
		LogST.logP(2,"NO-OP EXEC "+this.name);
	}

    
    public String toString() {
        return String.format( 
        "ParametersModule named '%s'\n with parameters:\n  %s",
        name,
        declaredParameterList); 
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasMore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void next() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
