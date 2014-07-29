package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class ApplyFunctionSlice extends Slice {
  public ApplyFunctionSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.ApplyFunction");
    importRoles("it.cnr.isti.thematrix.scripting.ApplyFunction", "evaluation");

} 
}