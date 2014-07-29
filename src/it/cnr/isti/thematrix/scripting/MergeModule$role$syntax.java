package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class MergeModule$role$syntax extends Syntax {
 public MergeModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "MergeModule", ")", "inputs", nt("ModuleImport"), nt("ModuleImport"), "parameters", "primaryKey", "=", "[", nt("ColumnIdList"), "]", "fieldNames", "=", "[", nt("ColumnIdList"), "]", "end")
   );
 }
}