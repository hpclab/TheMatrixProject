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
