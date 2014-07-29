package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.*;
public class AggregateSlice extends Slice {
  public AggregateSlice() {
    importSyntax("it.cnr.isti.thematrix.scripting.Aggregate");
    importRoles("it.cnr.isti.thematrix.scripting.Aggregate", "evaluation");

} 
}