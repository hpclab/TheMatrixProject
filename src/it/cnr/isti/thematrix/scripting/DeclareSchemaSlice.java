package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class DeclareSchemaSlice extends Slice {
  public DeclareSchemaSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.DeclareSchema");
    importRoles("it.cnr.isti.thematrix.scripting.DeclareSchema", "evaluation");

} 
}