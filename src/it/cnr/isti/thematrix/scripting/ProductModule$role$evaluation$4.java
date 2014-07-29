package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;
import it.cnr.isti.thematrix.scripting.productmodule.support.*;
import java.util.List;

public class ProductModule$role$evaluation$4 implements SemanticAction {
  public void apply(ASTNode n) {


      // module is instantiated here, 
      // so that it is available throughout the rest of the subtree

      String module1 = n.ntchild(1).getValue("moduleId");
      String schema1 = n.ntchild(1).getValue("schemaName");
      String module2 = n.ntchild(2).getValue("moduleId");
      String schema2 = n.ntchild(2).getValue("schemaName");
      String idField = n.ntchild(3).getValue("columnId");

      MatrixModule m = new MatrixProduct(
      	n.ntchild(0).getValue("moduleId").toString(), 
      	module1, schema1, 
      	module2, schema2, idField);
      n.setValue("moduleContents",  m);
      TheMatrixSys.addModule(m);

      // setCurrentModule is invoked here
      TheMatrixSys.setCurrentModule(m); 
  	
  }
}