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
 * @author carlini
 */
public class MatrixDrop extends MatrixModule
{
	private static final long serialVersionUID = 2841968905963439793L;

	public MatrixModule input;
	
	private List<String> fieldNames; 
	private DatasetRecord buffer;
	private boolean computed = false;
	
    public MatrixDrop(String name, String inputTable, String schemaName, List<String> fieldNames)
    {
    	super(name);
    	this.input = TheMatrixSys.getModule(inputTable);
    	this.fieldNames = fieldNames;
        input.schemaMatches(schemaName);
        input.addConsumer(this);
	}
  	
	@Override
	public void setup() 
	{
		// remove from the schema the fields we dont want 
		List<Symbol<?>> difference = new ArrayList<Symbol<?>>();
		
		for (Symbol<?> s : input.getSchema().attributes()) 
		{
			if (fieldNames.contains(s.name) == false)
				difference.add(s);
		}
		
		// create the new schema
        DatasetSchema newSchema = new DatasetSchema("Drop"+input.name+"$custom");  
        for (Symbol<?> s : difference) 
        {
            Symbol<?> newS = s.clone();
            newSchema.put(newS);
        }     
        this.setSchema(newSchema);
        
        /*
        System.out.println("--------------------------------> "+input.getSchema());
        System.out.println("--------------------------------> "+newSchema);
        System.out.println("--------------------------------> "+this.getSchema().attributes());
        */
	}
	
	
	public void changeInput (MatrixModule m)
    {		
		this.input.schemaMatches(m.getSchema().name); 
		this.input = m;
    }
    

	@Override
	public void exec()
	{
		LogST.logP(2,"NO-OP EXEC "+this.name);	
	}

	@Override
	public void reset()
	{
		throw new UnsupportedOperationException("Not supported yet.");	
	}
	
	@Override
	public boolean hasMore() 
	{
		if (computed)
		{
			LogST.logP(3, "MatrixDrop.hasMore() called again after end of input for module "+this.name);
			return true;
		}
		
		if (input.hasMore())
		{
			// read the line
			input.next();
			
			DatasetRecord record = DatasetRecord.emptyRecord(this.input);
			record.setAll(input);
						
			// save only the stuff we want
			buffer = DatasetRecord.emptyRecord(this);
			for (Symbol<?> item : record.getSchema().attributes())
			{				
				if (fieldNames.contains(item.name) == false)
					buffer.set(item.name, record.get(item.getName()).value);
			}
			
			computed = true;
			return true;
		}
		else
			return false;
	}

	@Override
	public void next() 
	{		
		this.setAll(buffer);
		computed = false;
	}

}
