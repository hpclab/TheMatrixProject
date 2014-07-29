package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.List;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import it.cnr.isti.thematrix.scripting.aggregate.support.*;

public class Aggregate$role$evaluation$12 implements SemanticAction {
  public void apply(ASTNode n) {

      // encapsulate the pairs (functionName, columnName) in Tuple2 object
      // so that they can then be collected using AttributeList
      
      String fname = n.ntchild(0).getValue("simpleId");
      String colId = n.ntchild(1).getValue("columnId");
      Tuple2 t = new Tuple2<String,String>(fname,colId);
      n.setValue("function",   t);
    
  }
}