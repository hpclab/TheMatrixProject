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

public class ProductModule$role$evaluation$23 implements SemanticAction {
  public void apply(ASTNode n) {

      String mname = n.ntchild(0).getValue("moduleId"); // module name
      String columnId = n.ntchild(0).getValue("columnId");

      // retrieve the MatrixProduct instance that is being currently processed (set in node 4)
      MatrixProduct mp = (MatrixProduct) TheMatrixSys.getCurrentModule();
      MatrixModule m = null;
      
      if (mp.input1.name.equals(mname)) m = mp.input1;
      else if (mp.input2.name.equals(mname)) m = mp.input2;
      else throw new RuntimeException("Unbound module '"+mname+"' in ProductModule");

      Symbol<?>    s = m.get(columnId);
      n.setValue("symbol",  s); // pass on the symbol
    
  }
}