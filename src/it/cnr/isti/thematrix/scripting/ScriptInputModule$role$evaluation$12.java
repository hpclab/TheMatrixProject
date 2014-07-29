package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.*;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;
import it.cnr.isti.thematrix.scripting.modules.*;

public class ScriptInputModule$role$evaluation$12 implements SemanticAction {
  public void apply(ASTNode n) {

      n.setValue("fileNameId",  n.ntchild(0).getValue("fileNameId"));
      n.setValue("params",  n.ntchild(1).getValue("params"));
    
  }
}