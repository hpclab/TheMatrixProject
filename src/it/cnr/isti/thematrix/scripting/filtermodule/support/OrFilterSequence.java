/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.filtermodule.support;

/**
 * Implements a short-circuited OR, which returns true if at least one condition is true
 * @author edoardovacchi
 */
public class OrFilterSequence extends FilterSequence {

    @Override
    public boolean apply() {
        for (FilterCondition fc : this) {
            if (fc.apply()) return true;
        }
        return false;
    }
    
}
