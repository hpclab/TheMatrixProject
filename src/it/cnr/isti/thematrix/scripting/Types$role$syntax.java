package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class Types$role$syntax extends Syntax {
 public Types$role$syntax() {
   declareProductions(
    p(nt("SimpleId"), regex("[a-zA-Z_][a-zA-Z0-9_]+")),
    p(nt("Variable"), regex("\\$[a-zA-Z_][a-zA-Z0-9_]+")),
    p(nt("FileNameId"), regex("[a-zA-Z0-9-_]+\\.(csv|txt)")),
    p(nt("Integer"), regex("[0-9]+")),
    p(nt("Float"), regex("[0-9]+\\.[0-9]+")),
    p(nt("Boolean"), "true"),
    p(nt("Boolean"), "false"),
    p(nt("String"), regex("\"([^\"]*)\"")),
    p(nt("TimeStamp"), regex("[0-9]{4}-[0-9]{2}-[0-9]{2}(( )+[0-9]{2}:[0-9]{2}(:[0-9]{2})?)?")),
    p(nt("Literal"), nt("Integer")),
    p(nt("Literal"), nt("Float")),
    p(nt("Literal"), nt("Boolean")),
    p(nt("Literal"), nt("String")),
    p(nt("Literal"), nt("TimeStamp")),
    p(nt("DataType"), "int"),
    p(nt("DataType"), "float"),
    p(nt("DataType"), "boolean"),
    p(nt("DataType"), "string"),
    p(nt("DataType"), "date"),
    p(nt("ColumnId"), nt("SimpleId")),
    p(nt("SchemaName"), nt("SimpleId")),
    p(nt("ConstantValue"), nt("Variable")),
    p(nt("ConstantValue"), nt("Literal")),
    p(nt("Value"), nt("ColumnId")),
    p(nt("ModuleId"), nt("SimpleId")),
    p(nt("ModuleImport"), nt("SchemaName"), "=", nt("ModuleId")),
    p(nt("Value"), nt("ConstantValue")),
    p(nt("Literal"), "MISSING"),
    p(nt("ColumnIdList"), nt("ColumnId"), ",", nt("ColumnIdList")),
    p(nt("ColumnIdList"), nt("ColumnId"))
   );
 }
}