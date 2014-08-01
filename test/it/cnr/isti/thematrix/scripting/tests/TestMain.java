/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.tests;

import it.cnr.isti.thematrix.scripting.filtermodule.support.MatchesFilterCondition;
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
public class TestMain extends MatrixBaseTestCase  {
    
    final String src = new File(".").getAbsolutePath() + "/main_sperimentazioneMATRICE.txt";
    
    @Test
    public void testScript0() throws IOException {
        System.err.println("testing file: "+src);
        TheMatrixSys.eval(FileUtils.fileToString(src));
    }
    
}
