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

public class FileOutputModule$role$syntax extends Syntax {
 public FileOutputModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "FileOutputModule", ")", "inputs", nt("ModuleImport"), "parameters", "checksum", "=", nt("FOChecksum"), "compression", "=", nt("FOCompressionType"), "end"),
    p(nt("FOChecksum"), "none"),
    p(nt("FOChecksum"), "md5"),
    p(nt("FOCompressionType"), "none"),
    p(nt("FOCompressionType"), "zip"),
    p(nt("FOCompressionType"), "gzip")
   );
 }
}