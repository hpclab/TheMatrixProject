package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class ParametersLoader extends Module {
  public ParametersLoader() {
    declareSyntax();
    declareRole("evaluation", 0, 8);

} 
}