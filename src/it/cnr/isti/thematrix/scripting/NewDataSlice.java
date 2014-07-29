package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class NewDataSlice extends Slice {
  public NewDataSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.NewDataModule");
    importRoles("it.cnr.isti.thematrix.scripting.NewDataModule", "evaluation");

} 
}