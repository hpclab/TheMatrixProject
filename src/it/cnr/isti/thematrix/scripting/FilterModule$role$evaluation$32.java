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

public class FilterModule$role$evaluation$32 implements SemanticAction {
  public void apply(ASTNode n) {
 
        FilterSequence fs = n.ntchild(1).getValue("bexpr");
        List<FilterCondition> conditionList = n.ntchild(0).getValue("conditionList");
        fs.addAll(conditionList);

        // retrieve filter type from the subtree rooted at FilterType
        MatrixFilter.Type t = n.ntchild(2).getValue("filterType");
        if (t == MatrixFilter.Type.DISCARD) {
          n.setValue("filters",  new DiscardFilterCondition(fs));
        } else {
          n.setValue("filters",  fs);
        }
     
  }
}