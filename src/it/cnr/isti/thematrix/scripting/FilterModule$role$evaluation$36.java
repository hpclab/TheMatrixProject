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
import it.cnr.isti.thematrix.scripting.modules.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;

public class FilterModule$role$evaluation$36 implements SemanticAction {
  public void apply(ASTNode n) {
 
        // if no boolean condition is specified, 
        // then the condition list must contain only one element

        List<FilterCondition> fcs = n.ntchild(0).getValue("conditionList");
        if (fcs.size() > 1) throw new RuntimeException("Too many conditions: a boolean expression must be specified!"); 
        
        // retrieve filter type from the subtree rooted at FilterType
        MatrixFilter.Type t = n.ntchild(1).getValue("filterType");
        if (t == MatrixFilter.Type.DISCARD) {
          // decorate the single condition we have with the DiscardFilterCondition object
          n.setValue("filters",  new DiscardFilterCondition(fcs.get(0)));
        } else {
          // otherwise, just pass on the single condition we have
          n.setValue("filters",  fcs.get(0));
        }
      
  }
}