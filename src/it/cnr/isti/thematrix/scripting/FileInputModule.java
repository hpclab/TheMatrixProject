package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class FileInputModule extends Module {
  public FileInputModule() {
    declareSyntax();
    declareRole("evaluation", 0, 3, 5, 8, 11);

} 
}