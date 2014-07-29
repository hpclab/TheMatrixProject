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

public class FilterModule$role$evaluation$36 implements SemanticAction {
  public void apply(ASTNode n) {
 
        // if no boolean condition is specified, 
        // then the condition list must contain only one element

        List<FilterCondition> fcs = n.ntchild(0).getValue("conditionList");
        if (fcs.size() > 1) throw new RuntimeException("Too many conditions: a boolean expression must be specified!"); 
        
        // retrieve filter type from the subtree rooted at FilterType
        MatrixFilter.Type t = n.ntchild(1).getValue("filterType");
        if (t == MatrixFilter.Type.DISCARD) {
          // decorate the single condition we have with the DiscardFilterCondition object
          n.setValue("filters",  new DiscardFilterCondition(fcs.get(0)));
        } else {
          // otherwise, just pass on the single condition we have
          n.setValue("filters",  fcs.get(0));
        }
      
  }
}