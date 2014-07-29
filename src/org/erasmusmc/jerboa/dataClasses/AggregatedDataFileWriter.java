package org.erasmusmc.jerboa.dataClasses;

import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.utilities.WriteCSVFile;

public class AggregatedDataFileWriter {
  private WriteCSVFile file;

  public AggregatedDataFileWriter(String filename){
    file = new WriteCSVFile(filename);
  }
  
  public void writeHeader(List<String> settings, String version, String runLabel, String consoleText){
    List<String> cells = new ArrayList<String>();
    cells.add("*** Jerboa version "+ version);
    file.write(cells);
    if (runLabel != null){
      cells.clear();
      cells.add("*** Run label: "+ runLabel);
      file.write(cells);
    }
    for (String setting : settings){
      cells.clear();
      cells.add("*** " + setting);
      file.write(cells);
    }
    if (consoleText != null){
    	cells.clear();
    	cells.add("***");
    	file.write(cells);
    	cells.clear();
    	cells.add("*** Console output:");
    	file.write(cells);
    	for (String line : consoleText.split("\n")){
    		cells.clear();
    		cells.add("*** " + line.replace(",", "").replace("\r", ""));
    		file.write(cells);
    	}
    }
  }
  
  public void writeStartOfTable(String name){
    List<String> cells = new ArrayList<String>();
    cells.add("*** Start of file " + name + " ***");
    file.write(cells);
  }

  public void write(List<String> data){
    file.write(data);
  }

  public void flush(){
    file.flush();
  }
  
  public void close(){
    file.close();
  }
}