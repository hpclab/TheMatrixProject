package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class ApplyFunction$role$syntax extends Syntax {
 public ApplyFunction$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "ApplyFunction", ")", "inputs", nt("ModuleImport"), "parameters", nt("ApplyFunctionParameters"), "end"),
    p(nt("FunctionAndResult"), "function", "=", nt("SimpleId"), "(", nt("ValueList"), ")", "result", "=", nt("ColumnId")),
    p(nt("ApplyFunctionParameters"), nt("FunctionAndResult")),
    p(nt("ApplyFunctionParameters"), nt("FunctionAndResult"), nt("FilterParameterList")),
    p(nt("ValueList"), nt("Value"), ",", nt("ValueList")),
    p(nt("ValueList"), nt("Value"))
   );
 }
}