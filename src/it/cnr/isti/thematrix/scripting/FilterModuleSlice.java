package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class FilterModuleSlice extends Slice {
  public FilterModuleSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.FilterModule");
    importRoles("it.cnr.isti.thematrix.scripting.FilterModule", "evaluation");

} 
}