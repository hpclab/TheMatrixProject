/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dexter.HashSet;

/**
 *
 * @author edoardovacchi
 */
public class MatrixRenameAttributes extends MatrixModule {

    private final MatrixModule inputModule;
    private final List<String> inAttr;
    private final List<String> outAttr;
    

    private int nAttr;
    
    public MatrixRenameAttributes(String name, 
            String inputModule, String sourceSchema, List<String> inputAttributes,
            List<String> resultAttributes) {
        super(name);
        this.inputModule = TheMatrixSys.getModule(inputModule);
        this.inputModule.schemaMatches(sourceSchema);
        this.inputModule.addConsumer(this);
        
        this.inAttr = inputAttributes;
        this.outAttr = resultAttributes;
        
    }
    
    @Override
    public void setup() {
    	
        if (inAttr.size() != outAttr.size()) {
            throw new RuntimeException("Mismatched attribute lists in module "+this.name);
        }
        
        nAttr = inAttr.size();
    	
    	DatasetSchema customSchema = new DatasetSchema(this.name+"$custom");
    	Set<String> nonRenamedAttrs = new HashSet<String>();
    	
        for (Symbol<?> attr : this.inputModule.getSchema().attributes()) {
        	int idx = this.inAttr.indexOf(attr.name);
        	// if found in our target list
        	if (idx >= 0) {
        		// it must exist in our output list
        		String newName = this.outAttr.get(idx);
        		Symbol<?> newAttribute = attr.clone();
        		newAttribute.name = newName;
        		customSchema.put(newAttribute.name, newAttribute);
        	} else {
        		nonRenamedAttrs.add(attr.name);
        		customSchema.put(attr.name, attr);
        	}
        }
    	
    	this.setSchema(customSchema);
        
    	// now that the schema is known, link the fields of the input source
    	// to the fields of this new dataset
    	int schemaSize = customSchema.size();
    	List<Symbol<?>> customSchemaAttrs = customSchema.attributes();
    	// actual tuple of values (not a schema!)
    	List<Symbol<?>> inputAttrs = this.inputModule.attributes();
    	for (int i = 0; i < schemaSize; i++) {
    		Symbol<?> attr = customSchemaAttrs.get(i); 
    		if (nonRenamedAttrs.contains(attr.name)) {
    			// link actual tuple reference in target to this
    			// in this case the name matches (name is in the set of non-renamed attributes)
    			this.put(attr.name, this.inputModule.get(attr.name));
    		} /* else {
    			// attribute has been renamed
    		}*/
    	}
    	
        
    	 LogST.logP(1,"MatrixRenameAttributes.setup() done. " +this.toString());
    }
    

	@Override
	public void exec() {
		LogST.logP(2,"EXEC "+this.name);
	}

    
    public String toString() {
        return String.format(
           "CopyAttributeModule named '%s'\n with parameters:\n  %s \n  %s %s\n\n",
            name,
         inputModule.name, inAttr, outAttr
         
       );
    }

    @Override
    public void reset() {
        this.inputModule.reset();
        // reset_myself()
    }
    
    @Override
    public boolean hasMore() {
    	return inputModule.hasMore();
    }

    @Override
    public void next() {
        // advance inputModule internal pointer
        // (implicitly updates referenced attributes)
        this.inputModule.next(); 
        // update internal pointers
        
        for (int i = 0; i<nAttr; i++) {
        	String in = inAttr.get(i);
        	String out = outAttr.get(i);
            Symbol<?> ref = this.inputModule.get(in);
            this.set(out, ref.value);
        }

    }
    
}
