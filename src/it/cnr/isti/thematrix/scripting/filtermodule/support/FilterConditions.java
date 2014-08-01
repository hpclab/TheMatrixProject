/*
 * Copyright (c) 2010-2014 "HPCLab at ISTI-CNR"
 *
 * This file is part of TheMatrix.
 *
 * TheMatrix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
