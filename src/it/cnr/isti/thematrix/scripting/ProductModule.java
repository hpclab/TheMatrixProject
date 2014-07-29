package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class ProductModule extends Module {
  public ProductModule() {
    declareSyntax();
    declareRole("evaluation", 0, 4, 6, 8, 11, 18, 21, 23, 30);

} 
}