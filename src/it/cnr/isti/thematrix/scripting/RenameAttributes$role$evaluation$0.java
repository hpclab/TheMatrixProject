package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import java.util.List;
import neverlang.utils.*;

public class RenameAttributes$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

       String inputTable  = n.ntchild(1).getValue("moduleId"); 
       String schemaName  = n.ntchild(1).getValue("schemaName"); 
       List<String> inAttr = AttributeList.collectFrom(n.ntchild(2), "columnId");
       List<String> outAttr = AttributeList.collectFrom(n.ntchild(3), "columnId");
       
       MatrixModule m = new MatrixRenameAttributes(n.ntchild(0).getValue("moduleId").toString(), inputTable, schemaName, inAttr, outAttr);
       n.setValue("moduleContents",  m);
       TheMatrixSys.addModule(m);
       m.setup();
    
  }
}