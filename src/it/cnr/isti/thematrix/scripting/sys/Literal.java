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
 * A literal is a symbol with a hardcoded name that represents the row, column
 * at which it was found in the source file.
 * 
 * @author edoardovacchi
 * @param <T> 
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
