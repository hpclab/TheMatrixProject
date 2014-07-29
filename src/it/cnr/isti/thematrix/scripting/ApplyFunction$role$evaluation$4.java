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

public class ApplyFunction$role$evaluation$4 implements SemanticAction {
  public void apply(ASTNode n) {

      n.setValue("functionName",  n.ntchild(0).getValue("simpleId").toString());
      List<Symbol<?>> valueList = AttributeList.collectFrom(n.ntchild(1), "value");
      n.setValue("valueList",  valueList);
      n.setValue("result",  TheMatrixSys.getCurrentModule().get(n.ntchild(2).getValue("columnId").toString()));
    
  }
}