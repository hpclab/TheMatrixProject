/*
 * Copyright (c) Erasmus MC
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.erasmusmc.utilities;

import java.util.List;
import java.util.Map;

public class Row {
	private List<String> cells;
	private Map<String, Integer> fieldName2ColumnIndex;

	public Row(List<String> cells, Map<String, Integer> fieldName2ColumnIndex){
		this.cells = cells;
		this.fieldName2ColumnIndex = fieldName2ColumnIndex;
	}

	public String get(String fieldName){
		int index;
		try {
			index = fieldName2ColumnIndex.get(fieldName);
		} catch(NullPointerException e){
			throw new RuntimeException("Field \"" + fieldName + "\" not found");
		}
		if (cells.size() <= index)
			return null;
		else
			return cells.get(index);
	}

	public List<String> getCells(){
		return cells;
	}
}
