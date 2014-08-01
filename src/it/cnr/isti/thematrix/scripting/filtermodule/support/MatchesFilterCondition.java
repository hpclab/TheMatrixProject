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

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.AbstractOperation2;

/**
 * code from http://stackoverflow.com/a/3687031/7849 it works, but it is recursive
 * 
 * Fixed to not bomb if passed a null string (but we should trap null fields before here).
 * 
 * @author edoardovacchi
 */
public class MatchesFilterCondition extends AbstractOperation2<String, Boolean> {

	public MatchesFilterCondition() {
		super("matches");
	}

	/**
	 * apply() method for string matching, can receive null as string to evaluate.<br>
	 * To fix behaviour with missing strings in data, we replace any text==null with an empty String, it should not
	 * affect the recursive semantics of other cases.<br>
	 * To avoid useless checks and spurious warnings, the recursive algorithms is moved to private method recApply() .
	 * 
	 * FIXME this prints a tons of messages on files with many null values!!!!!!!
	 * 
	 * @see it.cnr.isti.thematrix.scripting.sys.Operation#apply(java.lang.Object, java.lang.Object)
	 * 
	 * @param text
	 *            string to match (can be empty, but should not be null; null raises a warning message atm)
	 * @param glob
	 *            pattern to match against. Should never be null.
	 */
	@Override
	public Boolean apply(String text, String glob) {
		if (text == null) {
			/**
			 * we deal with null text as the empty sting; as the glob is not allowed to be null, it must contain at
			 * least one char; either a regular one (does not match null) or a meta, that can be a * (can match null) or
			 * ? (cannot match null).
			 */
			if (glob.equals("*")) { return true; }
			else { return false; }
//		LogST.logP(1, "WARNING MatchesFilter.apply() on a null string, missing field in record?");
//			text = ""; // avoid breaking at missing strings
		}
		return recApply(text, glob);// does the real work
	}

	/**
	 * The true implementation of apply() as recursive function. <br>
	 * Same parameters meaning, but they should never be null.
	 * 
	 * FIXME not sure this really works as intended, and undocumented: document and do unit testing 
	 * 
	 * @see it.cnr.isti.thematrix.filtermodule.support.MatchesFilterCondition.apply(java.lang.String, java.lang.String)
	 * @return
	 */
	private Boolean recApply(String text, String glob) {
		String rest = null;
		int pos = glob.indexOf('*');
		if (pos != -1) {
			rest = glob.substring(pos + 1);
			glob = glob.substring(0, pos);
		}

		if (glob.length() > text.length()) { return false; }

		// handle the part up to the first *
		for (int i = 0; i < glob.length(); i++) {
			if (glob.charAt(i) != '?' && !glob.substring(i, i + 1).equalsIgnoreCase(text.substring(i, i + 1))) { return false; }
		}

		// recurse for the part after the first *, if any
		if (rest == null) {
			return glob.length() == text.length();
		}
		else {
			for (int i = glob.length(); i <= text.length(); i++) {
				if (recApply(text.substring(i), rest)) { return true; }
			}
			return false;
		}
	}

}
