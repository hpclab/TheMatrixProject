/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.tests;

import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;

import java.io.IOException;

import neverlang.utils.FileUtils;

import org.junit.Test;

/**
 *
 * @author edoardovacchi
 */
public class ScriptCI extends MatrixBaseTestCase  {
    
    
    @Test
    public void testCI() throws IOException {
        final String src = Dynamic.getScriptPath() + "/script_CI_draft.txt";

    	System.err.println("testing file: "+src);
        //fail("Script3 is unfinished");
        TheMatrixSys.eval(FileUtils.fileToString(src));
    }
    
}
