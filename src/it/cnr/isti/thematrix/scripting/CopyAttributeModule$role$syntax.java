package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class CopyAttributeModule$role$syntax extends Syntax {
 public CopyAttributeModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "CopyAttributeModule", ")", "inputs", nt("ModuleImport"), nt("ModuleImport"), "parameters", "inputAttributes", "=", "[", nt("ColumnIdList"), "]", "resultAttributes", "=", "[", nt("ColumnIdList"), "]", "end")
   );
 }
}