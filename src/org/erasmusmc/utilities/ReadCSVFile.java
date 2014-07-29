package org.erasmusmc.utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import it.cnr.isti.thematrix.common.Enums.*;
import it.cnr.isti.thematrix.configuration.LogST;

public class ReadCSVFile implements Iterable<List<String>>{

  protected BufferedReader bufferedReader;
  public boolean EOF = false;
  private char delimiter = ',';

  /**
   * compression : indicates type of compression for read/write the file 
   */
  private CompressionType compression = CompressionType.NONE;
  
  public ReadCSVFile(String filename) {
    try {
      FileInputStream textFileStream = new FileInputStream(filename);
      bufferedReader = new BufferedReader(new InputStreamReader(textFileStream));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * constructor with compressionType parameter to open file in a proper way depending on compression format.
   */
  public ReadCSVFile(String filename, CompressionType compressionType) {
	compression = compressionType;
    try {
    	switch (this.compression){
    	case NONE:
    	      bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
    	      break;
    	case GZIP:
    		bufferedReader = new BufferedReader(
	                new InputStreamReader(
	                		new GZIPInputStream (
	                			new FileInputStream(filename))) 
	                );
    		break;
    	case ZIP:
			ZipFile file=new ZipFile(filename);
			ZipInputStream zis = new ZipInputStream (new FileInputStream(filename));
			bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(zis.getNextEntry())));
    		 
    	}
      
    } catch (FileNotFoundException e) {
    	 e.printStackTrace();
    }
    catch (IOException e){
    	 e.printStackTrace();
    }
  }
  
  @Deprecated
  public ReadCSVFile(InputStream inputstream){
  	bufferedReader = new BufferedReader(new InputStreamReader(inputstream));
  }
  
//  public Iterator<List<String>> getIterator() {
//	  //OLD CODE = return iterator();
//	  return new CSVFileIterator();
//  }
  
  public Iterator<List<String>> iterator() {
	    return new CSVFileIterator();
	  }

  private class CSVFileIterator implements Iterator<List<String>> {
    private String buffer;
    
    public CSVFileIterator() {
      try {
        buffer = bufferedReader.readLine();
        if (buffer == null){
          EOF = true;
          bufferedReader.close();
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      
    }

    public boolean hasNext() {
      return !EOF;
    }

    public List<String> next() {
      String result = buffer;
      try {
        buffer = bufferedReader.readLine();
        if (buffer == null){
          EOF = true;
          bufferedReader.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      return line2columns(result);
    }

    public void remove() {
      LogST.logP(0,"ReadCSVFile.remove(): Unimplemented method 'remove' called");
    }
  }
  
  private List<String> line2columns(String line){
    List<String> columns = StringUtilities.safeSplit(line, delimiter);
    for (int i = 0; i < columns.size(); i++){
      String column = columns.get(i);
      if (column.startsWith("\"") && column.endsWith("\"") && column.length() > 1)
        column = column.substring(1, column.length()-1);
      column = column.replace("\\\"", "\"");
      columns.set(i, column);
    }
    return columns;
  }

	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	public char getDelimiter() {
		return delimiter;
	}
}
