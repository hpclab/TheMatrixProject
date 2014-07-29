package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class ExtendDataSlice extends Slice {
  public ExtendDataSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.ExtendDataModule");
    importRoles("it.cnr.isti.thematrix.scripting.ExtendDataModule", "evaluation");

} 
}