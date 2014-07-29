/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.sys;

/**
 * A default implementation for the {@link Operation} interface,
 * with a nicer {@link #toString()} method
 * 
 * @author edoardovacchi
 */
public abstract class AbstractOperation<T1,T2,R> implements Operation<T1,T2,R> {
    protected final String id;
    public AbstractOperation(String id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return id;
    }
}
