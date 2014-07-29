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

public class ProductModule$role$evaluation$18 implements SemanticAction {
  public void apply(ASTNode n) {

       String functionName = n.ntchild(0).getValue("simpleId");
       // collects all the arguments for this function: either a literal, or 
       // a reference to a column of the input modules
       List<Symbol<?>> args = AttributeList.collectFrom(n.ntchild(1), "symbol");

       // functionName is lowercased to make the lookup case insensitive
       Operation<Object,Object,Object> op = TheMatrixSys.getFuncTable().get(DataType.BOOLEAN, functionName.toLowerCase());
       ProductFilterCondition pf = new ProductFilterCondition(args, op);
       n.setValue("filter",  pf);
    
  }
}