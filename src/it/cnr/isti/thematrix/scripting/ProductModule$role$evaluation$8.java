package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;
import it.cnr.isti.thematrix.scripting.productmodule.support.*;
import java.util.List;

public class ProductModule$role$evaluation$8 implements SemanticAction {
  public void apply(ASTNode n) {

       FilterSequence fs = n.ntchild(1).getValue("bexpr");
       List<FilterCondition> conditionList = n.ntchild(0).getValue("filters");
       fs.addAll(conditionList);
       n.setValue("filters",  fs); 
    
  }
}