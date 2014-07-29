package org.erasmusmc.jerboa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.erasmusmc.utilities.ReadTextFile;
import org.erasmusmc.utilities.StringUtilities;

public class ATC2Name {
  public ATC2Name(String filename){
    ReadTextFile in = new ReadTextFile(filename);
    for (String line : in){
      processLine(line);
    }
  }
  
  public ATC2Name(InputStream stream){
    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
      while (bufferedReader.ready()) {
        String line = bufferedReader.readLine();
        processLine(line);
      }
      bufferedReader.close();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void processLine(String line) {
    String atc = StringUtilities.findBetween(line, "ATCCode='", "'");
    if (atc.length() > 0){
      String name = StringUtilities.findBetween(line, "Name='", "'");
      atc2name.put(atc, name);
    }    
  }

  public String getName(String atc){
    String name = atc2name.get(atc);
    if (name == null)
      return "Unknown";
    else 
      return name;
            
  }
  private Map<String, String> atc2name = new HashMap<String, String>();
  
}
