package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;

public class Types$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {
 
			n.setValue("simpleId",  n.tchild(0).token.text); 
			n.setValue("row",  n.tchild(0).token.row);
			n.setValue("col",  n.tchild(0).token.col);
		
  }
}