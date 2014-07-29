package org.erasmusmc.jerboa.dataClasses;

import java.util.Iterator;
import java.util.List;

import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.StringUtilities;

public class AggregatedDataFileReader { 
  public static final String UNKNOWN = "unknown";
	public static boolean legacyMode = false;
  private String filename;
  private Iterator<List<String>> iterator;
  private String tableName = UNKNOWN;
  
  public boolean moveToTable(String tableName){
  	while (!getCurrentTableName().equalsIgnoreCase(tableName)){
      Iterator<List<String>> iterator = getStringIterator();
      if (!iterator.hasNext())
      	break;
      while (iterator.hasNext())
        iterator.next();
    }
    return getCurrentTableName().equalsIgnoreCase(tableName);
  }
  
  public boolean hasTable(String tableName){
    ReadCSVFile file = new ReadCSVFile(filename);  
    for (List<String> cells : file){
    	if (legacyMode){
        if (cells.size() != 0 && cells.get(0).startsWith("* Start of data type ")) 
          if (tableName.equalsIgnoreCase(StringUtilities.findBetween(cells.get(0), "* Start of data type ", " *")))
            return true;
    	} else {
      if (cells.size() != 0 && cells.get(0).startsWith("* Start of table ")) 
        if (tableName.equalsIgnoreCase(StringUtilities.findBetween(cells.get(0), "* Start of table ", " *")))
          return true;
    	}
    }
    return false;        
  }

  public AggregatedDataFileReader(String filename) {
    this.filename = filename;
  }

 
  public Iterator<List<String>> getStringIterator() {
    if (iterator == null)
      iterator = new ReadCSVFile(filename).iterator();//.getIterator();
    return new StringIterator();
  }
   
  public String getCurrentTableName(){
  	return tableName;
  }
  
  private class StringIterator implements Iterator<List<String>> {
    private boolean eof = false;
    private List<String> buffer = null;

    public StringIterator(){
      if (!iterator.hasNext()) 
        eof = true;
      else
        next(); 
    }
    
    public boolean hasNext() {
      return !eof;
    }

    public List<String> next() {
      List<String> result = buffer;
      if (iterator.hasNext()) {
        List<String> cells = iterator.next();
        if (cells.size() == 1){
          if (cells.get(0).startsWith("* Deleted")) {
            eof = true;
            if (iterator.hasNext()) 
              cells = iterator.next();
          }
          if (legacyMode) {
          	if (cells.get(0).startsWith("* Start of data type ")) {
          		tableName = StringUtilities.findBetween(cells.get(0), "* Start of data type ", " *");
          		eof = true;
          	}
          } else {
          	if (cells.get(0).startsWith("* Start of table ")) {
          		tableName = StringUtilities.findBetween(cells.get(0), "* Start of table ", " *");
          		eof = true;
          	}
          }
        }        
        buffer = cells;        
      } else
        eof = true;
      
      return result;
    }

    public void remove() {
      System.err.println("Unimplemented method 'remove' called");
    }
  }
}