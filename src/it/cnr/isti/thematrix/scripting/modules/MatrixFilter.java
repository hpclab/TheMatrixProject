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
import it.cnr.isti.thematrix.scripting.filtermodule.support.AndFilterSequence;
import it.cnr.isti.thematrix.scripting.filtermodule.support.DiscardFilterCondition;
import it.cnr.isti.thematrix.scripting.filtermodule.support.FilterCondition;
import it.cnr.isti.thematrix.scripting.filtermodule.support.OrFilterSequence;
import it.cnr.isti.thematrix.scripting.filtermodule.support.SingleFilterCondition;
import it.cnr.isti.thematrix.scripting.sys.DatasetRecord;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Operation;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;

/**
 * Because this module requires a complex set of parameters, it is processed
 * in a particular way. The immediate consequence is that the {@link #setup()} method,
 * unusually, is not the one responsible for the most of the setup phase.
 * 
 * A Filter is an object of type {@link FilterCondition}. Each filter is a predicate
 * on an attribute of the module. For instance
 *  
 *     <code> FOO > 10 </code>
 * 
 * where `FOO` is an integer column in this module. Every predicate is some kind
 * of comparison operator; for a list, see the Matrix syntax manual (script_syntax_new.rtf)
 * 
 * Because at the time of loading `FOO` does not point to a meaningful value, 
 * and because the filter must be re-evaluated for each row, this module is initialized 
 * as follow:
 * 
 * <ol>
 * 
 * <li> the interpreter instantiates the object, 
 * 
 * <li> it then invokes {@link #addInput(String, String)} with the inputModule parameters.
 * 
 * <li> finally, it invokes {@link TheMatrixSys#setCurrentModule(MatrixModule)} with the
 * inputModule instance. This way, the interpreter can retrieve the input module with 
 * {@link TheMatrixSys#getCurrentModule()} to retrieve the references
 * 
 * <li> for each comparison operator found in the `parameters` section an object of type
 * {@link SingleFilterCondition} is created, with a boolean {@link Operation}: the left-hand 
 * operand will be the {@link Symbol} corresponding to the left member of the comparison 
 * (in our example, `FOO`), the right-hand operand is a {@link Symbol} corresponding to a literal
 * (in our example, 10) or to a parameter {@link MatrixParameters} (e.g., $SOME_PARAM).
 * 
 * <li> the list of predicates is linked together using a connective {@link OrFilterSequence} or 
 * {@link AndFilterSequence}, which decorates the list of predicates
 * 
 * <li> Moreover, if the semantics of the filter is to DISCARD the matching records, a 
 *    {@link DiscardFilterCondition} wraps the chain inverts the meaning of the list of predicates
 *    (it is effectively a boolean NOT)
 *    
 * <li> Finally, the interpreter invokes {@link #setFilters(FilterCondition)} with the given 
 *     list of predicates
 *     
 * </ol>
 * 
 * 
 * <strong>Execution model.</strong>
 * When the filter is queried with {@link #hasMore()}, the {@link FilterCondition#apply()} method 
 * is invoked, when the method returns true, the tuple is kept, otherwise, it is discarded:
 * in former case, the module sets its internal pointers to the values of the input module; 
 * otherwise it advances the pointer of the input module until a matching tuple is found
 * 
 * 
 * 
 * <strong>Caveats.</strong> Because the internal references to the target are setup
 * <em>while evaluating the input script</em> and internal post-processing may require to
 * fiddle with these internal pointers, this module is quite brittle and requires extra-care.
 * 
 * It might be useful in future re-implementation to re-iterate over the given {@link FilterCondition}
 * and pull-out the name of the operands, re-instantiating predicates over the actual inputs.
 * However, current implementation does provide a way to change the input to an arbitrary module.
 *  
 * @author edoardovacchi, carlini
 */

public class MatrixFilter extends MatrixModule 
{
	private static final long serialVersionUID = -8503883507211280029L;
	
	private String inputModuleName;
	private MatrixModule inputModule;
	private FilterCondition filters;
	
	// counters
	private long totalRows = 0;
	private long discardedRows = 0;
	
	
	private DatasetRecord buffer;
	private boolean inited = false;
	private boolean isValidBuffer = false;
	private boolean hasNext = false;
	private boolean computed = false;

	public static enum Type 
	{
		KEEP, DISCARD;
	}
	
	public MatrixFilter(String name) 
	{
		super(name);
	}

	public void setFilters(FilterCondition filters) 
	{
		this.filters = filters;
	}

	/**
	 * fills the {@link #inputModule} field with the 
	 * object instance that matches the given name and schema.
	 * Moreover, it sets the schema for this module. Finally,
	 * 
	 * 
	 * 
	 * @param inputModuleName input module name 
	 * @param inputSchema schema name for the given module
	 */
	public void addInput(String inputModuleName, String inputSchema)
	{
		this.inputModule = TheMatrixSys.getModule(inputModuleName);
		inputModule.schemaMatches(inputSchema);
		this.setSchema(this.inputModule.getSchema());
		this.inputModule.addConsumer(this);

		TheMatrixSys.setCurrentModule(inputModule);
	}

	@Override
	public void setup() 
	{
		buffer = DatasetRecord.emptyRecord(this);
	}

	@Override
	public void exec() 
	{
		LogST.logP(2,"NO-OP EXEC "+this.name);
	}

	@Override
	public void reset() 
	{
		throw new UnsupportedOperationException("Not supported yet.");
		/*
		if (!inited) {
			inited = true;
			LogST.logP(0, "MatrixFilter.reset() initializing module ");
			prepareNextRow();
		}
		*/
	}

	/**
	 * Prepares the next row.
	 * @return true if the line in the buffer is valid (false means that the inputModule has no more lines).
	 */
	private boolean prepareNextRow() 
	{
		isValidBuffer = false;
		
		while (inputModule.hasMore() && isValidBuffer == false) 
		{
			inputModule.next(); 
			totalRows++;

			// NOTE: the filter is called on the inputModule
			if (filters.apply()) 
			{
				isValidBuffer = true;
				buffer.setAll(inputModule);
			}
			else
				discardedRows++; // row did not pass the test
		}
		
		// the same than returning isValidBuffer
		if (isValidBuffer)
			return true;
		else 
			return false;
	}

	/**
	 * It is safe to be called multiple times.
	 */
	@Override
	public boolean hasMore() 
	{
		if (inited == false)
			inited = true;
		
		if (computed == false)
		{			
			computed = true;
			hasNext = prepareNextRow();
			return hasNext;
		}
		else
		{
			return hasNext;
		}
	}

	/**
	 * Returns the content in the buffer.
	 * Raises error if it is called before the hasMore() or twice in a row,
	 */
	@Override
	public void next() 
	{		
		if (computed == false)
			throw new Error(this.getClass().getSimpleName()+": Called next() before hasMore() on "+this.name);
		else
		{
			this.setAll(buffer);
			computed = false;
		}
	}

	@Override
	public void changeInput(MatrixModule m) 
	{
		if (inited) 
			throw new Error("MatrixFilter.changeInput() not allowed after reset() " + this.name);
		LogST.logP(1, "MatrixFilter.changeInput() from "+inputModuleName+" to "+m.name);
		inputModule = m;
		inputModuleName = m.name;
		filters.changeInput(inputModule);

	}

	@Override
	public String toString() 
	{
		return String.format("FilterModule named '%s'\nwith input\n  %s\nand parameters\n  %s\n\n", name,
				inputModule.name, filters);
	}
}
