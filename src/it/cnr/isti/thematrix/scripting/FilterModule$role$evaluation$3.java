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

public class FilterModule$role$evaluation$3 implements SemanticAction {
  public void apply(ASTNode n) {

        // this sub-tree has been already visited,
        // thus, we can retrieve the processed filters
        // and pass them to the MatrixFilter instance
        MatrixFilter m = n.getValue("moduleContents");
        FilterCondition fc = n.ntchild(2).getValue("filters");
        m.setFilters(fc);
     
  }
}