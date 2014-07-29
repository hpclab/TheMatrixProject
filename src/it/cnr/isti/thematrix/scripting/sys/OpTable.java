/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.sys;

import it.cnr.isti.thematrix.scripting.utils.DataType;
import java.util.HashMap;

/**
 * A Map holding {@link Operation} objects.
 * @author edoardovacchi
 */
public class OpTable extends HashMap<String, Operation<?,?,?>> {
    private final char separator = '|';
    
    /**
     * registers an {@link Operation}
     * @param type reference datatype for the operation (it may refer to the return value, 
     *        or to one of its arguments. This is usage-dependent)
     * @param operation  name of the operation
     * @param implObj instance of the operation
     * @return
     */
    public OpTable put(DataType type, String operation, Operation<?,?,?> implObj) {
        super.put(type.toString()+separator+operation, implObj);
        return this;
    }
    
    /**
     * returns the operation associated with the given type, and with the given name
     * @param type the type to which the operation pertains
     * @param operation the name of the operation
     * @return the operation instance
     */
    public <T1,T2,R> Operation<T1,T2,R> get(DataType type, String operation) {
        final String key = type.toString()+separator+operation;
        if (this.containsKey(key)) { return (Operation<T1,T2,R>) this.get(key);}
        else throw new MissingOperationException(type.toString(), operation);
    }
    
    /**
     * invokes immediately the operation that matches the given parameters
     * 
     * @param type
     * @param operation
     * @param o1 first argument 
     * @param o2 second argument 
     * @return
     */
    public Object invoke(DataType type, String operation, Object o1, Object o2) {
        try {
            return this.get(type, operation).apply(o1,o2);
        } catch (ClassCastException e) {
            throw new MissingOperationException(type.toString(), operation);
//            return null;
        }
    }
}
