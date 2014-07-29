package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.List;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;

public class FilterModule$role$evaluation$17 implements SemanticAction {
  public void apply(ASTNode n) {
  
        String columnId = n.ntchild(0).getValue("columnId").toString();
        String operation = n.ntchild(1).getValue("comparisonOp").toString();

        // obtain reference to the attribute in the dataset 
        Symbol<?> columnValue = TheMatrixSys.getCurrentModule().get(columnId);
        Symbol<?> value       = (Symbol<?>)n.ntchild(2).getValue("value");

        // typecheck values
        if (!columnValue.isCompatible(value)) throw new UncompatibleTypeException(columnValue, value);

        // get right operation for the given type
        Operation<Object,Object,Object> operationImpl = TheMatrixSys.getOpTable().get(value.type, operation);
        
        // aggregate in a FilterCondition object (a kind of partially evaluated function)
        n.setValue("conditionSpec",  new SingleFilterCondition(columnValue, value, operationImpl));//String.format("%s %s %s", columnId, operationImpl, value);
     
  }
}