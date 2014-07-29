package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class ProductSlice extends Slice {
  public ProductSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.ProductModule");
    importRoles("it.cnr.isti.thematrix.scripting.ProductModule", "evaluation");

} 
}