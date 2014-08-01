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
import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import java.util.List;

/**
 *
 * @author edoardovacchi
 */
public class MatrixExtendData extends MatrixModule 
{
	private static final long serialVersionUID = 7536414593882819426L;

	private final List<Symbol<?>> newAttributes;
    private MatrixModule inputModule;
    
    public MatrixExtendData(String name, String inputModule, String schema, List<Symbol<?>> newAttributes) 
    {
        super(name);
        this.inputModule = TheMatrixSys.getModule(inputModule);
        this.inputModule.schemaMatches(schema);
        this.inputModule.addConsumer(this);
        this.newAttributes = newAttributes;
   		LogST.logP(2, "ExtendDataModule() :  module "+this.name+" added "+newAttributes.toString());
    }
    
    public String toString() {
        return String.format(
           "ExtendDataModule named '%s'\n with parameters:\n  %s\n  %s \n\n",
            name,
         inputModule.name, newAttributes
         
       );
    }

    @Override
    public void setup() 
    {
    	String errorCheck  = DatasetSchema.canExtend(inputModule.getSchema(), newAttributes);
    	if (errorCheck != null)
    	{
    		LogST.logP(0, "ExtendDataModule.setup() : ERROR in module "+this.name+"\nError is in "+errorCheck);
    		throw new Error("ExtendData module ("+this.name+") cannot extend the the schema "+inputModule.getSchema().name+" with the attributes "+newAttributes.toString());
    	}
        DatasetSchema extendedSchema = DatasetSchema.extend(inputModule.getSchema(), newAttributes, this.name); 
        
        this.setSchema(extendedSchema);
        //        System.err.println("attributes are : "+attributes());
        
        // instead of copying, we reference the source symbols
        // NOTE: the current solution does not work if a sort module is just before the extend data.
        // ema, 19.03.2014: changing the implementation so that this computation is actually done in the next();
        // 					this may be slower but probably safer.
        
        /* NOTE: old implementation left as reference
        List<Symbol<?>> inputAttributes = inputModule.attributes();
        // List<Symbol<?>> destAttributes  = this.attributes(); // NOT USED
        int nAttrs = inputAttributes.size();
        for (int i = 0; i < nAttrs; i++) {
            // re-link this field to inputModule's corresponding reference
            this.put(inputAttributes.get(i).name, inputAttributes.get(i));
        }
   		LogST.logP(1, "ExtendDataModule.setup() :  module "+this.name+" added "+newAttributes.toString()+
   				"\nproducing schema "+this.getSchema());
   	    */   
    }
    

	@Override
	public void exec() 
	{
		LogST.logP(2,"NO-OP EXEC "+this.name);
	}

	@Override
	public void changeInput (MatrixModule m)
    {		
		this.inputModule.schemaMatches(m.getSchema().name);
		this.inputModule = m;
		this.inputModule.addConsumer(this);
    }
	
    @Override
    public void reset() {
        inputModule.reset();
        // reset this
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasMore() {
        return inputModule.hasMore();
    }

    @Override
    public void next() 
    {
        this.inputModule.next();
//        LogST.logP(0,"ExtendDataModule.next() input row is "+this.inputModule.attributes().toString());
    	
        // first copy values from the original module
        for (Symbol<?> s : inputModule.attributes())
        	this.set(s.name, s.value);
              
        // add the new attributes values
        for (Symbol<?> s : newAttributes) {
            this.set(s.name, s.value);
        }
    	
//        LogST.logP(0,"ExtendDataModule.next() after extension, current row is "+this.attributes().toString());
    }
    
}
