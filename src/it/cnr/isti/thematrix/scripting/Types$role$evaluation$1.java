package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;

public class Types$role$evaluation$1 implements SemanticAction {
  public void apply(ASTNode n) {

			String vname = n.tchild(0).token.text.substring(1); // strip '$'
		    n.setValue("variable",  vname); 
		
  }
}