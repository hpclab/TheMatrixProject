package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import it.cnr.isti.thematrix.scripting.sys.*;

public class Projection$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

      String name = n.ntchild(0).getValue("moduleId");
      String inputModule1 = n.ntchild(1).getValue("moduleId");
      String inputSchema1 = n.ntchild(1).getValue("schemaName");
      String inputModule2 = n.ntchild(2).getValue("moduleId");
      String inputSchema2 = n.ntchild(2).getValue("schemaName");
      String join1 = n.ntchild(3).getValue("columnId");
      String join2 = n.ntchild(4).getValue("columnId");
      String input = n.ntchild(5).getValue("columnId");
      String result = n.ntchild(6).getValue("columnId");
      MatrixModule m = new MatrixProjection(
          name, inputModule1, inputSchema1, inputModule2, inputSchema2, join1, join2, input, result                      
      );
      TheMatrixSys.addModule(m);
  		n.setValue("moduleContents",  m);
      m.setup();
  	
  }
}