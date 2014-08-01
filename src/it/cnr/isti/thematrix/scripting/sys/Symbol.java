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

/**
 *
 * A Symbol represents an attribute of a dataset or a variable of the script.
 * A Symbol has a name, a type and a value of the given type.
 * 
 * It is a simple JavaBean for the fields value, name and type.
 * Types are represented by values of the enum type {@link DataType}
 *
 * @author edoardovacchi
 */
public class Symbol<T> {

    public T getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = (T) value;
    }
    
    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public T value;
    public DataType type = null;
    
    
    public Symbol(DataType t) {
        type=t;
    }
    
    public Symbol(T value, DataType t) {
        this(t);
        this.value = value;
    }
    
    
    public Symbol(String name) {
        this.name = name;
    }
    
    public Symbol(String name, DataType t) {
        this(name);
        setType(t);
    }
    
    
    public Symbol(String name, T value, DataType t) {
        this(name, t);
        setValue(value);
    }
    
    @Override 
    public String toString() {
        return String.format("<%s : %s = %s>", name, type, value);
    }

    private void setType(DataType t) {
        this.type = t;
    }
    
    public boolean isCompatible(Symbol<?> s) {
        return this.type == s.type ||  s.type == DataType.MISSING || this.type == DataType.MISSING;
    }
    
    /**
     * 
     * @param value an scalar value or a Symbol instance holding that value
     * @return true when the given value is type-wise compatible
     */
    public boolean isCompatible(Object value) {    	
    	if (value == null) return true;
        Class<?> clazz ;
    	if (value instanceof Symbol) {
    		Object val = ((Symbol<?>) value).value;
    		if (val == null) return true;
    		clazz = val.getClass();
    	} else {
    		clazz = value.getClass();
    	}
        switch(this.type) {
            case INT:
                return clazz==java.lang.Integer.class;
            case FLOAT:
                return clazz==java.lang.Float.class;
            case BOOLEAN:
                return clazz==java.lang.Boolean.class;
            case STRING:
                return clazz==java.lang.String.class;
            case DATE:
                return clazz==java.util.Date.class;
            default:
                return false;
                
        }
    }
    
    /**
     * @return a new instance of the symbol with the same value, type, and name
     * of this
     */
    public Symbol<T> clone() {
        return new Symbol<T>(this.name, this.value, this.type);
    }
    
    /**
	 * Method returning the expected size in the output file (as a String) of a
	 * given symbol. method defines default values; size does not include
	 * commas.
	 * 
	 * TODO maybe refactor as array access using the enum? Should support avg
	 * and max size, and max input size
	 * 
	 * TODO check these values
	 * 
	 * @return
	 */
    public int expectedOutputSizeCSV(){
    	switch (this.type){
    		case INT:
    			return 11; // standard int max lenght, with sign	
    		case FLOAT:
    			return 15; // really rough extimate, may be longer
    		case DATE:  
    			return 10; //YYYY-MM-DD
    		case BOOLEAN:
    			return 1; //it is stored as 0/1 int
    		case STRING:
    			return 33; //this is very rough, should return the max size of the string 
    		case RECORD:
    			return 0; //what is this?
    		case MISSING:
    			return 0;
    		default: // actually unreachable code
    			return Integer.MIN_VALUE;
    	}
    }
 
    /**
     * Check that a Symbol has a specific DataType inside it, raise exception otherwise, 
     * and if passed a null as symbol. Missing values are ignored (but their type is not).
     * 
     * FIXME currently MISSING is a separate type from others in this method. 
     * Check that elsewhere MISSING is not recognized as a placeholder for missing value 
     * of unspecified type.
     * 
     * @param s a Symbol, not null
     * @param t the DataType we expect to find in the Symbol
     */
    public static void assertType(Symbol<?> s, DataType t) throws RuntimeException {
    	if (s==null) throw new RuntimeException ("Symbol.assertType() of null");
    	if (s.type!=t) throw new RuntimeException ("Symbol.assertType() expected type "+t.toString()+" got Symbol "+s.toString());
    }
    
}
