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
import java.util.List;

public class Main$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {
 
			List<MatrixModule> moduleList = AttributeList.collectFrom(n.ntchild(0), "moduleContents"); 
			
//			for (MatrixModule m: moduleList) {
//				m.reset();
//			}
			
			
			for (MatrixModule m: moduleList) {
				m.exec();
			}
			
			TheMatrixSys.setCurrentModule(moduleList.get(moduleList.size()-1));
		
  }
}