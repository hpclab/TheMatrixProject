package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class DeclareSchema$role$syntax extends Syntax {
 public DeclareSchema$role$syntax() {
   declareProductions(
    p(nt("DeclareSchema"), "declareSchema", nt("SchemaName"), "=", "[", nt("ColumnDefList"), "]")
   );
 }
}