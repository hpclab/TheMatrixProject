package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class MainSlice extends Slice {
  public MainSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.Main");
    importRoles("it.cnr.isti.thematrix.scripting.Main", "evaluation");

} 
}