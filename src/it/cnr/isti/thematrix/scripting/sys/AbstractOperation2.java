/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.sys;

/**
 * A default implementation for an abstract operation 
 * where the two types T1 and T2 match (that is, T1==T2)
 * 
 * @author edoardovacchi
 */
public abstract class AbstractOperation2<T,R> extends AbstractOperation<T,T,R> {
    public AbstractOperation2(String id) { super(id); }
}
