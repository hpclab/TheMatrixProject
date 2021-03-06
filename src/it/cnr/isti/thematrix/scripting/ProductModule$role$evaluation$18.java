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