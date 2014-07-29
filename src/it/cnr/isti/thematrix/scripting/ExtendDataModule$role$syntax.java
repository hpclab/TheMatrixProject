package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class ExtendDataModule$role$syntax extends Syntax {
 public ExtendDataModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "ExtendDataModule", ")", "inputs", nt("ModuleImport"), "parameters", "attributes", "=", "[", nt("ColumnDefList"), "]", "end")
   );
 }
}