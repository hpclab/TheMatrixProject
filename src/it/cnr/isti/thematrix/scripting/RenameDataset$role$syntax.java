package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class RenameDataset$role$syntax extends Syntax {
 public RenameDataset$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "RenameDatasetModule", ")", "inputs", nt("ModuleImport"), "end")
   );
 }
}