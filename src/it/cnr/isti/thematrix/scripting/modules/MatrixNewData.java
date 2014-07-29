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
import java.util.List;

/**
 * Creates a new dataset with the given schema.
 * TODO  complete the  implementation
 * @author edoardovacchi
 */
public class MatrixNewData extends MatrixModule {
    private final List<Symbol<?>> schema;

    public MatrixNewData(String name, List<Symbol<?>> schema) { 
        super(name); 
        this.schema = schema;
    }
    
    @Override
    public void setup() {
        DatasetSchema customSchema = new DatasetSchema(this.name+"$custom");
        customSchema.putAll(schema);
        this.setSchema(customSchema);
        LogST.logP(1,"MatrixNewData.setup() done. " +this.toString());
    }

	@Override
	public void exec() {
		LogST.logP(2,"NO-OP EXEC "+this.name);
	}

    public String toString() {
        return String.format( 
        "NewDataModule named '%s'\n with schema:\n  %s",
        name,
        schema); 
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
