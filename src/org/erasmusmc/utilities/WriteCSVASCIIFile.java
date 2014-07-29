package org.erasmusmc.utilities;

import it.cnr.isti.thematrix.configuration.LogST;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;


public class WriteCSVASCIIFile  {
	
  private static ASCIIString quotes = new ASCIIString("\"");
  private static ASCIIString escapedQuotes = new ASCIIString("\\\"");
  private static ASCIIString comma = new ASCIIString(",");
  
  public WriteCSVASCIIFile(String filename){
    FileOutputStream PSFFile;
    try {
      PSFFile = new FileOutputStream(filename);
      bufferedWrite = new BufferedWriter( new OutputStreamWriter(PSFFile),10000);      
    } catch (FileNotFoundException e) {
    	LogST.logException(e);
    	//      e.printStackTrace();
    }
  }
  
  public void write(List<ASCIIString> string){
    try {
      bufferedWrite.write(columns2line(string));
      bufferedWrite.newLine();
    } catch (IOException e) {
    	LogST.logException(e);
//      e.printStackTrace();
    }
  }
  
  
	/**
	 * This function reformats a row from the CSVfile into a String, and has
	 * major performance and stability issues. The StringBuilder is initialized
	 * to default length, which will always be too short for practical files -->
	 * memory fragmentation.
	 * 
	 * @param columns
	 * @return
	 */
private static String columns2line(List<ASCIIString> columns) {
	/* added new default initializzation length to reduce mem fragmentation*/
    StringBuilder sb = new StringBuilder(16*columns.size());
    Iterator<ASCIIString> iterator = columns.iterator();
    while (iterator.hasNext()){
    	ASCIIString column = iterator.next();
      column = column.replace(quotes, escapedQuotes);
      if (column.contains(comma)){
      	sb.append("\"");
      	sb.append(column);
      	sb.append("\"");
      } else 
        sb.append(column);
      if (iterator.hasNext())
        sb.append(",");
    }
    return sb.toString();
  }

  public void flush(){
    try {
      bufferedWrite.flush();
    } catch (IOException e) {
    	LogST.logException(e);
//     e.printStackTrace();
    }
  }
  
  public void close() {
    try {
      bufferedWrite.close();
    } catch (IOException e) {
    	LogST.logException(e);
//      e.printStackTrace();
    }
  }
  
  private BufferedWriter bufferedWrite;
}
