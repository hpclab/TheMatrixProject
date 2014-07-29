package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;

public class Types$role$evaluation$8 implements SemanticAction {
  public void apply(ASTNode n) {

			n.setValue("timestamp",  new Literal<java.util.Date>(n.tchild(0).token.row, n.tchild(0).token.col, DateUtil.parse(n.tchild(0).token.text), DataType.DATE));
		
  }
}