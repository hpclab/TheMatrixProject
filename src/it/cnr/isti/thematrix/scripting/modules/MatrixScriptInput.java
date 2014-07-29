package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.meta.CsvDescriptor;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;
import it.cnr.isti.thematrix.mapping.utils.TempFileManager;
import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import dexter.parser.UnexpectedSymbolException;

/**
 * This class implements the behavior of nested script.
 * The logic is simple: if a file with "inputName" exists,
 * then the nested script is resolved as a FileInputModule.
 * If the file does not exist, the class execute the scripts 
 * and search for FileOutputModule with the "inputName" name
 * inside the script. It links such module to the main script.
 *
 * Conditions:
 * - there can be an arbitrary FileOutputModule in a nested script
 * - the name of one of the FileOutputModule in the nested script must be the same
 * then the inputName.
 *
 * @author edoardovacchi, carlini
 */
public class MatrixScriptInput extends MatrixModule 
{
	private static final long serialVersionUID = -3345679747323500719L;

	private String scriptName;
    private List<Symbol<?>> params;
    private String inputName;
    private String expectedSchema;

    public MatrixScriptInput(String name, String scriptName, List<Symbol<?>> params, String inputName, String expectedSchema) 
    {
        super(name);
        this.scriptName = scriptName;
        this.params = (params);
        this.inputName = inputName;
        this.expectedSchema = expectedSchema;
    }
    
    @Override
    public void setup() 
    {        
        // setup the schema of this module.
    	// NOTE: currently, the "custom" schema is inserted only if fullIAD is 0.
    	// 		in the other cases, it is not inserted and crashes
    	// this.setSchema(TheMatrixSys.getPredefinedSchema(this.expectedSchema));
        
        // this is the module that will be linked in the caller script
        MatrixModule result = null;
        
        // check whether the data file exists or not, considering xml
        boolean ignoreXML = false;
        File path = TempFileManager.getPathForFile(this.inputName+".csv");        
        boolean exists = CSVFile.checkExistence(path.getAbsolutePath()+"/", this.inputName, ignoreXML);
        
        LogST.logP(1,"MatrixScriptInput: checking for existence "+this.inputName+".csv --> "+exists);
        
        // if errors when reading the file
        boolean error = false;
        
        if (exists)
        {
        	try
        	{
        		result = in_case_file_exists();
        	}
        	catch (Exception e) 
        	{
        		e.printStackTrace();
        		LogST.logP(0, "\tWARNING MatrixScriptInput ("+this.name+"): .csv or .xml is corrupted o uncomplete for "+this.inputName+"; Script will be executed.");
        		error = true;
        	}
        }
        	
        // if error happened or file does not exist, execute the script
        if (error || exists == false) {
        	result = in_case_file_not_exists();
        
			/*
			 * FIXME check what follows: since we are substituting the
			 * ScriptInput with a FileOutput, the File Output becomes visible at
			 * the outer script level; this has the unwanted side effect of
			 * letting the outer output routine draw output from the inner
			 * script too (or _instead_ of the main script, if it happens to
			 * miss a fileoutput). I patch it by marking this FileOutput as a
			 * postprocessed module, so that the output routine will ignore it.
			 */
			((MatrixFileOutput) result).setPostProcessed();
        }
        
        // substitute the module in the main script
        TheMatrixSys.substituteModule(this.name, result);
        

    }


    /**
     * If file exists, the result module is simply a FileInputModule
     * loading the file. No script will be executed.
     * @return 
     */
    private MatrixModule in_case_file_exists() throws Exception
    {    	
    	// here we are sure that the .xml file does exist
    	// and we read the schema from it
    	DatasetSchema targetSchema = null;

    	File path = TempFileManager.getPathForFile(this.inputName+".csv");
    	
		String xml_name = path.getAbsolutePath()+"/" + this.inputName + ".xml";
		JAXBContext context = JAXBContext.newInstance(CsvDescriptor.class);
		Unmarshaller u = context.createUnmarshaller();
		CsvDescriptor descriptor = (CsvDescriptor)u.unmarshal(new File(xml_name));
		targetSchema = DatasetSchema.fromJSON(descriptor.getJsonDescription());

    	    	
    	// create the file input
        MatrixFileInput mfi = new MatrixFileInput(this.name, // gets the name of the InputScriptModule
        		inputName+".csv",  // i'm searching for the .csv file
        		targetSchema, // schema will be the one read above
        		new ArrayList<String>()); // no order-by in this case
        
        mfi.setup();
        
        // check against the expected schema
        mfi.schemaMatches(this.expectedSchema);
        
        return mfi;
    }
    
    /**
     * Evaluates the nested script and substitute the InputModule with 
     * the FileOutputModule in the nested script.
     * @return
     */
    private MatrixModule in_case_file_not_exists()
    {
    	LogST.logP(1,"MatrixScriptInput: executing "+scriptName);
    	
    	// Save the state of the current interpreter and prepare a fresh interpreter
        TheMatrixSys.saveState();
        
        // NOT SUPPORTED? load arguments as list of strings 
        // TheMatrixSys.setArguments(params);
        
        // set parameters list
        TheMatrixSys.setParamList(params);
        
        MatrixModule result = null;
        try 
        {       
            // execute the fresh interpreter on the provided script
            TheMatrixSys.evalFile(Dynamic.getScriptPath()+scriptName);
            
			/***
			 *  FIXME 14/1/2014 put here the stub for post-processing the nested
			 *  graph (adding buffers and the like)
			 */
            // TheMatrixSys.performModuleGraphPostProcessing(scriptName);
            
            // check if something is wrong
            checkForError();
            
            // get the result (the inputName module)
            result = TheMatrixSys.getModule(this.inputName);
            
			/**
			 * FIXME 14/01/2014 here we should save what we may need to reuse in
			 * order to support calling again the same nested script without
			 * rebuilding a duplicate of the script graph; basically you can
			 * cache the restoreState() output.
			 * 
			 * Use case: A calls B many times with different parameter values
			 * and/or calling different outputs of B.
			 * 
			 * Desired effect 1: when B is called again with same parameters,
			 * but a different output is needed, we shall trace back to the
			 * right subscript graph and link to the right output; this link
			 * needs to be be made available to the postprocessing routine at
			 * the outer level (e.g. to add buffers when needed)
			 * 
			 * Desired effect 2: when B is called again with different
			 * parameters, we need to rebuild the script graph separately as it
			 * will perform a different computation; note that (i) we need to
			 * disambiguate this situation at the upper level as we are
			 * potentially linking different nodes with the same name in the
			 * upper level graph, this may not work and can be a mess to debug;
			 * (ii) this call must be also available for a future effect 1)
			 * 
			 * Desired side effect: all outputs of B which are not called are
			 * passed to the upper interpreter, and if they are not used they
			 * are eventually gathered and made available to the output pulling
			 * routine at the main script. This ensures that ALL outputs are
			 * pulled from B and all its outputs are eventually written to disk.
			 * 
			 * Essential assumption: the file name is not only the fileoutput
			 * name but also encodes all parameters (hence caching needs a
			 * better support) so that by opening the file we can check what
			 * content it is expected to hold
			 * 
			 * Schema of the solution
			 * 
			 * 1) define an encoding of parameters as a hash in the name, add
			 * parameters inside the xml descriptor
			 * 
			 * 2) apply that encoding in the output and in the check in
			 * scriptinput
			 * 
			 * 3) identify the special cases 1 and 2 and treat them separately
			 * 
			 * * new nested interpreter call:
			 * 
			 * call the eval()
			 * 
			 * save <scriptname, parameters> outputname interpreter into a
			 * key-value KV1 store linked to the upper interpreter, where the
			 * first couple is the key; (KV1 holds the already used outputs)
			 * 
			 * save the unused outputs in a similar keyvalue store KV2 linked to
			 * the upper interpreter; (KV2 holds the still unused but available
			 * outputs)
			 * 
			 * possibly alter the output name to avoid ambiguities; add the
			 * output module to the upper level graph and pop interpreter stack
			 * 
			 * * identify and deal with effect 1) here
			 * 
			 * if <B, output> is in KV1 or in KV2
			 * 
			 * if found in KV2, move entry to KV1; if found in KV1, link it
			 * (modifying the name if needed to avoid clashes);
			 * 
			 * * identify and deal with effect 2) here
			 * 
			 * if B is not in KV1 or in KV2
			 * 
			 * perform nested interpreter call
			 * 
			 * * deal with KV1 and KV2 at the outer level
			 * 
			 * KV1 and KV2 must be gathered as global; no direct or indirect
			 * recursion is allowed via ScriptInput (we may use a stack to
			 * detect this, but we just say semantics is undefined)
			 * 
			 * KV1 is passed along; what is left in KV2 is to be pulled (as
			 * secondary output) in the main output routine (I'm not sure is
			 * order may become relevant here)
			 */

            // restore state of the current interpreter
            TheMatrixSys.restoreState();
            
            // verifies the result of the evaluation to match the provided schema            
            result.schemaMatches(expectedSchema);
            result.name = this.name;
                                    
            LogST.logP(1,"MatrixScriptInput: finished "+scriptName);
            
        } 
        catch (IOException e) {
            LogST.logP(-1,"-- ERROR MatrixScriptInput -- Script: "+scriptName+" cannot be found.");
            e.printStackTrace();
        } 
        catch (UnexpectedSymbolException e) {
            LogST.logP(-1,"-- ERROR MatrixScriptInput -- evaluating script: "+scriptName);
            e.printStackTrace();
        }
        
        return result;
    }
    
    
    /**
     * FIXME - document method!
     */
    private void checkForError()
    {
    	
		List<MatrixFileOutput> list = TheMatrixSys.getUserOutputModules(true);
		
		// are there output modules at all?
		boolean atLeastOne = list.size() > 0;
		 
		// is this.inputName matches with an output module?
		boolean nameMatch = false;
		 
		for (MatrixModule m: list)
		{
			if (m.name.equals(this.inputName))
			{
				nameMatch = true;
				break;
			}
		} 
		
		// manage the errors
		if (atLeastOne == false || nameMatch == false)
		{
			LogST.logP(0, "ERROR in ScriptInput "+this.name+"\n\tFILEOUTPUT PRESENT: "+atLeastOne+"\n\tNAME MATCHING: "+nameMatch);
			throw new RuntimeException ("ERROR in ScriptInput "+this.name+", see logs");
		}
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
    public boolean hasMore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void next() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
