/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.tests;

import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.scripting.filtermodule.support.MatchesFilterCondition;
import it.cnr.isti.thematrix.scripting.modules.MatrixFileOutput;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Collection;
import junit.framework.TestCase;
import neverlang.utils.FileUtils;
import org.junit.Test;
/* TheMatrix imports follow */
import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.MappingSingleton;
import it.cnr.isti.thematrix.mapping.MappingManager;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;;


/**
 *
 * @author edoardovacchi
 */
public class ScriptCoorte extends MatrixBaseTestCase  {
    
    
    @Test
    public void testScript0() throws IOException {
		//init the logging system, maximum detail		
		LogST.getInstance().enable(3);
		LogST.getInstance().startupMessage();

    	final String src = Dynamic.getScriptPath() + "/script_coorte_draft.txt";

    	System.err.println("testing file: "+src);
    	TheMatrixSys.setParams(new Symbol<Date>("DATE",DateUtil.parse("2011-01-01"),DataType.DATE));
    	
        TheMatrixSys.eval(FileUtils.fileToString(src));
        MatrixModule m = TheMatrixSys.getCurrentModule();
        
        MatrixModule out = new MatrixFileOutput("__SCRIPT_COORTE_TEST", m);
        out.setup();
        int i=0;
        while (out.hasMore()) { out.next(); i++;}
        System.out.println(i);
    }
	
    //        System.out.println("COUNT="+i);
}