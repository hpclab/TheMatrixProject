package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import java.util.List;

public class ParametersLoader$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

      List<Symbol<?>> parameterList = AttributeList.collectFrom(n.ntchild(1), "parameter");
      
      String moduleId = n.ntchild(0).getValue("moduleId");
      MatrixModule m = new MatrixParameters(moduleId, parameterList); 
      TheMatrixSys.addModule(m);     
      n.setValue("moduleContents",  m);
      m.setup();
  	
  }
}