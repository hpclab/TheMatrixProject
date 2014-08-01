package it.cnr.isti.thematrix.scripting.tests;

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
/* TheMatrix imports follow */
import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.MappingSingleton;


/**
 * Classe di test ausiliaria per il solo script LoadPERSON.txt 
 * 
 * The class supporting file input relies on many classes developed when extending Jerboa into TheMatrix. The corresponding initialization
 * code must be called before we call the interpreter. For the moment we put it here.
 * 
 * @author edoardovacchi, massimo
 */
public class SortOUTPAT extends MatrixBaseTestCase {

	@Test
	public void testScript0() throws IOException {
		// must be there -- this is a test class, its static init cannot rely on _any_ of the Singletons
		final String src = Dynamic.getScriptPath()+"SortOUTPAT.txt";

		//init the logging system, maximum detail		
		LogST.getInstance().enable(2);
		LogST.getInstance().startupMessage();

		System.out.println("testing file: " + src);
		/* old output -- remove		
		TheMatrixSys.eval(FileUtils.fileToString(src));
		// cycle to read all the data and do nothing
		int i=0;
		MatrixModule m = TheMatrixSys.getModule("PERSONFile");
		while (m.hasMore())
		{
			m.next();
			if (i%1024==0) {
				System.out.println(""+i+"::"+m.attributes());
			}
			i++;
		}

		/////
		System.out.println(""+i+"::"+m.attributes());
		System.out.println("testing file: " + src + " has read " + i + "lines");
*/

//		TheMatrixSys.setParams(new Symbol<Date>("DATE",DateUtil.parse("2011-01-01"),DataType.DATE)); // no params in this test
        TheMatrixSys.eval(FileUtils.fileToString(src));

        MatrixModule m = TheMatrixSys.getCurrentModule();
        MatrixModule out = new MatrixFileOutput("SCRIPTSORTOUTPAT_TEST", m);
        out.setup();
        int i=0;
        while (out.hasMore()) { out.next(); i++;}
        System.out.println(i);

	
	}

}
