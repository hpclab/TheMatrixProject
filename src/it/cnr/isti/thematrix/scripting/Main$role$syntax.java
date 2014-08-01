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

public class Main$role$syntax extends Syntax {
 public Main$role$syntax() {
   declareProductions(
    p(nt("Program"), nt("ModuleList")),
    p(nt("ModuleList"), nt("Module"), nt("ModuleList")),
    p(nt("ModuleList"), nt("Module")),
    p(nt("Program"), nt("DeclareList"), nt("ModuleList")),
    p(nt("DeclareList"), nt("DeclareSchema"), nt("DeclareList")),
    p(nt("DeclareList"), nt("DeclareSchema")),
    p(nt("_"), regex("\\s")),
    p(nt("_"), regex("(?s:/\\*.*?\\*/)")),
    p(nt("_"), regex("//[^\\n]+"))
   );
 }
}