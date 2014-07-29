package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class TypesSlice extends Slice {
  public TypesSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.Types");
    importRoles("it.cnr.isti.thematrix.scripting.Types", "evaluation");

} 
}