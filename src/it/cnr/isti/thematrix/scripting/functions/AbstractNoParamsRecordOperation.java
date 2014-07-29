/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.functions;

import it.cnr.isti.thematrix.scripting.sys.DatasetRecord;
import it.cnr.isti.thematrix.scripting.sys.Operation;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import java.util.List;

/**
 * Represents an operation on a list of arbitrary length of parameters that returns 
 * a value of type R
 * 
 * @author edoardovacchi
 */
public abstract class AbstractNoParamsRecordOperation<R> implements Operation<List<Symbol<?>>,Void,R> {
    protected final String id;

    public AbstractNoParamsRecordOperation(String id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return id;
    }

}
