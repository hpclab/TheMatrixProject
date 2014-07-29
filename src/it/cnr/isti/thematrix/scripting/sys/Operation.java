package it.cnr.isti.thematrix.scripting.sys;

/**
 * An Operation is a binary function that given 
 * two values of type T1 and T2 returns a third value of type R.
 * 
 * @author edoardovacchi
 */
public interface Operation<T1,T2,R> {
    public R apply(T1 op1, T2 op2);
}
