package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import java.util.List;

public class ExtendDataModule$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

      String moduleId = n.ntchild(1).getValue("moduleId");
      String schemaName = n.ntchild(1).getValue("schemaName");
      List<Symbol<?>> newAttributes = AttributeList.collectFrom(n.ntchild(2), "columnDef");
      MatrixModule m = new MatrixExtendData(n.ntchild(0).getValue("moduleId").toString(), moduleId, schemaName, newAttributes);
      n.setValue("moduleContents",  m);
      TheMatrixSys.addModule(m);
      m.setup();
    
  }
}