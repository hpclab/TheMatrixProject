package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class SortModule$role$syntax extends Syntax {
 public SortModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "SortModule", ")", "inputs", nt("ModuleImport"), "parameters", "fieldNames", "=", "[", nt("ColumnIdList"), "]", "end")
   );
 }
}