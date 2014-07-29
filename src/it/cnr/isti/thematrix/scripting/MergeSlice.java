package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class MergeSlice extends Slice {
  public MergeSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.MergeModule");
    importRoles("it.cnr.isti.thematrix.scripting.MergeModule", "evaluation");

} 
}