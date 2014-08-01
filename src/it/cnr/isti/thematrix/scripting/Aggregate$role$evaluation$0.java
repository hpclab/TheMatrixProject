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