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
import java.util.Date;
import junit.framework.TestCase;
import neverlang.utils.FileUtils;
import org.junit.Test;

/**
 *
 * @author edoardovacchi
 */
public class ScriptDiabetes extends MatrixBaseTestCase  {
    
    
    @Test
    public void testScript0() throws IOException {
        final String src = Dynamic.getScriptPath() + "/script_diabetici_draft.txt";

    	System.err.println("testing file: "+src);
        TheMatrixSys.eval(FileUtils.fileToString(src));
        
        MatrixModule m = TheMatrixSys.getCurrentModule();

        int i =0;
        while(m.hasMore() && i < 20) {
        	m.next();
        	i++;
        	System.out.println(m.attributes());
        }
    }
    
    
}
