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

public class DatabaseIDFileReader implements Iterable<DatabaseID> {
  private String filename;

	public DatabaseIDFileReader(String filename) {
    this.filename = filename; 
	}

	public Iterator<DatabaseID> iterator() {
    return new DatabaseIDIterator(filename);
	}

  private class DatabaseIDIterator extends InputFileIterator<DatabaseID> {
    private int databaseID;
      
    public DatabaseIDIterator(String filename) {
      super(filename);
    }
      
    public void processHeader(List<String> row){
    	databaseID = findIndex("DatabaseID", row);
    }

    public DatabaseID row2object(List<String> columns) throws Exception{
    	DatabaseID databaseid = new DatabaseID();
    	databaseid.databaseID = columns.get(databaseID);
      return databaseid;
    }
  }
}
