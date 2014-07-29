package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class FirstSlice extends Slice {
  public FirstSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.FirstModule");
    importRoles("it.cnr.isti.thematrix.scripting.FirstModule", "evaluation");

} 
}