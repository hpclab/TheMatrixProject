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

public class ProductModule$role$evaluation$6 implements SemanticAction {
  public void apply(ASTNode n) {

      // if the boolean connective is unspecified, then the list of filters
      // must contain only one element
      List<FilterCondition> filters = n.ntchild(0).getValue("filters");
      if (filters.size()>1)  throw new RuntimeException("Too many conditions: a boolean expression must be specified!"); 
      n.setValue("filters",  filters.get(0));
    
  }
}