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

import it.cnr.isti.thematrix.scripting.utils.DataType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * A SymbolTable is a {@link java.util.Map} from String to {@link Symbol}, where
 * the String key is always the value {@link Symbol#getName()} in the given symbol
 * 
 * This implementation <em>does not</em> return null when a key is missing, but
 * it throws a {@link MissingSymbolException}
 * 
 * @author edoardovacchi
 */
public class SymbolTable extends HashMap<String, Symbol<?>> {
    
	/**
	 * puts each symbol in the given collection,
	 * overwriting any existing symbol with the same name
	 * 
	 * @param ss
	 */
    public void putAll(Collection<? extends Symbol<?>> ss) {
        for (Symbol<?> s : ss) this.put(s.name, s);
    }
   

    /**
     * returns the symbol for the given symbolName
     * @param symbolName
     * @return the corresponding {@link Symbol}, 
     *         or a {@link MissingSymbolException} if the given symbol is missing
     */
    @Override
    public Symbol<?> get(Object symbolName) {
        if (this.containsKey(symbolName)) {
            return super.get(symbolName);
        }
                
        throw new MissingSymbolException("SymbolTable.get() MissingSymbolException " +
        		"for "+symbolName.toString()+ " -- List of keys: "+this.keySet());
    }
    
    /**
     * Sets value to the given symbol in a type-safe manner
     * @param <T> type of the new value
     * @param symbolName name of the symbol you are updating
     * @param value new value for symbolName
     * @return the same instance of SymbolTable, for method chaining, 
     *          i.e.: <pre>
     *                myTable.set("foo", 10)
     *                       .set("bar", true)
     *                       .set("baz", "a String");</pre>
     */
    public <T> SymbolTable set(String symbolName, T value) {
        if (this.containsKey(symbolName)) {
           Symbol<T> s = (Symbol<T>)this.get(symbolName);
           // assertions will be enabled only during development
           //assert (value!=null && s.value!=null && value.getClass() == s.value.getClass()); ///throw new ClassCastException();
           assert(s.isCompatible(value));
           s.setValue(value);
           return this;
        }
        
        throw new MissingSymbolException("SymbolTable.set() MissingSymbolException " +
        		"for "+symbolName.toString()+ " -- List of keys: "+this.keySet());
    }
    
    /**
     * check whether s1 is compatible with s2, type-wise
     * 
     * @param s1 a symbolName
     * @param s2 another symbolName
     * @return true if the symbol datatypes are compatible, false otherwise
     */
    public boolean isCompatible(String s1, String s2) {
        return get(s1).type==get(s2).type;
    }
    
    

}
