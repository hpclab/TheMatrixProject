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
public class Script0 extends MatrixBaseTestCase  {
    
    final String src = new File(".").getAbsolutePath() + "/script0.txt";
    
    @Test
    public void testScript0() throws IOException {
        System.err.println("testing file: "+src);
        TheMatrixSys.eval(FileUtils.fileToString(src));
    }
    
}
