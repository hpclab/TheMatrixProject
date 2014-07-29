package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class ScriptInputSlice extends Slice {
  public ScriptInputSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.ScriptInputModule");
    importRoles("it.cnr.isti.thematrix.scripting.ScriptInputModule", "evaluation");

} 
}