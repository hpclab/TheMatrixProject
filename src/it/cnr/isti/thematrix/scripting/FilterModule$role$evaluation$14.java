package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.List;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;

public class FilterModule$role$evaluation$14 implements SemanticAction {
  public void apply(ASTNode n) {
  
        // name of the column to be compared
        String columnId       = n.ntchild(0).getValue("columnId");
        // name of the comparison operation
        String operation = n.ntchild(1).getValue("comparisonOp").toString();

        // obtain reference to the attribute in the dataset 
        Symbol<?> columnValue = TheMatrixSys.getCurrentModule().get(columnId);
        Symbol<?> value       = n.ntchild(2).getValue("value"); // value to compare with

        // typecheck values
        if (!columnValue.isCompatible(value)) throw new UncompatibleTypeException(columnValue, value);

        // get right operation for the given type
        Operation<Object,Object,Object> operationImpl = TheMatrixSys.getOpTable().get(columnValue.type, operation);
        
        // aggregate in a FilterCondition object (a kind of partially evaluated function)
        n.setValue("conditionSpec",  new SingleFilterCondition(columnValue, value, operationImpl));
      
  }
}