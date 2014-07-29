
package it.cnr.isti.thematrix.scripting.filtermodule.support;

import it.cnr.isti.thematrix.scripting.sys.MatrixModule;

/**
 * inverts the semantics of a filter
 * @author edoardovacchi
 */
public class DiscardFilterCondition implements FilterCondition {

	private FilterCondition fc;
	public DiscardFilterCondition(FilterCondition fc) {
		this.fc = fc;
	}
    @SuppressWarnings(value = "unchecked")
	public boolean apply() {
    	return !fc.apply();
    }
    
    public String toString() {
    	return "DISCARD -- " + fc.toString(); 
    }
	@Override
	public void changeInput(MatrixModule m) {
		fc.changeInput(m);
	}
	public FilterCondition getFilterCondition() {
		return fc;
	}
    
}
