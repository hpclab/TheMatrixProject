package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class ParametersLoader$role$syntax extends Syntax {
 public ParametersLoader$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "ParametersModule", ")", "parameters", "params", "=", "[", nt("ParameterList"), "]", "end"),
    p(nt("ParameterList"), nt("Parameter"), ";", nt("ParameterList")),
    p(nt("ParameterList"), nt("Parameter")),
    p(nt("Parameter"), "{", nt("SimpleId"), ":", nt("DataType"), "}")
   );
 }
}