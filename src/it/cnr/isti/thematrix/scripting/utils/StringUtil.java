package it.cnr.isti.thematrix.scripting.utils;

import it.cnr.isti.thematrix.scripting.sys.Symbol;

import java.util.Date;


/**
 * Utility class to convert Neverlang/TheMatrix symbols to strings for saving into csv files.
 * 
 * Modified to deal with MISSING values, as we have a flow of "null" strings in the saved CSV.
 * 
 * @author edoardo
 *
 */
public class StringUtil {
	/**
	 * Convert a given Symbol to its String representation.
	 * @param s	a Symbol 
	 * @return the String serialization. MISSING is represented by the empty string.
	 */
	public static String symbolToString(Symbol<?> s) {
    	//INT, FLOAT, DATE, BOOLEAN, STRING, RECORD, MISSING;
    	if (s.value==null) return "";
    	switch(s.type) {
    		case INT:
    		case FLOAT:
    			// TODO check that this outputs a *.?+ like float with optional exponent
    			return String.valueOf(s.value); 
    		case BOOLEAN:
    			if ((Boolean)s.value) return "1"; else return "0";
    		case STRING:
    			//FIXME handle null strings? shall we return the empty string instead?
				/*
				 * THIS is needed to correct the implementation. No time to test it now
				 * 
				 * if (s.value == null) return "";
				 */
    			return (String)s.value;
    		case DATE:
    			return DateUtil.toDateString((Date)s.value);
    		case MISSING:
    			return "";
    		default:
    			throw new Error("cannot convert to string: "+s.type);
    	}
    }
}
