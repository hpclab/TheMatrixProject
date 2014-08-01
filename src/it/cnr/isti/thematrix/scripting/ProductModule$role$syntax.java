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

public class ProductModule$role$syntax extends Syntax {
 public ProductModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "ProductModule", ")", "inputs", nt("ModuleImport"), nt("ModuleImport"), "parameters", "IDfield", "=", nt("ColumnId"), nt("ManyPrFunctions"), "end"),
    p(nt("ManyPrFunctions"), nt("PrFunctionSpec")),
    p(nt("ManyPrFunctions"), nt("PrFunctionSpec"), nt("BooleanExpr")),
    p(nt("PrFunctionSpec"), "functions", "=", "[", nt("PrFunctionList"), "]"),
    p(nt("PrFunctionList"), nt("PrFunction"), ",", nt("PrFunctionList")),
    p(nt("PrFunctionList"), nt("PrFunction")),
    p(nt("PrFunction"), nt("SimpleId"), "(", nt("PrArgList"), ")"),
    p(nt("PrArg"), nt("Literal")),
    p(nt("PrArg"), nt("QualifiedColumnId")),
    p(nt("PrArgList"), nt("PrArg"), ",", nt("PrArgList")),
    p(nt("PrArgList"), nt("PrArg")),
    p(nt("QualifiedColumnId"), nt("ModuleId"), ".", nt("ColumnId"))
   );
 }
}