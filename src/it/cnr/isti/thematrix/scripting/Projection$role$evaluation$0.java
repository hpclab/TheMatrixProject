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
import it.cnr.isti.thematrix.scripting.modules.*;
import it.cnr.isti.thematrix.scripting.sys.*;

public class Projection$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

      String name = n.ntchild(0).getValue("moduleId");
      String inputModule1 = n.ntchild(1).getValue("moduleId");
      String inputSchema1 = n.ntchild(1).getValue("schemaName");
      String inputModule2 = n.ntchild(2).getValue("moduleId");
      String inputSchema2 = n.ntchild(2).getValue("schemaName");
      String join1 = n.ntchild(3).getValue("columnId");
      String join2 = n.ntchild(4).getValue("columnId");
      String input = n.ntchild(5).getValue("columnId");
      String result = n.ntchild(6).getValue("columnId");
      MatrixModule m = new MatrixProjection(
          name, inputModule1, inputSchema1, inputModule2, inputSchema2, join1, join2, input, result                      
      );
      TheMatrixSys.addModule(m);
  		n.setValue("moduleContents",  m);
      m.setup();
  	
  }
}