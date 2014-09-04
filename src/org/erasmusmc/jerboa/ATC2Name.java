/*
 * Copyright (c) Erasmus MC
 *
 * This file is part of TheMatrix.
 *
 * TheMatrix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
