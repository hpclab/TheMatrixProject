/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.utils;

/**
 *
 * @author edoardovacchi
 */
public class Tuple2<A,B> {
    public final  A _1;
    public final B _2;
    public Tuple2(A a, B b) {
        _1=a; _2=b;
    }
    public String toString() {
        return String.format("(%s,%s)", _1, _2);
    }
}
