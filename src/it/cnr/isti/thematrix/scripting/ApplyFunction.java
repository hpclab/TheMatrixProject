package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class ApplyFunction extends Module {
  public ApplyFunction() {
    declareSyntax();
    declareRole("evaluation", 0, 2, 4, 8, 10);

} 
}