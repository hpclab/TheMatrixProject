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

public class FilterModule$role$evaluation$7 implements SemanticAction {
  public void apply(ASTNode n) {

        // put the list of condition from the tree rooted at child n.ntchild(0) into the attribute 
        // "conditionList" of this node
        n.setValue("conditionList",  AttributeList.collectFrom(n.ntchild(0), "conditionSpec"));
      
  }
}