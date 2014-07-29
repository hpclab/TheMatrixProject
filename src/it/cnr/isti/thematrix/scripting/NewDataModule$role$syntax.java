package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class NewDataModule$role$syntax extends Syntax {
 public NewDataModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "NewDataModule", ")", "parameters", "attributes", "=", "[", nt("ColumnDefList"), "]", "end"),
    p(nt("ColumnDefList"), nt("ColumnDef"), ";", nt("ColumnDefList")),
    p(nt("ColumnDefList"), nt("ColumnDef")),
    p(nt("ColumnDef"), "{", nt("ColumnId"), ":", nt("DataType"), "}"),
    p(nt("ColumnDef"), "{", nt("ColumnId"), ":", nt("DataType"), "=", nt("ConstantValue"), "}")
   );
 }
}