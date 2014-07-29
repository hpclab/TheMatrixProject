package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class ParametersLoaderSlice extends Slice {
  public ParametersLoaderSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.ParametersLoader");
    importRoles("it.cnr.isti.thematrix.scripting.ParametersLoader", "evaluation");

} 
}