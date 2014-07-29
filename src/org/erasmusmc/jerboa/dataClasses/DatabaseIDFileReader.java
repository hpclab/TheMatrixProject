package org.erasmusmc.jerboa.dataClasses;

import java.util.Iterator;
import java.util.List;

public class DatabaseIDFileReader implements Iterable<DatabaseID> {
  private String filename;

	public DatabaseIDFileReader(String filename) {
    this.filename = filename; 
	}

	public Iterator<DatabaseID> iterator() {
    return new DatabaseIDIterator(filename);
	}

  private class DatabaseIDIterator extends InputFileIterator<DatabaseID> {
    private int databaseID;
      
    public DatabaseIDIterator(String filename) {
      super(filename);
    }
      
    public void processHeader(List<String> row){
    	databaseID = findIndex("DatabaseID", row);
    }

    public DatabaseID row2object(List<String> columns) throws Exception{
    	DatabaseID databaseid = new DatabaseID();
    	databaseid.databaseID = columns.get(databaseID);
      return databaseid;
    }
  }
}
