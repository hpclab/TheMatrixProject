package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;

public class Types$role$evaluation$34 implements SemanticAction {
  public void apply(ASTNode n) {
 
			n.setValue("moduleId",  n.ntchild(0).getValue("simpleId")); 
			n.setValue("row",  n.ntchild(0).getValue("row")); 
			n.setValue("col",  n.ntchild(0).getValue("col")); 
		
  }
}