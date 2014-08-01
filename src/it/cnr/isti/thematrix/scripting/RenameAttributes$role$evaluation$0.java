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
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import java.util.List;
import neverlang.utils.*;

public class RenameAttributes$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

       String inputTable  = n.ntchild(1).getValue("moduleId"); 
       String schemaName  = n.ntchild(1).getValue("schemaName"); 
       List<String> inAttr = AttributeList.collectFrom(n.ntchild(2), "columnId");
       List<String> outAttr = AttributeList.collectFrom(n.ntchild(3), "columnId");
       
       MatrixModule m = new MatrixRenameAttributes(n.ntchild(0).getValue("moduleId").toString(), inputTable, schemaName, inAttr, outAttr);
       n.setValue("moduleContents",  m);
       TheMatrixSys.addModule(m);
       m.setup();
    
  }
}