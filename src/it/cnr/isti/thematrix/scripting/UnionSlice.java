package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class UnionSlice extends Slice {
  public UnionSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.Union");
    importRoles("it.cnr.isti.thematrix.scripting.Union", "evaluation");

} 
}