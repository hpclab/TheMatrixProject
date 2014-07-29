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

public class MergeModule$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

       String inputTable1  = n.ntchild(1).getValue("moduleId"); 
       String schemaName1  = n.ntchild(1).getValue("schemaName"); 
       String inputTable2  = n.ntchild(2).getValue("moduleId"); 
       String schemaName2  = n.ntchild(2).getValue("schemaName"); 

       List<String> primaryKey = AttributeList.collectFrom(n.ntchild(3), "columnId");
       List<String> fieldNames = AttributeList.collectFrom(n.ntchild(4), "columnId");
       MatrixModule m = new MatrixMerge(
          n.ntchild(0).getValue("moduleId").toString(), inputTable1, schemaName1, inputTable2, schemaName2, 
          primaryKey, fieldNames);
       n.setValue("moduleContents",  m);
       TheMatrixSys.addModule(m);
       m.setup();
    
  }
}