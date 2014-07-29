package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class RenameAttributes$role$syntax extends Syntax {
 public RenameAttributes$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "RenameAttributesModule", ")", "inputs", nt("ModuleImport"), "parameters", "inputAttributes", "=", "[", nt("ColumnIdList"), "]", "outputAttributes", "=", "[", nt("ColumnIdList"), "]", "end")
   );
 }
}