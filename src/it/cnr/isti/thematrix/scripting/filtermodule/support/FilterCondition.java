/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.filtermodule.support;

import it.cnr.isti.thematrix.scripting.sys.MatrixModule;

/**
 *
 * Represents a boolean condition. Assumes that the arguments are either
 * from one single module or they are parameters.
 * @author edoardovacchi
 */
public interface FilterCondition {

	/**
	 * 
	 * @return true IFF the condition holds
	 */
    @SuppressWarnings(value = "unchecked")
    boolean apply();
    
    /**
     * changes the symbols in this filter to reference the given module
     * assumes that the filter applies to only one dataset at a time;
     * that is, the filter cannot predicate about more than one dataset at once
     * 
     * e.g. MyDatasetX.Value1 > MyDatasetY.ValueB is UNSUPPORTED
     * 
     * @param m a new module with the same schema of the inputs
     */
    public void changeInput(MatrixModule m);
}
