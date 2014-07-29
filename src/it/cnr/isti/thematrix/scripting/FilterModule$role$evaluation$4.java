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

public class FilterModule$role$evaluation$4 implements SemanticAction {
  public void apply(ASTNode n) {
  
       // extract the FilterSequence instance from the subtree rooted at n.ntchild(1)
       FilterSequence fs = n.ntchild(1).getValue("bexpr");
       List<FilterCondition> conditionList = n.ntchild(0).getValue("conditionList");
       fs.addAll(conditionList); // and add the list of conditions to it
       n.setValue("filters",  fs); 
     
  }
}