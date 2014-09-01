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
 * The base class for each module of the Matrix scripting language
 * 
 * @author edoardovacchi
 */
@SuppressWarnings("serial")
public abstract class MatrixModule extends DatasetRecord {
    public String name; 
    private List<MatrixModule> consumers = new ArrayList<MatrixModule>();
    
    /**
	 * Store here the logical consumer in case this module has no consumers due
	 * to postprocessing; disregarded by script execution. I.e.: a fileOutput to
	 * a temporary file is logically followed by modules reading that temporary
	 * file. This is relevant for debug output and postprocessing, and defaults to the empty list. 
	 */
    private List<MatrixModule> logicalConsumers = null;
    
    public MatrixModule(String name) {
        this.name = name;
    }
   
    /**
     * 
     * This method tests whether the given name of a schema matches with the internal schema
     * for this module. E.g., the test passes when both inputSchema == "FOO" and 
     * {@link #getSchema()} == "FOO". The test also passes when {@link #getSchema()} ends with the
     * magic string '$custom'. This is the way modules with a custom schema are internally represented.
     * The effect is that when the schema is custom, we are disabling any sanity check
     *
     * 
     * @param inputSchema a schema name
     * @throws MismatchedSchemaException when the name of the schema for this module 
     * does not match the given schema name (the test always passes if this schema ends 
     * with the magic string '$custom'
     * 
     */
    public void schemaMatches(String inputSchema) {
        if (!this.getSchema().name.equals(inputSchema) && !this.getSchema().name.endsWith("$custom")) {
            throw new MismatchedSchemaException(name, this.getSchema().name, inputSchema);
        }
    }
    
    /**
     * Adds a consumer to the internal list
     * @param m 
     */
    public void addConsumer(MatrixModule m) {
    	if (!consumers.contains(m))
    		consumers.add(m);
    }
    
    public List<MatrixModule> getConsumers() {
    	return consumers;
    }
    
    public int getReferenceCount() {
    	return consumers.size();
    }
    
    /**
     * This method is invoked <strong> once </strong> by the interpreter, after the constructor, 
     * during the analysis of the input script.
     * 
     * The purpose of this method is to setup the internal state of the module, so that the following
     * modules in the script can safely access this module's data
     * 
     * In this method, the programmer <strong>must</strong> invoke 
     * {@link #setSchema(DatasetSchema)} with a proper {@link DatasetSchema} instance
     * and any other internal state variables must be initialized to sane defaults.
     * 
     * If this module has inputs, it is also supposed to invoke {@link #addConsumer(MatrixModule)}
     * for each input module instance.
     * 
     * Subclasses may implement this method in such a way that it may be re-invoked by 
     * {@link #changeInput(MatrixModule)} or {@link #substituteInput(MatrixModule, MatrixModule)}
     * to re-setup the internal state of the module.
     *   
     */
    public abstract void setup();
    
    /**
     * this method has no longer a real purpose. It is invoked by the interpreter when <strong>all</strong> 
     * of the input script has been processed, for all modules in the script, in sequence.
     * 
     */
    @Deprecated
    public abstract void exec();
    
    /**
     * resets the internal state of the module: it restore the internal cursor to the first
     * record of the dataset, and it restores any other variable to sane defaults. 
     */
    public abstract void reset();
    
    /**
     * this method returns true until the dataset has reached its last record. 
     * When the last record is reached it returns false. This method is allowed to
     * manipulate the internal state of the object, provided that 
     * two subsequent call to this method does not change any observable result 
     * (e.g., this method should not advance the internal pointer)
     * 
     * @return true until the dataset has reached its last record
     * 
     */
    public abstract boolean hasMore();
    
    /**
     * advances the internal pointer to the next record in the dataset. Implementors
     * may choose to return an error if {@link #hasMore()} is false. When a module has been
     * constructed and setup, this method should invoked at least once, before accessing
     * the values pointed at (e.g. using {@link MatrixModule#get(Object)}).
     * 
     */
    public abstract void next();
    
    /**
     * When this method is invoked <strong>before</strong> any invocation to {@link #next()}
     * the behavior is <strong>undefined</strong>. Default implementations may either return
     * symbols holding `null` values or a default value. For instance, if the dataset has a schema:
     * 
     * <code>[{FOO: string}; {BAR: string}; {BAZ: int = 0}]</code>
     * 
     * where `0` is the default value for column BAZ, then the expected result would be the dataset:
     * 
     * <code> null, null, 0 </code>
     * 
     * However, being this module-depenendant, it is not advisable for subclasses and clients
     * of this method to rely on this information: this method should always be invoked when next()
     * has been invoked at least once. Subclasses may choose to throw an {@link Error} or a 
     * {@link RuntimeException} if this contract is not satisfied.
     * 
     * 
     */
    @Override
    public Symbol<?> get(Object o) {
    	try {
    	return super.get(o);
    	} catch (MissingSymbolException ex) {
    		LogST.logP(0, "ERROR: MatrixModule.get() failed: module "+this.name+" can't find symbol "+o.toString());
    		if (this.consumers.size()>0)
    			LogST.logP(0, "ERROR: Error triggered with following (pulling) module "+this.consumers.get(0).name);
    		else
    			LogST.logP(0, "ERROR: Error happened with no following module");
//    		ex.printStackTrace(new PrintWriter(LogST.getInstance().getWriter()));
    		LogST.logException(ex);
    		LogST.spindown();
			/**
			 * this relaunched exception shall definitely be caught, worst case it is returned by the eval() inside
			 * TheMatrixSys
			 */
    		throw new MissingFieldException(ex.getMessage(), this); 
    	}
    }
    
    
    /**
	 * Special method used to hack the input of a module to a different module
	 * <b>with the same schema</b>.
	 * 
	 * By default (if not overridden by a concrete module class) it will emit a
	 * line in the log. This method is needed for relinking those modules that
	 * keep a reference to their source modules after their setup() call
	 * (examples: Filter, Product), each time we need to modify the graph of
	 * modules right before execution of the script (postprocessing in
	 * TheMatrix.sys, SortModule and others).
	 * 
	 * Method needed whenever we want to add a buffer module with its proxy
	 * modules. This always happens after the module chain has been already
	 * built by the interpreter.
	 * 
	 * Method used to have a by-default error behaviour as 1) the input variable
	 * has not been standardized in this abstract class; 2) there can be
	 * specific dependencies with code in the setup, exec and reset methods.
	 * 
	 * Currently, it just logs (i.e. all modules which need it MUST override it,
	 * or they will compute on void/incorrect data).
	 * 
	 * FIXME rewrite following quick and dirty notes about the effect of
	 * changing inputs
	 * 
	 * per il filter: change input analizza i due riferimenti ai termini da
	 * confrontare; <br>
	 * li cambia con il nuovo input. se la seconda colonna non viene trovata
	 * vuol dire che era una costante e viene ignorata. (questo perche' il
	 * filter puo' usare piu' colonne del suo unico input).
	 * 
	 * per il product e in generale per tutti i moduli con due input distinti <br>
	 * c'e' il problema di capire quale stiamo andando a sostituire, la
	 * changeinput con un solo parametro non basta
	 * 
	 * TODO In the future, we may refactor MatrixModule to contain the input
	 * reference, taking care of the common case.
	 * 
	 * @param m
	 *            the new module to be set as input (passed by reference, does
	 *            not need to belong to the module list).
	 */
    public void changeInput (MatrixModule m)
    {
    	LogST.logP(-1, "MatrixModule.changeInput() not overridden for module "+this.name);
    	LogST.logP(-1, Thread.currentThread().getStackTrace().toString());
    	throw new Error ("MatrixModule.changeInput() not overridden for module "+this.name);
    }

    /**
	 * This method is needed for relinking purposes of modules that
	 * have more than one input. It substitute the first input parameter with 
	 * the second input parameter.
     * @param old old input module
     * @param brand_new   new input module
	 */
    public void substituteInput(MatrixModule old, MatrixModule brand_new)
    {
    	throw new Error ("MatrixModule.substituteInput() not allowed for module "+this.name);
    }
    
    /**
     * This method must be override by those modules that have more 
     * than one input. Default behaviors is to return true. 
     *   
     * @return true if the module has only one input, false otherwise.
     */
    public boolean hasOneInput()
    {
    	return true;
    }
    

    /**
     * default toString() for Modules
     */
	@Override
	public String toString() {
		return "MatrixModule [name=" + name + ", consumers=" + consumers + ", map size=" + this.size() +  ", content values=" + this.values()
				+ "]";
	}

	/**
	 * set the logical consumers to a list of modules
	 * @param lConsumers
	 */
	public void setLogicalConsumers(List<MatrixModule> lConsumers){
		logicalConsumers = lConsumers;
	}

	/**
	 * set the logical consumers to a single module list
	 * @param consumer
	 */
	public void setLogicalConsumer(MatrixModule consumer) {
		logicalConsumers = new ArrayList<MatrixModule>(1);
		logicalConsumers.add(consumer);
	}
	
	/**
	 * return the list of logical consumers
	 * @return
	 */
	public List<MatrixModule> getLogicalConsumers(){
		return logicalConsumers;
	}
	
}
