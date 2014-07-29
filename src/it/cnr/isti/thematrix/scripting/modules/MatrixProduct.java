package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.filtermodule.support.FilterCondition;
import it.cnr.isti.thematrix.scripting.filtermodule.support.FilterSequence;
import it.cnr.isti.thematrix.scripting.productmodule.support.ProductFilterCondition;
import it.cnr.isti.thematrix.scripting.sys.DatasetRecord;
import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import it.cnr.isti.thematrix.scripting.utils.DataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the Product module.
 * This class reads in memory two blocks and execute the join and the filter.
 * If the two blocks are not synch (i.e. the value of their keyField is different) 
 * they are synchronized automatically. 
 * 
 * Functionalities
 * - supports multiple functions condition
 * 
 * Assumptions / Limitations:
 * - the two dataset share the common key
 * - both datasests are ordered by key (it doesn't check)
 * - the datasets do not share any attribute in common (unknown beahviour in this case). 
 * - the name of fields in conditions must be indicated as FILE.IDFIELD
 * 
 * @author edoardovacchi, carlini
 */
public class MatrixProduct extends MatrixModule
{
	private static final long serialVersionUID = 8651879394346535769L;
	
	public MatrixModule input1;
    public MatrixModule input2;
    private final String idField;
    private FilterCondition filter;
    
    // the list of attributes that need to be added at the input1 to perform a join
    private List<Symbol<?>> difference = new ArrayList<Symbol<?>>();
    
    
    private List<DatasetRecord> buffer;
    private BlockReader br1;
    private BlockReader br2;
    
	
    public MatrixProduct(String name, String module1, String schema1, 
			  String module2, String schema2, String idField)
    { 
	        super(name); 
	        this.input1 = TheMatrixSys.getModule(module1);
	        input1.schemaMatches(schema1);
	        this.input1.addConsumer(this);
	        this.input2 = TheMatrixSys.getModule(module2);
	        input2.schemaMatches(schema2);
	        this.input2.addConsumer(this);
	        this.idField = idField;
	}
    
    
    public void setFilters(FilterCondition fc) {
    	LogST.logP(3, "MatrixProduct.setFilters() "+fc.toString());
        this.filter = fc;
    }
    
	@Override
	public void setup() 
	{
		// init buffers
		this.buffer = new ArrayList<DatasetRecord>();	
		this.br1 = new BlockReader(input1, this.idField);
		this.br2 = new BlockReader(input2, this.idField);		
		
		// checking if idField is contained in both inputs
		if (input1.getSchema().fieldNames().contains(idField) == false ||
			input2.getSchema().fieldNames().contains(idField) == false)	
		{
			throw new Error("MatrixProduct.setup(): the schemata "+input1.getSchema().name+" and "+input2.getSchema().name+" do not have "+idField+" in common.");
		}
			
        // create the difference in schema, and check if they have attributes with the same name
		for (Symbol<?> s : input2.getSchema().attributes()) 
		{
			if (input1.getSchema().fieldNames().contains(s.name) == false)
				difference.add(s);
			else
			{
				if (s.name.equals(this.idField) == false)
				{
					throw new Error("MatrixProduct.setup(): Schemata "+input1.getSchema().name+" and "+input2.getSchema().name+
						" have the field "+s+" in common. Please, consider the RENAME ATTRIBUTES module.");
				}
			}
		}
			
		// creating the new schema
        DatasetSchema newSchema = new DatasetSchema("Product"+input1.name+"+"+input2.name+"$custom");
       
        for (Symbol<?> s : this.input1.getSchema().attributes()) 
        {
            Symbol<?> newS = s.clone();
            newSchema.put(newS);
        }
        
        for (Symbol<?> s: difference)
        {
            Symbol<?> newS = s.clone();
            newSchema.put(newS);
        }
        
        this.setSchema(newSchema);
        
        // setup the filter with the new schema
        this.filter.changeInput(this);
        
        LogST.logP(3, "MatrixProduct.setup()" + newSchema);
	}

	@Override
	public boolean hasOneInput()
    {
    	return false;
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
    	
    	// recreate the proper block readers
		this.br1 = new BlockReader(input1, this.idField);
		this.br2 = new BlockReader(input2, this.idField);	
    }

	@Override
	public void exec() {
		LogST.logP(2,"NO-OP EXEC "+this.name);
	}

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
	
   @Override
    public String toString() {
        return String.format(
           "ProductModule named '%s'\n with parameters:\n  %s\n  %s\n\n",
            name,
            input1.name, input2.name
         
       );    
    }

	@Override
	public boolean hasMore() 
	{
		// if more hasMore() are called without next() this method 
		// just return true without entering the while
		
		while (buffer.size() == 0)
		{
			Block[] blocks = getBlocks();
			
			if (blocks == null) // then I'm done
				return false;
			
			
			// else execute the conditional join
			this.buffer = conditionalJoin(blocks[0], blocks[1]);
			
		}
		
		return true;		
	}

	@Override
	public void next() 
	{
		DatasetRecord result = buffer.remove(0);
		this.setAll(result);
	}
	
	private List<DatasetRecord> conditionalJoin(Block a, Block b)
	{
		long rows = a.size() * b.size();
		
		if (rows > 10000)
			LogST.logP(3, "MatrixProduct: WARNING: about to execute a join that will results in "+rows+" rows");
		
		
		ArrayList<DatasetRecord> list = new ArrayList<DatasetRecord>();
		
		for (DatasetRecord d1 : a)
		{
			for (DatasetRecord d2: b)
			{
				DatasetRecord joined = this.join(d1, d2);
				
				// hack: force update of the filter with fresh values
				if (this.filter instanceof ProductFilterCondition) 
				{
					((ProductFilterCondition)this.filter).setDatasetRecord(joined);					
				}
				else if (this.filter instanceof FilterSequence)
				{
					for (FilterCondition fc: (FilterSequence)this.filter)
					{
						((ProductFilterCondition)fc).setDatasetRecord(joined);
					}
				}
				
				// if the join is good, add it to the list
				if (filter.apply())
					list.add(joined);
			}
		}
		return list;
	}
	
	private DatasetRecord join(DatasetRecord d1, DatasetRecord d2)
	{
		// get a new dataset with this module schema
		DatasetRecord toReturn = DatasetRecord.emptyRecord(this);
		
		// fill it up with the first DR
		toReturn.setAll(d1);
		
		// fill it up with the difference 
		for (Symbol<?> s : difference)
			toReturn.set(s.name, d2.get(s.name).getValue());
		
		return toReturn;
	}
	
	private Block[] getBlocks()
	{
		// LogST.logP(0, "MatrixProduct.getBlocks()");

		Block b1 = br1.readBlock();
		Block b2 = br2.readBlock();
		
		/*
		System.out.println("B1: "+b1+" input: "+input1.name);
		System.out.println("B2: "+b2+" input: "+input2.name);
		System.out.println(b1.compareTo(b2));
		*/
		
		// synchronize blocks
		while (b1.compareTo(b2) != 0)
		{
			if (b1.compareTo(b2) > 0)
			{
				if (b2.isEmpty())
					return null;
				else
					b2 = br2.readBlock();
			}
			else
			{
				if (b1.isEmpty())
					return null;
				else
					b1 = br1.readBlock();
			}
		}
		
		// here blocks are synchronized
		if (b1.isEmpty() && b2.isEmpty())
			return null;
		else
			return new Block[]{b1,b2};
	}

}


class BlockReader
{
	private MatrixModule input;
	
	private Block current_block; 
	private Block ahead_block;
	private String keyField;
	
	public BlockReader(MatrixModule input, String keyField)
	{
		this.input = input;
		this.current_block = new Block(keyField);
		this.ahead_block = new Block(keyField);
		this.keyField = keyField;
	}
	
	public Block readBlock()
	{
		boolean stay = true; // should i read another line?
		
		current_block = ahead_block;
		ahead_block = new Block(keyField);
		
		while (input.hasMore() && stay)
		{
			input.next();
			DatasetRecord record = DatasetRecord.emptyRecord(this.input);
			record.setAll(input);
						
			if (current_block.support(record))
				current_block.add(record);
			else
			{
				ahead_block.add(record);
				stay = false;
			}
		}
		
		return current_block;
			
	}
}

class Block extends ArrayList<DatasetRecord> implements Comparable<Object>
{
	private static final long serialVersionUID = 5884165246293327544L;

	private String keyField;
	
	public Block(String keyField)
	{
		this.keyField = keyField;
	}
	
	/**
	 * Returns true if the record belongs to the block
	 * @param record
	 * 
	 */
	public boolean support(DatasetRecord record)
	{
		if (this.size() == 0)
			return true;
		else
		{
			DatasetRecord sample = this.get(0);
			
			if (sample.get(keyField).value == null || record.get(keyField).value == null)
				return false;
			
			if (sample.get(keyField).value.equals(record.get(keyField).value))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Return the key value of the block (the index of the db)
	 * 
	 */
	public Object getKeyValue()
	{
		if (this.size() == 0)
			return null;
		else
		{
			return this.get(0).get(keyField).value;
		}
	}

	/**
	 * Compare two blocks, according the lexicographical order of their keys
	 */
	@Override
	public int compareTo(Object other) 
	{
		if (other == null)
			return 1;
		if ((other instanceof Block) == false)
			return 1;
		
		Block other_block = (Block) other;
		
		if (other_block.size() == 0 && this.size() == 0)
			return 0;
		else if (other_block.size() == 0)
			return 1;
		else if (this.size() == 0)
			return -1;
			
		// -- Here i am sure the blocks are not null and with data --//
		
		// get the first line of each block
		DatasetRecord dr1 = this.get(0);
		DatasetRecord dr2 = other_block.get(0);
		
		// get the type (here I assume they are the same) 
		DataType type = (DataType)dr1.get(keyField).type;
		
		// get the actual data
		Comparable<Object> v1 = (Comparable<Object>) dr1.get(keyField).value;
		Comparable<Object> v2 = (Comparable<Object>) dr2.get(keyField).value;
		
		return v1.compareTo(v2);
	}

	@Override
	public String toString()
	{
		return "["+this.keyField+"] ["+this.size()+"]";
	}

	public String verbose()
	{
		return toString() + super.toString();
	}
}
