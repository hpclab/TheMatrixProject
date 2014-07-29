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
 *
 * @author edoardovacchi
 */
public abstract class AbstractSymbolListOperation<R> implements Operation<List<Symbol<?>>,Void,R> {
    private final String name;
    public AbstractSymbolListOperation(String name) {
        this.name=name;
    }
    @Override
    public String toString() {
        return name;
    }
}
