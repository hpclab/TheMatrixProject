/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
