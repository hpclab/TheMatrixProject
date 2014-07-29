/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.sys;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.utils.DataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * A {@link DatasetSchema} is a {@link SymbolTable} that represents the schema
 * for a {@link DatasetRecord}. Each symbol in a {@link DatasetSchema} represents 
 * an attribute of the record
 * 
 * @author edoardovacchi
 */
public class DatasetSchema extends SymbolTable {
    
    
	/**
	 * Extend a schema with a new Symbol.
	 * 
	 * @param schema
	 * @param s
	 * @return
	 */
    public static DatasetSchema extend(DatasetSchema schema, Symbol<?> s) {
//         List<Symbol<?>> xs = (List<Symbol<?>>) Collections.singletonList((Symbol<?>)s);
         List<Symbol<?>> ss = new ArrayList<Symbol<?>>(1);
//         ss.add(s);
         return DatasetSchema.extend(schema, ss);
     }

    /**
     * Generate a new schema name from the name of an existing schema, adds an hash code.
     * @param schema
     * @return
     */
    private static String generateName(DatasetSchema schema) {
    	 return schema.name+schema.hashCode()+"$custom";
     }

    /**
	 * Return a schema extended with a List of new Symbols, actually returns a
	 * new schema (with different schema name).
	 * 
	 * @param schema
	 * @param newAttributes
	 * @return
	 */
    public static DatasetSchema extend(DatasetSchema schema, List<Symbol<?>> newAttributes) {
        DatasetSchema newSchema = new DatasetSchema(generateName(schema));
        newSchema.putAll(schema.attributes());
        newSchema.putAll(newAttributes);
        return newSchema;
    }
    
    /**
	 * Return a schema extended with a List of new Symbols, actually returns a
	 * new schema (with schema name built using the prefix parameter).
	 * 
	 * @param schema
	 * @param newAttributes
	 * @param prefix
	 * @return
	 */
    public static DatasetSchema extend(DatasetSchema schema, List<Symbol<?>> newAttributes, String prefix) {
    	DatasetSchema newSchema = new DatasetSchema(schema.name+"|"+prefix+"$custom");
        newSchema.putAll(schema.attributes());
        newSchema.putAll(newAttributes);
        return newSchema;
    }
    
    /**
	 * Check if a set of attributes can be added to a dataset without duplication in the Schema, return null if no
	 * issues are found. The method checks both duplication with the existing schema and duplicate name in the list of
	 * attributes to be added. If passed a null or empty list of attributes, the method will warn in the logs but return
	 * true. A detailed error message is returned if issues are found.
	 * 
	 * @param schema
	 *            the schema to extend
	 * @param newAttributes
	 *            the List of new attributes as Symbol<?>
	 * @return either a String with detailed error messages about all conflicting new attributes, or null if there are
	 *         none.
	 */
    public static String canExtend(DatasetSchema schema, List<Symbol<?>> newAttributes){
    	if (newAttributes == null || newAttributes.size() == 0)
    	{
    		LogST.logP(0,"WARNING DatasetSchema.canExtend() called with empty or null newAttributes");
    		return null;
    	}
    	String result = null;
    	
    	List<String> duplicates = new ArrayList<String>();
    	for (Symbol<?> attribute : newAttributes)
    	{
    		if (schema.containsKey(attribute.name)) 
    			duplicates.add(attribute.name);
    	}
    	if (duplicates.size()>0)
    		result = " new attributes duplicate existing attributes: "+duplicates.toString()+";";
    	
    	/** 
    	 * sort the List of new attributes, so that we can check with the existing ones and between them.
    	 * We copy the list for sorting it, but we do not modify its elements.
    	 * FIXME the comparator for Symbol<?> should be in Symbol
    	 **/
    	ArrayList<Symbol<?>> copyNewAttributes = new ArrayList(newAttributes);
    	
    	Collections.sort(copyNewAttributes, new Comparator<Symbol<?>>() {
    		public int compare (Symbol<?> s1, Symbol <?> s2) {return s1.name.compareTo(s2.name);} 
    		} );

    	Iterator<Symbol<?>> i = copyNewAttributes.iterator();
    	Symbol<?> s = i.next(); // we know the list is not empty
    	Symbol<?> prev = null;
    	duplicates.clear();
    	
    	while (i.hasNext()) {
    		prev = s;
    		s=i.next();
    		if (s.name.equals(prev.name)) 
    			duplicates.add(s.name);
    	}
    	if (duplicates.size()>0) {
    		if (result == null) result = "";
    		result = result + " new attributes duplicate each other: "+duplicates.toString()+";";
    	}
    	return result == null? null: "DatasetSchema : "+result;
    }
    
    public final String name;
    private List<Symbol<?>> order = new ArrayList<Symbol<?>>();
    public DatasetSchema(String name) {
        this.name = name;
    }
    
    public DatasetSchema put(Symbol<?> s) {
    	this.put(s.name, s);
        return this;
    }
    
    @Override
    public Symbol<?> put(String name, Symbol<?> s) {
    	Symbol<?> retVal = super.put(s.name, s);
        this.order.add(s);
        return retVal;
    }
    
    
    
    public void putAll(Collection<? extends Symbol<?>> ss) {
        for(Symbol<?> s: ss) this.put(s.clone());
    }
    
    /**
     * 
     * @return List of the attributes in the order they were defined
     */
    public List<Symbol<?>> attributes() {
        return order; // Collections.unmodifiableList(order);
    }
    
    /**
     * @return the list of Symbol names in the order they were defined. May return the empty list.
     */
    public ArrayList<String> fieldNames() {
    	ArrayList<String> l = new ArrayList<String>();
		for (Symbol s : order) {
			l.add(s.getName());
		}
    	return l;
	 }
    
    public String toString() {
        return this.name + order;
    }
    
    /**
     * Convert the argument schema into a JSON String
     * @param schema
     * @return
     */
	public static String toJSON(DatasetSchema schema)
	{
		JSONArray list = new JSONArray();
		list.add(schema.name);
		for (Symbol<?> s: schema.attributes())
		{
			JSONObject obj = new JSONObject();
			obj.put("name", s.name);
			obj.put("type", s.type.toString());
			list.add(obj);
		}
                
		return list.toJSONString();
	}

	/**
	 * Convert the argument JSON string into a DatasetSchema
	 * @param content
	 * @return
	 */
	public static DatasetSchema fromJSON(String jsonContent)
	{
		Object obj=JSONValue.parse(jsonContent);
		JSONArray array=(JSONArray)obj;
		
		String schemaName = (String)array.get(0);
		DatasetSchema ds = new DatasetSchema(schemaName); 
		
		for (int i=1; i < array.size(); i++)
		{
			JSONObject o = (JSONObject)array.get(i);
			String name = (String)o.get("name");
			DataType type = DataType.valueOf((String)o.get("type"));
			ds.put(new Symbol<Integer>(name, null, type));
		}

		return ds;
	}
}
