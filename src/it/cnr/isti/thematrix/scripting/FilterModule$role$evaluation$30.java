package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.List;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;

public class FilterModule$role$evaluation$30 implements SemanticAction {
  public void apply(ASTNode n) {
 
      List<FilterCondition> fcs = n.ntchild(0).getValue("conditionList");
      if (fcs.size() > 1) throw new RuntimeException("Too many conditions: a boolean expression must be specified!"); 
      n.setValue("filters",  fcs.get(0));
      
  }
}