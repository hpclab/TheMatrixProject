package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;

public class Types$role$evaluation$24 implements SemanticAction {
  public void apply(ASTNode n) {
 n.setValue("columnId",  n.ntchild(0).getValue("simpleId")); 
  }
}