/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.tests;

import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;
import it.cnr.isti.thematrix.scripting.filtermodule.support.MatchesFilterCondition;
import it.cnr.isti.thematrix.scripting.modules.MatrixFileOutput;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import neverlang.utils.FileUtils;
import org.junit.Test;

/**
 *
 * @author edoardovacchi
 */
public class ScriptEmoGlic extends MatrixBaseTestCase  {
    
    
    @Test
    public void testScript0() throws IOException {
        final String src = Dynamic.getScriptPath() + "/script_draft_emo_glic.txt";

		LogST.getInstance().enable(5);
		LogST.getInstance().startupMessage();
        
    	System.err.println("testing file: "+src);

    	TheMatrixSys.setParams(
    			new Symbol<Date>("DATESTARTFUP", DateUtil.parse("2011-01-01"),DataType.DATE),
    			new Symbol<Date>("DATEENDFUP",   DateUtil.parse("2011-12-31"),DataType.DATE));
    	
        TheMatrixSys.eval(FileUtils.fileToString(src));
        MatrixModule m = TheMatrixSys.getCurrentModule();
        MatrixModule out = new MatrixFileOutput("SCRIPTEMOGLIC_TEST", m);
        out.setup();
        int i =0;
        while (out.hasMore()) { 
        	out.next(); i++; 
        	System.out.println(out.attributes());
        }
       
        System.out.println(i);
        System.out.println(out.attributes());
    }
    
    public void oldCode() {
        
     // output to a temp file
        CSVFile outputCSV=null;
        if (true){
        	//init
        	outputCSV=new CSVFile(Dynamic.getIadPath(),"SCRIPTEMOGLOC_TEST","");

        	// create a header for the file starting from the schema
        	List<Symbol<?>> schema = TheMatrixSys.getCurrentModule().getSchema().attributes();
        	Collection<String> newHeader = new ArrayList<String>();
        	for (Symbol<?> field: schema)
        	{
        		newHeader.add(field.name);
        	}
        	outputCSV.setHeader(newHeader);
			/*
			 * FIXME current version of the header does not save column type information; this is needed if we want to
			 * avoid having to fully specify all the schemata in all the scripts (poor maintainability) and if we
			 * want (in the future) provide automatic column selection based on name and type information (original plan).
			 */
        }


        // FIXME we need to add an iterator to the CSVFile to avoid reimplementing this pattern over and over. 
        MatrixModule m = TheMatrixSys.getCurrentModule();
        // int i = 0;
        // the number of the current batch in writing the file 
        int iterationCount=0;
        int rowsTotal=0;
        while (m.hasMore()) {
            int rowsInBatch=0;
        	// prepare one batch of rows; I am assuming the order is kept the same as in the Schema
			while (rowsInBatch < Dynamic.prefetchCSVSize && m.hasMore()) { 
				// prepare and store one row of data
				m.next(); // this may not be correct, with current implementation I think it skips the first row.
				int columnCount = 0;
				for (Symbol<?> field : m.attributes()) {
					outputCSV.setValue(columnCount, StringUtil.symbolToString(field));
					columnCount++;
					//maybe add some checks here? if one column skips we mangle all the data
				}
				rowsInBatch++;	// the step to next row in outputCSV is implicit				
			}
        	LogST.logP(2, "ScriptCoorte - CSV output - done batch " + iterationCount + " with " + rowsInBatch + "rows");
        	try {
        		outputCSV.save(iterationCount > 0); // truncate file at the first iteration <=> append only after
        	} catch (Exception e) {
        		LogST.logP(0,"ScriptCoorte - CSV output - Unexpected Exception"+e); 
        		throw new Error("ScriptCoorte - CSV output - Unexpected Exception"+e);
        	};
        	
        	iterationCount++;
        	rowsTotal+=rowsInBatch;	
        }
        LogST.logP(1, "ScriptEmoGlic - CSV output complete, totaling " + rowsTotal + " rows");
    }
    
    
}
