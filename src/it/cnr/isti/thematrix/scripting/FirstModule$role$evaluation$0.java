package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.List;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;
import it.cnr.isti.thematrix.scripting.modules.*;

public class FirstModule$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

       String inputTable  = n.ntchild(1).getValue("moduleId"); 
       String schemaName  = n.ntchild(1).getValue("schemaName"); 

       List<String> fieldNames = AttributeList.collectFrom(n.ntchild(2), "columnId");
       MatrixModule m = new MatrixFirst(n.ntchild(0).getValue("moduleId").toString(), inputTable, schemaName, fieldNames);
       n.setValue("moduleContents",  m);
       TheMatrixSys.addModule(m);
       m.setup();
    
  }
}