package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class SortSlice extends Slice {
  public SortSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.SortModule");
    importRoles("it.cnr.isti.thematrix.scripting.SortModule", "evaluation");

} 
}