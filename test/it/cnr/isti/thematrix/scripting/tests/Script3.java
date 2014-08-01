/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.tests;

import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;

import java.io.File;
import java.io.IOException;

import neverlang.utils.FileUtils;

import org.junit.Test;

/**
 *
 * @author edoardovacchi
 */
public class Script3 extends MatrixBaseTestCase  {
    
    final String src = new File(".").getAbsolutePath() + "/script3.txt";
    
    @Test
    public void testScript1() throws IOException {
        System.err.println("testing file: "+src);
        //fail("Script3 is unfinished");
        TheMatrixSys.eval(FileUtils.fileToString(src));
    }
    
}
