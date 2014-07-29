package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class CopyAttributeSlice extends Slice {
  public CopyAttributeSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.CopyAttributeModule");
    importRoles("it.cnr.isti.thematrix.scripting.CopyAttributeModule", "evaluation");

} 
}