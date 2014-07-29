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

public class ScriptInputModule$role$evaluation$11 implements SemanticAction {
  public void apply(ASTNode n) {

      //n.setValue("stringValue",  TheMatrixSys.getParams().get(n.ntchild(0).getValue("variable").toString()).value.toString()); 
      n.setValue("symbol",  TheMatrixSys.getParamTable().get(n.ntchild(0).getValue("variable").toString())); 
    
  }
}