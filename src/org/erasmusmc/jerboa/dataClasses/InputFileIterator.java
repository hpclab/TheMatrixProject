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

import java.util.Iterator;
import java.util.List;

import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.StringUtilities;

public abstract class InputFileIterator<T> implements Iterator<T>  
{
	private Iterator<List<String>> iterator;
	private T buffer;
	private String filename;
	private int lineCount = 1;
	private boolean eof = false;
  
	public InputFileIterator(String filename)
	{
		open(filename);
	}
  
  /**
   * If you use this constructor, you must make an implicit call to the 'open' method!
   */
  protected InputFileIterator(){}
  
  protected void open(String filename){
    this.filename = filename;
    ReadCSVFile file = new ReadCSVFile(filename);
    iterator = file.iterator();
    if (iterator.hasNext()) {
      processHeader(iterator.next());
      lineCount++;
    }
    if (buffer == null && iterator.hasNext()) {
      buffer = errorCaughtRow2Object(iterator.next());
      lineCount++;
    } else
      eof = true;
  }
  
  private T errorCaughtRow2Object(List<String> row) {
    try {
      return row2object(row);
    } catch (Exception e){
      throw new InputFileException(lineCount, filename, "Error parsing file \"" + filename + "\" in line " + lineCount + ": "+ e.getLocalizedMessage());
    }
  }

  public boolean hasNext() {
    return !eof;
  }
  
  public T next() {
    T result = buffer;
    if (iterator.hasNext()){
      buffer = errorCaughtRow2Object(iterator.next());
      lineCount++;
    } else
      eof = true;
    return result;
  }

  public void remove() {
    System.err.println("Unimplemented method 'remove' called");
  }

  protected abstract T row2object(List<String> row) throws Exception;

  protected abstract void processHeader(List<String> row);
  
  protected int findIndex(String value, List<String> list) {
    int result = StringUtilities.caseInsensitiveIndexOf(value, list);
    if (result == -1)
      throw new InputFileException(lineCount, filename, "Could not find column \""+value+"\" in file "+filename);
    return result;
  }
  
  protected int findIndexOptional(String value, List<String> list) {
    return StringUtilities.caseInsensitiveIndexOf(value, list);
  }

}
