/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.sys;

/**
 *
 * @author edoardovacchi
 */
public class MissingOperationException extends RuntimeException {

    public MissingOperationException() {
    }

    public MissingOperationException(String type, String operation) {
        super(type+"::"+operation);
    }
    
}
