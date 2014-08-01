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
import neverlang.runtime.Syntax;

public class FilterModule$role$syntax extends Syntax {
 public FilterModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "FilterModule", ")", "inputs", nt("ModuleImport"), "parameters", nt("FilterParameterList"), "end"),
    p(nt("FilterParameterList"), nt("Conditions"), nt("BooleanExpr")),
    p(nt("Conditions"), "conditions", "=", "[", nt("ConditionSpecifierList"), "]"),
    p(nt("ConditionSpecifierList"), nt("ConditionSpecifier"), ";", nt("ConditionSpecifierList")),
    p(nt("ConditionSpecifierList"), nt("ConditionSpecifier")),
    p(nt("ConditionSpecifier"), "{", nt("ColumnId"), nt("ComparisonOp"), nt("Value"), "}"),
    p(nt("BooleanExpr"), "boolExpr", "=", nt("BoolExprType")),
    p(nt("BoolExprType"), "AND"),
    p(nt("BoolExprType"), "OR"),
    p(nt("BoolExprType"), "XOR"),
    p(nt("ComparisonOp"), "="),
    p(nt("ComparisonOp"), "!="),
    p(nt("ComparisonOp"), "<"),
    p(nt("ComparisonOp"), "<="),
    p(nt("ComparisonOp"), ">"),
    p(nt("ComparisonOp"), ">="),
    p(nt("ComparisonOp"), "matches"),
    p(nt("FilterParameterList"), nt("Conditions")),
    p(nt("FilterParameterList"), nt("Conditions"), nt("BooleanExpr"), nt("FilterType")),
    p(nt("FilterParameterList"), nt("Conditions"), nt("FilterType")),
    p(nt("FilterType"), "filterType", "=", "keep"),
    p(nt("FilterType"), "filterType", "=", "discard")
   );
 }
}