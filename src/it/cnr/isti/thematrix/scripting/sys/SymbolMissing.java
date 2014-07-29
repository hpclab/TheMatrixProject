/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.sys;

import it.cnr.isti.thematrix.scripting.utils.DataType;

/**
 *
 * @author edoardovacchi
 */
public class SymbolMissing extends Symbol<Void> {
    public static final SymbolMissing MISSING = new SymbolMissing(); 
    private SymbolMissing() {
         super("MISSING", null, DataType.MISSING);
    }
}
