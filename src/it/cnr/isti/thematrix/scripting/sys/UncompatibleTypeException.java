/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.sys;

/**
 *
 * @author edoardovacchi
 */
public class UncompatibleTypeException extends RuntimeException {
    public UncompatibleTypeException(Symbol<?> s1, Symbol<?> s2) {
        super(String.format("Symbol <%s> has type <%s>, while Symbol <%s> has type <%s>",
                s1.name, s1.type,
                s2.name, s2.type));
    }
}
