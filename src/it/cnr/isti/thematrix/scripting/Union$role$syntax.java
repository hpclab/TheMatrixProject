package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class Union$role$syntax extends Syntax {
 public Union$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "UnionModule", ")", "inputs", nt("ModuleImport"), nt("ModuleImport"), "end")
   );
 }
}