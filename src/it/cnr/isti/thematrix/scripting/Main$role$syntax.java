package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class Main$role$syntax extends Syntax {
 public Main$role$syntax() {
   declareProductions(
    p(nt("Program"), nt("ModuleList")),
    p(nt("ModuleList"), nt("Module"), nt("ModuleList")),
    p(nt("ModuleList"), nt("Module")),
    p(nt("Program"), nt("DeclareList"), nt("ModuleList")),
    p(nt("DeclareList"), nt("DeclareSchema"), nt("DeclareList")),
    p(nt("DeclareList"), nt("DeclareSchema")),
    p(nt("_"), regex("\\s")),
    p(nt("_"), regex("(?s:/\\*.*?\\*/)")),
    p(nt("_"), regex("//[^\\n]+"))
   );
 }
}