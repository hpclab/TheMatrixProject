/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;

/**
 * enqueue the second input module to the first one, when the first reaches 
 * its end
 * 
 * @author edoardovacchi
 */
public class MatrixUnion extends MatrixModule
{    
	private static final long serialVersionUID = -7019829034272571017L;
	
	private MatrixModule input1;
    private MatrixModule input2;
    
    public MatrixUnion(String name, String input1, String schema1, String input2, String schema2) {
        super(name);
        this.input1 = TheMatrixSys.getModule(input1);
        this.input1.schemaMatches(schema1);
        this.input1.addConsumer(this);
        
        this.input2 = TheMatrixSys.getModule(input2);
        this.input2.schemaMatches(schema2);
        this.input2.addConsumer(this);
    }
    
    public String toString() {
        return String.format(
           "MatrixUnion named '%s'\n with parameters:\n  %s , %s \n\n",
            name,
            input1.name,
            input2.name
             
       );
    }

    @Override
    public void setup() 
    {
    	LogST.logP(2, this.getClass().getSimpleName()+".setup() of "+this.name+" going to union : "
    			+ input1.name+" => "+input1.getSchema().fieldNames()+ " "
    			+ input2.name+" => "+input2.getSchema().fieldNames());
    	
        // check compatibility between schemas
    	// for each symbol in the schema of input1
        for (Symbol<?> s1 : input1.getSchema().attributes()) {
        	// look for the same symbol name, with the same type in input2
        	Symbol<?> s2 = input2.getSchema().get(s1.name);
        	// note: it may fail with MissingSymbolException 
        	if (!s2.isCompatible(s1)) {
        		throw new Error(String.format("Mismatched attribute types: <%s.%s : %s> != <%s.%s : %s>", 
        						input1.name, s1.name, s1.type,
        						input2.name, s2.name, s2.type
        						));
        	}
        	
        }
        
        // the other check
        for (Symbol<?> s2 : input2.getSchema().attributes()) {
        	// look for the same symbol name, with the same type in input2
        	Symbol<?> s1 = input1.getSchema().get(s2.name);
        	// note: it may fail with MissingSymbolException 
        	if (!s2.isCompatible(s1)) {
        		throw new Error(String.format("Mismatched attribute types: <%s.%s : %s> != <%s.%s : %s>", 
        						input1.name, s1.name, s1.type,
        						input2.name, s2.name, s2.type
        						));
        	}
        	
        }
        

        this.setSchema(input1.getSchema());
    }
    
    
	@Override
	public void substituteInput(MatrixModule old, MatrixModule brand_new)
    {
		// search for the correct input and change it
    	if (old.name.equalsIgnoreCase(input1.name))
    		input1 = brand_new;
    	else if (old.name.equalsIgnoreCase(input2.name))
    		input2 = brand_new;
    	
    	// here call a setup to refresh the internal data structures
    	setup();
    }
    
	@Override
	public boolean hasOneInput()
    {
    	return false;
    }


	@Override
	public void exec() {
		LogST.logP(2,"NO-OP EXEC "+this.name);
	}


    @Override
    public void reset() {
    	input1.reset();
    	input2.reset();
	}

    @Override
    public boolean hasMore() {
    	return input1.hasMore() || (!input1.hasMore() && input2.hasMore());
    }

    @Override
    public void next() {
    	if (input1.hasMore()) {
    		input1.next();
    		this.setAll(input1);
    	} else if (input2.hasMore()) {
    		input2.next();
    		this.setAll(input2);
    	} else {
    		throw new Error("end of dataset");
    	}
    }
    
}
