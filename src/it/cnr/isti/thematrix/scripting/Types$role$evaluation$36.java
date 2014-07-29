package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;

public class Types$role$evaluation$36 implements SemanticAction {
  public void apply(ASTNode n) {
 n.setValue("moduleId",  n.ntchild(1).getValue("moduleId")); n.setValue("schemaName",  n.ntchild(0).getValue("schemaName")); 
  }
}