package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class NewDataModule extends Module {
  public NewDataModule() {
    declareSyntax();
    declareRole("evaluation", 0, 8, 11);

} 
}