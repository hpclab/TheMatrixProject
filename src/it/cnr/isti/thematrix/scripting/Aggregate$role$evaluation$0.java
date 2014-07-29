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

public class Aggregate$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

     String inputTable  = n.ntchild(1).getValue("moduleId"); 
       String schemaName  = n.ntchild(1).getValue("schemaName"); 
       Symbol<Boolean> sorted = n.ntchild(2).getValue("bool");

       // collect all attributes "columnId" from the subtree rooted at n.ntchild(3)
       List<String> groupBy = AttributeList.collectFrom(n.ntchild(3), "columnId");
       List<Tuple2<String,String>> functions = AttributeList.collectFrom(n.ntchild(4), "function");
       List<Symbol<?>> results = AttributeList.collectFrom(n.ntchild(5), "columnDef");

       MatrixModule m = new MatrixAggregate(n.ntchild(0).getValue("moduleId").toString(), 
        inputTable, schemaName, groupBy, functions, results, sorted.value);
       n.setValue("moduleContents",  m);
       TheMatrixSys.addModule(m);
       m.setup();
    
  }
}