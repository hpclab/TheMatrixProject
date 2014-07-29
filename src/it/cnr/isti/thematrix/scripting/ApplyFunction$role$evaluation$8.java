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

public class ApplyFunction$role$evaluation$8 implements SemanticAction {
  public void apply(ASTNode n) {

      n.setValue("filters",  DummyFilterCondition.INSTANCE);
      // pass stuff on
      n.setValue("functionName",  n.ntchild(0).getValue("functionName")); 
      n.setValue("valueList",  n.ntchild(0).getValue("valueList"));
      n.setValue("result",  n.ntchild(0).getValue("result"));

    
  }
}