package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import java.util.List;

public class ParametersLoader$role$evaluation$8 implements SemanticAction {
  public void apply(ASTNode n) {

      // extract the symbol
      Symbol<?> s = (Symbol<?>) n.ntchild(1).getValue("type");
      s.setName( n.ntchild(0).getValue("simpleId").toString() );

      // pass it on
      n.setValue("parameter",  s);
  	
  }
}