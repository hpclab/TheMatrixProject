package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class ScriptInputModule extends Module {
  public ScriptInputModule() {
    declareSyntax();
    declareRole("evaluation", 0, 10, 12, 15, 17);

} 
}