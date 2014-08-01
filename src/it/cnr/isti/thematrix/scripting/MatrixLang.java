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
import neverlang.runtime.*;
public class MatrixLang extends Language {
  public MatrixLang() {
    importSlices(
     "it.cnr.isti.thematrix.scripting.MainSlice", 
     "it.cnr.isti.thematrix.scripting.DeclareSchemaSlice", 
     "it.cnr.isti.thematrix.scripting.TypesSlice", 
     "it.cnr.isti.thematrix.scripting.FilterModuleSlice", 
     "it.cnr.isti.thematrix.scripting.ParametersLoaderSlice", 
     "it.cnr.isti.thematrix.scripting.FileInputSlice", 
     "it.cnr.isti.thematrix.scripting.CopyAttributeSlice", 
     "it.cnr.isti.thematrix.scripting.NewDataSlice", 
     "it.cnr.isti.thematrix.scripting.SortSlice", 
     "it.cnr.isti.thematrix.scripting.ProjectionSlice", 
     "it.cnr.isti.thematrix.scripting.ProductSlice", 
     "it.cnr.isti.thematrix.scripting.ExtendDataSlice", 
     "it.cnr.isti.thematrix.scripting.ApplyFunctionSlice", 
     "it.cnr.isti.thematrix.scripting.FirstSlice", 
     "it.cnr.isti.thematrix.scripting.MergeSlice", 
     "it.cnr.isti.thematrix.scripting.ScriptInputSlice", 
     "it.cnr.isti.thematrix.scripting.AggregateSlice", 
     "it.cnr.isti.thematrix.scripting.RenameDatasetSlice", 
     "it.cnr.isti.thematrix.scripting.RenameAttributesSlice", 
     "it.cnr.isti.thematrix.scripting.UnionSlice", 
     "it.cnr.isti.thematrix.scripting.FileOutputSlice", 
     "it.cnr.isti.thematrix.scripting.DropSlice"
    );
    declare(
     role("evaluation")
    );

  } 
}