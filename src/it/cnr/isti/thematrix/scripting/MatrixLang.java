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