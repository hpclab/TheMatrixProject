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

public class ApplyFunction$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

       String inputTable  = n.ntchild(1).getValue("moduleId"); 
       String schemaName  = n.ntchild(1).getValue("schemaName"); 
       
       String functionName = n.ntchild(2).getValue("functionName");
       List<Symbol<?>> args = n.ntchild(2).getValue("valueList");
       Symbol<?> result   = n.ntchild(2).getValue("result");
       FilterCondition fc = n.ntchild(2).getValue("filters");


       MatrixModule m = new MatrixApplyFunction(n.ntchild(0).getValue("moduleId").toString(), inputTable, schemaName, functionName, args, result, fc);
       n.setValue("moduleContents",  m);
       TheMatrixSys.addModule(m);
       m.setup();
    
  }
}