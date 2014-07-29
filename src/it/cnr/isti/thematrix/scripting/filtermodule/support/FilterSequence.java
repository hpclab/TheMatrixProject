/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.filtermodule.support;

import it.cnr.isti.thematrix.scripting.sys.MatrixModule;

import java.util.ArrayList;

/**
 * a sequence of {@link FilterCondition}
 * @author edoardovacchi
 */
public abstract class FilterSequence extends ArrayList<FilterCondition> implements FilterCondition {
    
    public abstract boolean apply();
	@Override
	public void changeInput(MatrixModule m) {
		for (FilterCondition f: this) f.changeInput(m);
	}
}
