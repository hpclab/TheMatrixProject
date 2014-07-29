package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.List;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;

public class FilterModule$role$evaluation$10 implements SemanticAction {
  public void apply(ASTNode n) {

        n.setValue("conditionList",  new NonTerminalListIterator<FilterCondition>(n.ntchild(0), "conditionSpec"));
      
  }
}