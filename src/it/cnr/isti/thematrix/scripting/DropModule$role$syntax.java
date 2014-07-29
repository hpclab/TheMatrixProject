package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class DropModule$role$syntax extends Syntax {
 public DropModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "DropModule", ")", "inputs", nt("ModuleImport"), "parameters", "params", "=", "[", nt("ColumnIdList"), "]", "end")
   );
 }
}