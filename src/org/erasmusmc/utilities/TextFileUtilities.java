package org.erasmusmc.utilities;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class TextFileUtilities {
  
  public static List<String> loadFromFile(String filename){
    List<String> result = new ArrayList<String>();
    for (String line : new ReadTextFile(filename))
    	result.add(line);
    return result;
  }
  
  
  public static void saveToFile(List<String> lines, String filename){
  	WriteTextFile out = new WriteTextFile(filename);
  	for (String line : lines)
  		out.writeln(line);
  	out.close();
  }
  
  public static void appendToFile(String text, String fileName) {                 
    try {
      FileOutputStream file = new FileOutputStream(fileName,true);
      BufferedWriter bufferedWrite = new BufferedWriter(new OutputStreamWriter(file),1000000);
      try {
        bufferedWrite.write(text);  
        bufferedWrite.newLine();
        bufferedWrite.flush();
        bufferedWrite.close();
      }catch (IOException e) {
        e.printStackTrace();
      }
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    }
  }
}  
