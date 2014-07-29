package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import java.util.List;

public class NewDataModule$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

      List<Symbol<?>> schema = AttributeList.collectFrom(n.ntchild(1), "columnDef");
      MatrixModule m = new MatrixNewData(n.ntchild(0).getValue("moduleId").toString(), schema);
      n.setValue("moduleContents",  m);
      TheMatrixSys.addModule(m);
      m.setup();
  	
  }
}