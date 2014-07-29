package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.DatasetRecord;
import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import java.util.ArrayList;
import java.util.List;

/**
 * This module implements the merge operation between two datasets X,Y with a common key (internal key for the first
 * one). <br>
 * Assumes both datasets are ordered by the key. For each value of the key in X and Y, if (any number of) match is
 * found, output a row X extended with attributes from a row of Y, for all possible matching rows; if match is not
 * found, rows from X are emitted anyway extended with null values, rows of Y are discarded.
 * 
 * Implementation notes: the whole class prepares one row of the output in advance in order to be able to correctly
 * answer hasMore() calls. Tricky extra logic is required to distinguish and deal with the exhaustion of the input(s)
 * and the statement that no more rows can be output.
 * 
 * FIXME bug reported if one input file is empty and/or it is from a temporary sort file that is empty (because no data
 * is in that stream).
 * 
 * @author edoardovacchi, massimo
 */
public class MatrixMerge extends MatrixModule 
{
	private static final long serialVersionUID = -2782881595531302201L;
	
	private MatrixModule input1;
	private MatrixModule input2;
	private final List<String> primaryKey;
	private final List<String> fieldList;
	private int nAttributes;

	private List<Symbol<?>> diffAttributes = new ArrayList<Symbol<?>>();
	private int nDiffAttributes;

	public MatrixMerge(String name, String input1, String schema1, String input2, String schema2,
			List<String> primaryKey, List<String> fieldList) {
		super(name);
		this.input1 = TheMatrixSys.getModule(input1);
		this.input1.schemaMatches(schema1);
		this.input1.addConsumer(this);

		this.input2 = TheMatrixSys.getModule(input2);
		this.input2.schemaMatches(schema2);
		this.input2.addConsumer(this);

		if (primaryKey.size() != fieldList.size())
			throw new RuntimeException("Error in " + this.name + ": "
					+ "primary key and field list did not match in size.");

		// FIXME check for matching between column types

		this.nAttributes = primaryKey.size();

		this.primaryKey = primaryKey;
		this.fieldList = fieldList;

	}

	// new version

	/**
	 *  true iff reset() was called on this module
	 */
	private boolean inited = false; 

	/**
	 * true if the next row has been prepared in the buffer
	 */
	private boolean newRowReady = false; 

	/**
	 * true if the current row of input1 has been already used to produce at least one output row.
	 */
	private boolean input1Used = false;

	/**
	 * true if the current row for input1 is valid. Must be init.ed in setup(), is maintained by advance.
	 */
	private boolean input1Valid = true; 
	/**
	 * true if the current row for input2 is valid. Must be init.ed in setup(), is maintained by advance.
	 */
	private boolean input2Valid = true;

	/**
	 * true if we have run out of input data (on the input 1 channel). Note that we MAY have still have data on input 2,
	 * and we MAY have an output row in the buffer.
	 */
	private boolean atEnd = false; 

	/**
	 * internal buffer holding the next output row, which is prepared in advance.
	 */
	DatasetRecord buffer = null;

	/**
	 * The method next has a peculiar behavior. Next row of output is generated in advance and put into this.buffer.
	 * When called, generates following row and returns the content of buffer. Uses two vars to know if there are still
	 * records (not EOF) on each input. It is safe to call again when output is over.
	 * 
	 * 
	 * @see it.cnr.isti.thematrix.scripting.sys.MatrixModule#next()
	 */
	@Override
	public void next() {
		// whatever happens, we need an init run via reset();		
		if (!inited) reset();

		// first thing, we output our last generated row. May be empty at end. It is empty at reset() time.	
		this.setAll(buffer);
		newRowReady = false;
		
		// only happens at last line of output
		if (atEnd) { // ending condition flushes last row, next() does nothing if called again
			buffer.clear();
			newRowReady=false;
			return;
		}

		// only on first initialization
		// and check that input1 and input2 are not empty
		// FIXME switch to Valid; input 2 is no necessary condition
		//		if (!newRowReady && !input1.hasMore() && !input2.hasMore()) { return; }
		// this should no longer be needed
		
		// we enter the loop with valid records for X OR Y 
		do {
			// if keys are not equal, we will advance the smaller key
			switch (compareKeys()) {
				case 0 : // input 1 == input 2
					// generateRow(input2.attributes); // stores in buffer
					generateRowXY();
					advanceInput2();
					break;
				case -1 : // input1 < input2
					advanceInput1(); // calls input1.next(), can output a row
					break;
				case 1 : // input1 > input2
					advanceInput2(); // calls input2.next(), no output
					break;
			}
		} while (!newRowReady && (input1Valid || input2Valid ));

		// if we can get no more data from X input, then we are at the end
		if (! input1Valid) atEnd= true;
		// and if we could not _even_ generate a new row of output
		if (!newRowReady && !input1Valid) {
			buffer.clear();
			newRowReady = false;
		}
		//LogST.logP(3, "Merge exits with newRowReady ="+newRowReady+" prepared buffer with"+buffer.attributes());

	}

	/**
	 * Compares the two current records of X and Y based on the fields of the key.
	 * 
	 * We may be called with invalid X or Y. Semantics: EOF is the largest value for a key.
	 * 
	 * @return -1 if X<Y, 0 if equal, 1 if X>Y
	 */
	@SuppressWarnings("unchecked")
	public int compareKeys() {
		// if (input2HasMore) return -1; // WTF! why?
		// check that X and Y are valid records
		if (!input1Valid) { // X is over
			return 1;
		}
		if (!input2Valid) { // Y is over
			return -1;
		}
		for (int i = 0; i < this.nAttributes; i++) {
			String k1 = primaryKey.get(i);
			String k2 = fieldList.get(i);

			Comparable<Object> v1 = (Comparable<Object>) input1.get(k1).value;
			Comparable<Object> v2 = (Comparable<Object>) input2.get(k2).value;

			// FIXME data error logging --  to be used
			if (v1 == null) {
				LogST.logP(0, "WARNING : MatrixMerge.compareKeys()  Null Primary Key in input");
				return -1;
			} else if (v2 == null) {
				LogST.logP(0, "WARNING : MatrixMerge.compareKeys()  Null Secondary Key in input");
				return 1;
			} else if (v1.compareTo(v2) > 0) {
				return 1;
			}
			else if (v1.compareTo(v2) < 0) { return -1; }
		}
		// end of the loop, keys compare equal
		return 0;
	}

	/**
	 * Advances input 1; generates a row if input1 unused; updates input1Valid, input1Used.
	 */
	void advanceInput1() {
		if (!input1Used) {
			generateRowX(); // emit X extended with missing values
			input1Used= true;
			return;
		}

		input1Valid = input1.hasMore(); 
		if (input1Valid) input1.next();
		input1Used = false; // input1Used = input1Valid; ??
	}

	/**
	 * Advances input 2; updates input2Valid.
	 */
	void advanceInput2() {
		input2Valid = input2.hasMore();
		if (input2Valid) input2.next();
	}

	/**
	 * Generate a record combining valid input 1 and 2. 
	 */
	void generateRowXY() {
		generateRowX(); // important inits inside there

		for (Symbol<?> s : this.diffAttributes) {
			buffer.set(s.name, input2.get(s.name).getValue()); 
			//bug, we shall recover the input2 value!! there should be a cleaner way to do this
			//FIXME is this still a bug?
		}
	}

	/**
	 * Generate a row from input 1; updates input1Used.
	 */
	void generateRowX() {
		buffer.clear();
		buffer.setAll(input1);
		input1Used = true; // vero, nella semantica caso1 + caso3
		newRowReady = true;
	}

	// initialize the first row in the buffer
	public void reset() {
		inited = true;
		/* prepare the field copy function for generateRow -- maybe in setup() */
		input1Valid = input1.hasMore();
		if (input1Valid) 
			input1.next();
		input2Valid = input2.hasMore();
		if (input2Valid) 
			input2.next();
		next();
	}

	public void setup() 
	{

		// find all the attributes in 2 that are not in in fieldList
		for (Symbol<?> s : input2.getSchema().attributes()) {
			if (!fieldList.contains(s.name)) {
				diffAttributes.add(s);
			}
		}
		this.nDiffAttributes = this.diffAttributes.size();

		this.setSchema(DatasetSchema.extend(input1.getSchema(), diffAttributes, this.name));

		buffer = DatasetRecord.emptyRecord(this);
		buffer.clear();

	}
	
	@Override
	public void substituteInput(MatrixModule old, MatrixModule brand_new)
	{		
		// search for the correct input and change it
    	if (old.name.equalsIgnoreCase(input1.name))
    	{
    		brand_new.schemaMatches(input1.getSchema().name);
    		input1 = brand_new;
    	}
    	else if (old.name.equalsIgnoreCase(input2.name))
    	{
    		brand_new.schemaMatches(input2.getSchema().name);
    		input2 = brand_new;
    	}
    }
	
	@Override
	public boolean hasOneInput()
    {
    	return false;
    }
	
	

	@Override
	public void exec() {
		LogST.logP(2,"NO-OP EXEC "+this.name);
	}

	@Override
	public boolean hasMore() {
		if (!inited) reset();
		return newRowReady; //TODO this will have to go in a monitor!
	}

	/****
	 * @Override public void setup() {
	 * 
	 * 
	 *           }
	 * @Override public void exec() { System. out.println("EXEC "+this.name); }
	 * @Override public void reset() { throw new UnsupportedOperationException("Not supported yet."); }
	 * @Override public boolean hasMore() { return input2.hasMore(); }
	 * 
	 *           DatasetRecord lastRecord = null ;
	 * @Override public void next() { input2.next(); if (lastRecord==null) { // this.toggleEndOfDataset();
	 *           input1.next(); } do { lastRecord = input1.deepCopy(); input1.next();
	 * 
	 *           } while(input1.hasMore() && !isSameGroup(lastRecord));
	 * 
	 *           // copy over missing values for (int i=0; i<this.nDiffAttributes; i++) { Symbol<?> f1 =
	 *           this.diffAttributes.get(i); this.set(f1.name, f1.value); }
	 * 
	 *           }
	 * 
	 *           private boolean isSameGroup(DatasetRecord oldRecord) { if (oldRecord == null) return true; for (int
	 *           i=0; i<this.nAttributes; i++) { String f1 = this.primaryKey.get(i); String f2 = this.fieldList.get(i);
	 * 
	 *           Object v1 = input1.get(f1).value; Object v2 = oldRecord.get(f2).value;
	 * 
	 *           if ( v1==null && v1!=v2 || v1!=null && !v1.equals(v2) ) { //System. out.println(v1+"!="+v2); return
	 *           false; } } return true; }
	 **/
}
