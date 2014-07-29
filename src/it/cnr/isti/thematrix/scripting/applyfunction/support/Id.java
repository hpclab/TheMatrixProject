/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.applyfunction.support;

import it.cnr.isti.thematrix.scripting.functions.AbstractSymbolListOperation;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import java.util.List;

/**
 *
 * @author edoardovacchi
 */
public class Id extends AbstractSymbolListOperation<Integer> {

    public Id() { super("int::id"); }
    @Override
    public Integer apply(List<Symbol<?>> op1, Void op2) {
        Symbol<Integer> arg = (Symbol<Integer>) op1.get(0);
        return arg.value;
    }
    
}
