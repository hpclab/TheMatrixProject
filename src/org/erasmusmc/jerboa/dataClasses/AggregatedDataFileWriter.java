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