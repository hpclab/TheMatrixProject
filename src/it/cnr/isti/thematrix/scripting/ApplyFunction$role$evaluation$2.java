package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.List;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;
import it.cnr.isti.thematrix.scripting.modules.*;

public class ApplyFunction$role$evaluation$2 implements SemanticAction {
  public void apply(ASTNode n) {

      String mname = n.ntchild(1).getValue("moduleId");
      MatrixModule m = TheMatrixSys.getModule(mname);
      TheMatrixSys.setCurrentModule(m);
    
  }
}