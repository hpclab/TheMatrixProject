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

public class ApplyFunction$role$evaluation$10 implements SemanticAction {
  public void apply(ASTNode n) {

      FilterCondition filters = n.ntchild(1).getValue("filters");
      n.setValue("filters",  filters);
      
      n.setValue("functionName",  n.ntchild(0).getValue("functionName")); 
      n.setValue("valueList",  n.ntchild(0).getValue("valueList"));
      n.setValue("result",  n.ntchild(0).getValue("result"));
    
  }
}