package org.erasmusmc.utilities;

import it.cnr.isti.thematrix.configuration.LogST;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ReadCSVASCIIFile  implements Iterable<List<ASCIIString>>{
  protected BufferedReader bufferedReader;
  private FileInputStream textFileStream;
  private boolean EOF = false;
  private char delimiter = ',';
  private int lastRowSize = 0;
  private static ASCIIString quotes = new ASCIIString("\"");
  private static ASCIIString escapedQuotes = new ASCIIString("\\\"");

  public ReadCSVASCIIFile(String filename) {
    try {
      textFileStream = new FileInputStream(filename);
      bufferedReader = new BufferedReader(new InputStreamReader(textFileStream));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  public ReadCSVASCIIFile(InputStream inputstream){
  	bufferedReader = new BufferedReader(new InputStreamReader(inputstream));
  }
  
  public void close(){
  	try {
  		textFileStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
  
  public Iterator<List<ASCIIString>> getIterator() {
    return iterator();
  }

  private class CSVFileIterator implements Iterator<List<ASCIIString>> {
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

    public List<ASCIIString> next() {
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
    	LogST.logP(0,"ReadCSVASCIIFile.remove(): Unimplemented method 'remove' called");
    }
  }

  public Iterator<List<ASCIIString>> iterator() {
    return new CSVFileIterator();
  }
  
  private List<ASCIIString> line2columns(String line){
    List<ASCIIString> columns = safeSplit(line, delimiter);
    for (int i = 0; i < columns.size(); i++){
    	ASCIIString column = columns.get(i);
      if (column.startsWith(quotes) && column.endsWith(quotes) && column.length() > 1)
        column = column.substring(1, column.length()-1);
      column = column.replace(escapedQuotes, quotes);
      columns.set(i, column);
    }
    return columns;
  }
  
  public List<ASCIIString> safeSplit(String string, char divider){
  	ArrayList<ASCIIString> result = new ArrayList<ASCIIString>(lastRowSize);
    if(string.length()==0){
      result.add(new ASCIIString(""));
      return result;
    }
    boolean literal = false;
    boolean escape = false;
    int startpos = 0;
    int i = 0;
    char currentchar;
    while (i < string.length()){
      currentchar = string.charAt(i);
      if (currentchar =='"')
      	literal = !literal;
      if (!literal && (currentchar == divider && !escape)){
        result.add(new ASCIIString(string,startpos,i));
        startpos = i+1;
      }
      if (currentchar == '\\')
      	escape = !escape; 
      else 
      	escape = false;
      i++;
    }
    //if (startpos != i){
      result.add(new ASCIIString(string,startpos,i));
    //}  
    lastRowSize = result.size();
    return result;
  } 

	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	public char getDelimiter() {
		return delimiter;
	}
}
