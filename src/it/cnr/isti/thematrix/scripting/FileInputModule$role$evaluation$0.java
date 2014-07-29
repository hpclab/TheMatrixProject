package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import java.util.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;

public class FileInputModule$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

  		/*n.setValue("moduleContents",  String.format( 
  			"FileInputModule named '%s'\n with from file: '%s'\nordered by %s",
  			n.ntchild(0).getValue("moduleId"),
  			n.ntchild(1).getValue("outputFileName"),
        n.ntchild(1).getValue("orderByList")));*/

        String moduleName = n.ntchild(0).getValue("moduleId");
        //TheMatrixSys.setCurrentDataset(datasetName);
      MatrixModule m = new MatrixFileInput(moduleName, (String)n.ntchild(1).getValue("inputFileName"), (String)n.ntchild(1).getValue("inputSchema"), (List<String>)n.ntchild(1).getValue("orderByList"));
      TheMatrixSys.addModule(m);
      m.setup();
      n.setValue("moduleContents",  m);

  	
  }
}