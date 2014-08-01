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

import it.cnr.isti.thematrix.configuration.LogST;

import java.util.ArrayList;
import java.util.List;

/**
 * A DatasetRecord is a symbol table that represents a row in a dataset.
 * In this case, each symbol represents an attribute of the dataset (a column of a table)
 * A dataset <strong>must have</strong> a schema, represented by a {@link DatasetSchema}
 * 
 * This class also provides some utility static methods
 * 
 * @author edoardovacchi
 */
@SuppressWarnings("serial")
public class DatasetRecord extends SymbolTable {
    
    private DatasetSchema schema; 
    private List<Symbol<?>> order;    
   
    /**
     * returns an empty record with the same schema of the given {@link DatasetRecord}
     * @param r 
     * @return a new {@link DatasetRecord} instance with each attribute empty (null)
     */
    public static DatasetRecord emptyRecord(DatasetRecord r) {
    	DatasetRecord newDataset = new DatasetRecord();
        newDataset.setSchema(r.getSchema());
        newDataset.clear();
        return newDataset;
    }

    
    /**
     * creates a new {@link DatasetRecord} for the given list of symbols, 
     * using these symbols as the data values.
     * 
     * It also sets automatically a {@link DatasetSchema} conforming to the given symbol types and names
     * 
     * @param columnList
     * @return the new record instances
     */
    public static DatasetRecord linkTo(List<Symbol<?>> columnList) {
        DatasetRecord newDataset = new DatasetRecord();
        DatasetSchema newSchema = new DatasetSchema("dataset-"+newDataset.hashCode()+"$custom");
        newSchema.putAll(columnList);
        newDataset.setSchema(newSchema);
        newDataset.putAll(columnList);
        return newDataset;
    }
    
    /**
     * Set the schema for this instance to be the same as the parameter, 
     * by performing a full copy (all symbols are cloned).
     * @param d
     */
    public void setSchema(DatasetSchema d) {
    	if (schema != null) 
    		LogST.logP(0, "DatasetRecord.setSchema("+d+") called more than once on DatasetRecord "+this.toString());
    	super.clear();
        
    	
		schema = d;
        order = new ArrayList<Symbol<?>>();
        
        for (Symbol<?> s : d.attributes()) {
            Symbol<?> clone = s.clone();
            this.put(s.name, clone);
        }
    }
    
    /**
     * The overridden method puts the values and keeps the ordering of the internal
     * list of attributes
     * 
     */
    @Override
    public Symbol<?> put(String name, Symbol<?> s) {
        Symbol<?> oldS = super.put(name, s);
        if (oldS == null) {
            // if s is new
            order.add(s);
        } else {
            // if we are overwriting s, keep the same ordering
            int pos = order.indexOf(oldS);
            order.set(pos, s);
        }
        return oldS;
    }
    
    /**
     * @return the list of symbols that belong to this record, in the order
     * specified at the moment of its creation
     * 
     */
    public List<Symbol<?>> attributes() {
    	if (order.isEmpty()) throw new Error("Something went wrong: this dataset has no attributes! schema: "+this.schema);
        return order;
    }
   
    /**
     * for each value in the argument dataset record, copies the values 
     * in the corresponding keys. Keys must exist in the current object
     * 
     * @param r
     * @throws MissingSymbolException if the schemas are mismatched
     * 
     */
    public void setAll(DatasetRecord r) {
    	for (Symbol<?> s : r.attributes()) {
    		this.set(s.name, s.value);
    	}
    }
    
    /**
     * sets all the values to null
     */
    public void clear() {
    	for (Symbol<?> s : this.schema.attributes()) {
    		this.set(s.name, null);
    	}
    }
    
   
    @Override
    public Symbol<?> get(Object symbolName) {
    	try {
    		return super.get(symbolName);
    	} catch(MissingSymbolException e) {
    		LogST.logP(0, "DatasetRecord.get() WARNING: Missing symbol, dumping schema");
    		LogST.logP(0, this.getSchema().toString());
    		throw e;
    	}
    }
    
    public DatasetSchema getSchema() {
        if (schema==null) throw new RuntimeException("Cannot getSchema() from "+this+": no schema set!");
        return schema;
    }
    
    public boolean isSchemaEmpty() {
        return this.isEmpty();
    }
    
    /**
     * Copies the contents of current pointed record in a non-volatile copy
     * @return a non-volatile copy of the current record
     */
    @Deprecated
    public DatasetRecord deepCopy() {
        DatasetRecord r = new DatasetRecord();
        r.setSchema(this.getSchema());
        for (Symbol<?> s: this.attributes()) {
            r.set(s.name, s.value);
        }
        return r;
    }

}
