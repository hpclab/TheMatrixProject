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

import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class ExposureFileWriter {
  private WriteCSVFile file;
  private boolean headerWritten = false;
  public ExposureFileWriter(String filename){
    file = new WriteCSVFile(filename);
  }
  
  public void write(Exposure exposure){
    if (!headerWritten)
      writeHeader();
    List<String> columns = new ArrayList<String>();
    columns.add(exposure.patientID);
    columns.add(exposure.caseset);
    columns.add(StringUtilities.daysToSortableDateString(exposure.start));
    columns.add(exposure.type);
    file.write(columns);
  }
  
  private void writeHeader() {
    List<String> headers = new ArrayList<String>();
    headers.add("PatientID");
    headers.add("CaseSet");
    headers.add("Date");
    headers.add("Type");
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