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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author edoardovacchi
 */
public class MatrixCopyAttribute extends MatrixModule {
    private final MatrixModule resultModule;
    private final MatrixModule inputModule;
    private final List<String> inputAttributes;
    private final List<String> resultAttributes;
    
    private final List<Symbol<?>> updatableAttributes = new ArrayList<Symbol<?>>();
    
    public MatrixCopyAttribute(String name, 
            String inputModule, String sourceSchema, String resultModule, String resultSchema, 
            List<String> inputAttributes,
            List<String> resultAttributes) {
        super(name);
        this.inputModule = TheMatrixSys.getModule(inputModule);
        this.inputModule.schemaMatches(sourceSchema);
        this.inputModule.addConsumer(this);

        this.resultModule = TheMatrixSys.getModule(resultModule);
        this.resultModule.schemaMatches(resultSchema);
        this.resultModule.addConsumer(this);
        
        this.inputAttributes = inputAttributes;
        this.resultAttributes = resultAttributes;
    }
    
    @Override
    public void setup() {
        this.setSchema(resultModule.getSchema());
        
//        System.err.println("attributes are : "+attributes());
        
        if (inputAttributes.size() != resultAttributes.size()) {
            throw new RuntimeException("Mismatched attribute lists in module "+this.name);
        }
        
        
        // instead of copying, we reference the source symbols
        int nAttrs = inputAttributes.size();
        for (int i = 0; i < nAttrs; i++) {
            // re-link this field to inputModule's corresponding reference
            String inputAttrName = inputAttributes.get(i);
            String resultAttrName = resultAttributes.get(i);

            LogST.logP(3,inputModule.attributes().toString());
            Symbol<?> inputAttr = inputModule.get(inputAttrName);
            this.put(resultAttrName, inputAttr);
        }
        
        // fill a list of attributes that must be filled by next()
        // the others don't need explicit updating, we will just be calling inputModule.next()
        for (Symbol<?> s : attributes()) {
            if (! inputAttributes.contains(s.name) ) updatableAttributes.add(s);
        }        
        
//        System.err.println("attributes are now: "+attributes());
        LogST.logP(1,"MatrixCopyAttribute.setup() done. " +this.toString());
    }
    

	@Override
	public void exec() {
		LogST.logP(2,"NO-OP EXEC "+this.name);
	}

    
    public String toString() {
        return String.format(
           "CopyAttributeModule named '%s'\n with parameters:\n  %s %s\n  %s %s\n\n",
            name,
         inputModule.name, inputAttributes,
         resultModule.name, resultAttributes
         
       );
    }

    @Override
    public void reset() {
        this.inputModule.reset();
        // reset_myself()
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasMore() {
        // this.inputModule.hasMore() && myself_hasMore()
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void next() {
        // advance inputModule internal pointer
        // (implicitly updates referenced attributes)
        this.inputModule.next(); 
        // update internal pointers
        for (Symbol<?> s : updatableAttributes) {
            // *do* update pointers
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
