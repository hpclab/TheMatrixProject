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

public class ProductModule$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

      FilterCondition fc = n.ntchild(4).getValue("filters");
      MatrixProduct m = (MatrixProduct)TheMatrixSys.getCurrentModule();
      m.setFilters(fc);  
      m.setup();
    
  }
}