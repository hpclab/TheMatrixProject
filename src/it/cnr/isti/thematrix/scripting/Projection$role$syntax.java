package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class Projection$role$syntax extends Syntax {
 public Projection$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "ProjectionModule", ")", "inputs", nt("ModuleImport"), nt("ModuleImport"), "parameters", "joinAttribute1", "=", nt("ColumnId"), "joinAttribute2", "=", nt("ColumnId"), "inputAttribute", "=", nt("ColumnId"), "resultAttribute", "=", nt("ColumnId"), "end")
   );
 }
}