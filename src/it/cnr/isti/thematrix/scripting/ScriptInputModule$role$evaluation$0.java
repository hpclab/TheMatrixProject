package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.*;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;
import it.cnr.isti.thematrix.scripting.modules.*;

public class ScriptInputModule$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

       String scriptName  = n.ntchild(1).getValue("fileNameId"); 
       List<Symbol<?>> params = n.ntchild(1).getValue("params") ;
       String inputName = n.ntchild(2).getValue("moduleId");
       String schema = n.ntchild(3).getValue("schemaName");
       
       MatrixModule m = new MatrixScriptInput(n.ntchild(0).getValue("moduleId").toString(), scriptName, params, inputName, schema);
       n.setValue("moduleContents",  m);
       TheMatrixSys.addModule(m);
       m.setup();
    
  }
}