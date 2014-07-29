package it.cnr.isti.thematrix.scripting.filtermodule.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sun.net.httpserver.Filter;

/**
 * provides utility methods to inspect a FilterCondition
 * @author edoardovacchi
 *
 */
public abstract class FilterConditions {
	
	/**
	 * converts a given filter condition to a simple, flat list of conditions
	 * @param fc
	 * @return
	 */
	public static List<SingleFilterCondition> toList(FilterCondition fc) {
		if (fc instanceof FilterSequence) {
			return (List<SingleFilterCondition>) fc;
		} else if (isDiscard(fc)){
			return toList(((DiscardFilterCondition)fc).getFilterCondition());
		} else if (fc instanceof SingleFilterCondition) {
			return Arrays.asList((SingleFilterCondition)fc);
		} else if (fc instanceof DummyFilterCondition) {
			return Collections.emptyList();
		} else {
			throw new IllegalArgumentException(fc.toString());
		}
	}
	
	/**
	 * 
	 * @param fc
	 * @return true if the semantics is discard, false otherwise
	 */
	public static boolean isDiscard(FilterCondition fc) {
		return fc instanceof DiscardFilterCondition;
	}
	
	/**
	 * 
	 * @param fc
	 * @return true if it is a simple condition, or if the condition clause is empty
	 */
	public static boolean isSingle(FilterCondition fc) {
		return fc instanceof SingleFilterCondition || fc instanceof DummyFilterCondition;
	}
	
	/**
	 * 
	 * @param fc
	 * @return if it is a list of conditions connected with AND or if it is just a single condition
	 */
	public static boolean isAnd(FilterCondition fc) {
		return fc instanceof AndFilterSequence || isSingle(fc) || isDiscard(fc) && isAnd((DiscardFilterCondition)fc);
	}
	
	/**
	 * 
	 * @param fc
	 * @return if it is a list of conditions connected with OR or if it is just a single condition	 */
	public static boolean isOr(FilterCondition fc) {
		return fc instanceof OrFilterSequence || isSingle(fc) || isDiscard(fc) && isOr((DiscardFilterCondition)fc);
	}
}
