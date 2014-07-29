/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.filtermodule.support;

/**
 * Implements a short-circuited AND which return false if any filter return false
 * @author edoardovacchi
 */
public class AndFilterSequence extends FilterSequence{

    @Override
    public boolean apply() {
        for (FilterCondition f : this) {
            if (!f.apply()) return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "AND::"+super.toString();
    }
    
}
