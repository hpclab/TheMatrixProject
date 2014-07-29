package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class FirstModule$role$syntax extends Syntax {
 public FirstModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "FirstModule", ")", "inputs", nt("ModuleImport"), "parameters", "params", "=", "[", nt("ColumnIdList"), "]", "end")
   );
 }
}