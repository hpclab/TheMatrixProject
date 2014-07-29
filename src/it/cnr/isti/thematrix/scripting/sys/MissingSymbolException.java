/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.sys;

/**
 *
 * @author edoardovacchi
 */
public class MissingSymbolException extends RuntimeException {

    public MissingSymbolException(String attributeName) {
        super(attributeName);
    }
    
}
