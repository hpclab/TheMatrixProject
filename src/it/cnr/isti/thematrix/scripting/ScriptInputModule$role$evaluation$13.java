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

public class ScriptInputModule$role$evaluation$13 implements SemanticAction {
  public void apply(ASTNode n) {

      n.setValue("symbol",  n.ntchild(0).getValue("symbol"));
      //Symbol<?> sym = n.ntchild(0).getValue("symbol");
      //n.setValue("stringValue",  sym.value.toString());
    
  }
}