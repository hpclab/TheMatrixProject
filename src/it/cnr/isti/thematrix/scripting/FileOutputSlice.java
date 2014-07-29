package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class FileOutputSlice extends Slice {
  public FileOutputSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.FileOutputModule");
    importRoles("it.cnr.isti.thematrix.scripting.FileOutputModule", "evaluation");

} 
}