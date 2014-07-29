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
