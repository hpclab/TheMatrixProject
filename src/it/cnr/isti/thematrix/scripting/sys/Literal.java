/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.sys;

import it.cnr.isti.thematrix.scripting.utils.DataType;

/**
 * A literal is a symbol with a hardcoded name that represents the row, column
 * at which it was found in the source file.
 * 
 * @author edoardovacchi
 */
public class Literal<T> extends Symbol<T> {
	private final int row, col;
    public Literal(int row, int col, T value, DataType t) {
        super(String.format(
            "literal#%d,%d", row, col
        ), value, t);
        this.row = row;
        this.col = col;
    }
    
    public int getRow() {
    	return row;
    }
    
    public int getColumn() {
    	return col;
    }
}
