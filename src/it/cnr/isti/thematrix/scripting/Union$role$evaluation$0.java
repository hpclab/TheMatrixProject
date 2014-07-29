package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import java.util.List;
import neverlang.utils.*;

public class Union$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

       String inputTable1  = n.ntchild(1).getValue("moduleId"); 
       String schemaName1  = n.ntchild(1).getValue("schemaName"); 
       String inputTable2  = n.ntchild(2).getValue("moduleId"); 
       String schemaName2  = n.ntchild(2).getValue("schemaName"); 


       MatrixModule m = new MatrixUnion(n.ntchild(0).getValue("moduleId").toString(), inputTable1, schemaName1, inputTable2, schemaName2);
       n.setValue("moduleContents",  m);
       TheMatrixSys.addModule(m);
       m.setup();
    
  }
}