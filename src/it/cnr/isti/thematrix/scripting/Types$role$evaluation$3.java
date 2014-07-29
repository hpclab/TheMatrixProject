package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;

public class Types$role$evaluation$3 implements SemanticAction {
  public void apply(ASTNode n) {
 
			n.setValue("integer",  new Literal<Integer>(n.tchild(0).token.row, n.tchild(0).token.col, Integer.parseInt(n.tchild(0).token.text), DataType.INT));
		
  }
}