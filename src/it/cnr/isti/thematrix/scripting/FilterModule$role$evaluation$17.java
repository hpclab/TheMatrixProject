/*
 * Copyright (c) 2010-2014 "HPCLab at ISTI-CNR"
 *
 * This file is part of TheMatrix.
 *
 * TheMatrix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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