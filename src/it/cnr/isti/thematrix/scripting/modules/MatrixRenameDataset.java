/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import java.util.List;

/**
 *
 * @author edoardovacchi
 */
public class MatrixRenameDataset extends MatrixModule {
    

    private final MatrixModule inputModule;
    public MatrixRenameDataset(String name, String inputModule, String schema) {
        super(name);
        this.inputModule = TheMatrixSys.getModule(inputModule);
        this.inputModule.schemaMatches(schema);
        this.inputModule.addConsumer(this);
    }
    
    public String toString() {
        return String.format(
           "MatrixRenameDataset named '%s'\n with parameters:\n  %s \n\n",
            name,
         inputModule.name
         
       );
    }

    @Override
    public void setup() {
        
        this.setSchema(inputModule.getSchema());
    }
    

	@Override
	public void exec() {
		inputModule.exec(); // FIXME seems like a bug where exec() is called twice
	}


    @Override
    public void reset() {
        inputModule.reset();
    }

    @Override
    public boolean hasMore() {
        return inputModule.hasMore();
    }

    @Override
    public void next() {
        this.inputModule.next();
    }
    
    @Override
    public Symbol<?> get(Object o) {
    	return this.inputModule.get(o);
    }
    
    @Override
    public List<Symbol<?>> attributes() {
    	return this.inputModule.attributes();
    }
    
}
