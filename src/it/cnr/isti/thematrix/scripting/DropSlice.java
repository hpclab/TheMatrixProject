package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class DropSlice extends Slice {
  public DropSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.DropModule");
    importRoles("it.cnr.isti.thematrix.scripting.DropModule", "evaluation");

} 
}