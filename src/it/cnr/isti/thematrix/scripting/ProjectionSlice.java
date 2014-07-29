package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class ProjectionSlice extends Slice {
  public ProjectionSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.Projection");
    importRoles("it.cnr.isti.thematrix.scripting.Projection", "evaluation");

} 
}