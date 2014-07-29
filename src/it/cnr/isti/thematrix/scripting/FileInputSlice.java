package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class FileInputSlice extends Slice {
  public FileInputSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.FileInputModule");
    importRoles("it.cnr.isti.thematrix.scripting.FileInputModule", "evaluation");

} 
}