/*
 * Copyright (c) 2010-2014 "HPCLab at ISTI-CNR"
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.isti.thematrix.scripting.sys;

import java.util.HashMap;

/**
 * A simple Map from schema names to {@link DatasetSchema}.
 * It is designed to hold each schema that should be globally available or it may be necessary 
 * throughout a script. It is used in {@link TheMatrixSys} and {@link TheMatrixIADDefinition}
 * to maintain a list of the predefined schemata. It may be used by the interpreter to add
 * new schema definitions (using `declareSchema`. This feature has never really been used as of 01/01/14)
 * 
 * 
 * @author edoardovacchi
 */
@SuppressWarnings("serial")
public class SchemaTable extends HashMap<String,DatasetSchema> {
	
	/**
	 * Factory method that creates a new {@link DatasetSchema} instance
	 * with the given name and insert it into the table.
	 * 
	 * @param name
	 * @return the new DatasetSchema instance, ready to setup with the attribute names and types
	 */
    public DatasetSchema create(String name) {
        if (this.containsKey(name)) throw new RuntimeException("Schema already exists: "+name);
        DatasetSchema ds = new DatasetSchema(name);
        this.put(name, ds);
        return ds;
    }
}
