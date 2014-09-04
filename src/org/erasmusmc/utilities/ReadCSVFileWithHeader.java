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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ReadCSVFileWithHeader implements Iterable<Row>{
	private String filename;
	
  public ReadCSVFileWithHeader(String filename){
  	this.filename = filename;
  }
  
	@Override
	public Iterator<Row> iterator() {
		return new RowIterator(filename);
	}
	
	public class RowIterator implements Iterator<Row>{

		private Iterator<List<String>> iterator;
		private Map<String, Integer> fieldName2ColumnIndex;
		public RowIterator(String filename){
			iterator = new ReadCSVFile(filename).iterator();
			fieldName2ColumnIndex = new HashMap<String, Integer>();
			for (String header : iterator.next())
				fieldName2ColumnIndex.put(header, fieldName2ColumnIndex.size());
		}
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Row next() {
			return new Row(iterator.next(),fieldName2ColumnIndex);
		}

		@Override
		public void remove() {
			throw new RuntimeException("Remove not supported");			
		}
		
	}
}
