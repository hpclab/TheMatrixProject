/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.filtermodule.support;

import it.cnr.isti.thematrix.scripting.sys.MatrixModule;

/**
 * Implements a lazy AND which return false if any filter return false
 * @author edoardovacchi
 */
public class DummyFilterCondition implements FilterCondition{

    public static final DummyFilterCondition INSTANCE = new DummyFilterCondition();
    
    @Override
    public boolean apply() {
        return true;
    }

	@Override
	public void changeInput(MatrixModule m) {}
    
    
}
