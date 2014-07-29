/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import java.util.List;

/**
 *
 * Equivalent to UNIX `uniq` tool. 
 * 
 * TODO Complete the implementation.
 * 
 * @author edoardovacchi
 */
public class MatrixFirst extends MatrixModule {
    private final MatrixModule inputModule;
    private final List<String> fieldNames;
    
    public MatrixFirst(
            String name, 
            String inputTable, 
            String schemaName, 
            List<String> fieldNames) {
        super(name);
        this.inputModule = TheMatrixSys.getModule(inputTable);
        inputModule.schemaMatches(schemaName);
        this.inputModule.addConsumer(this);

        this.fieldNames = fieldNames;
        
    }
    
    @Override
    public void setup() {
        this.setSchema(inputModule.getSchema());
    }
    

	@Override
	public void exec() {
		LogST.logP(2,"NO-OP EXEC "+this.name);
	}

    
    public String toString() {
        return String.format(
           "FirstModule named '%s'\n with parameters:\n  %s\n  %s\n\n",
            name,
         inputModule.name, fieldNames
         
       );
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
