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
package it.cnr.isti.thematrix.scripting.sys;

import java.util.HashMap;

/**
 *
 * A simple Map from module name to {@link MatrixModule}. 
 * Throws a runtime exception when a requested module does not exist in the map
 * 
 * @author edoardovacchi
 */
@SuppressWarnings("serial")
public class ModuleTable extends HashMap<String,MatrixModule>{
	
	/**
	 * @param the module name
	 * @return the module for the given name
	 * @throws MissingModuleException
	 */
     public MatrixModule get(Object o) {
         if (this.containsKey(o)) return super.get(o);
         throw new MissingModuleException("Missing module: "+o);
     }
     
	/**
	 * (non-Javadoc) Add a module to the lookup table for this interpreter. No checks here that the module already
	 * exists, as there are cases when we want to replace a module. Actually there is no need to override any longer.
	 * 
	 * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public MatrixModule put(String s, MatrixModule m) {
		//DEBUG    	 LogST.logP(-1,"TheMatrix string / module name "+s+" "+m.name+" is in table? containsKey returns "+this.containsKey(s));
		//DEBUG    	 Thread.dumpStack();

		return super.put(s, m);
		/**
		MatrixModule m1 = super.put(s, m);
		if (m1 == null) return m1;
		throw new Error("TheMatrix module name conflict: module " + m.name + " already exists");
		**/
//		LogST.logP(-1, "TheMatrix module name conflict: module " + m.name + " with definition\n" + m.toString()
//				+ "\n already exists as: \n" + m1.toString());
//
//		return m1;

	}
}
