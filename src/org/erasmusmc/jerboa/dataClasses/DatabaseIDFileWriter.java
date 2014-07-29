package org.erasmusmc.jerboa.dataClasses;

import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.utilities.WriteCSVFile;

public class DatabaseIDFileWriter {
	  private WriteCSVFile file;
	  private boolean headerWritten = false;
	  
	  public DatabaseIDFileWriter(String filename){
	    file = new WriteCSVFile(filename);
	  }
	  
	  public void write(String dbID){
	    if (!headerWritten)
	      writeHeader();
	    List<String> columns = new ArrayList<String>();
	    columns.add(dbID);
	    file.write(columns);
	  }
	  
	  private void writeHeader() {
	    List<String> headers = new ArrayList<String>();
	    headers.add("DatabaseID");
	    file.write(headers);
	    headerWritten = true;
	  }

	  public void flush(){
	    file.flush();
	  }
	  
	  public void close(){
	    file.close();
	  }

}
