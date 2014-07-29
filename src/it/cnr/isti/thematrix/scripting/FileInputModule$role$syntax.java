package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class FileInputModule$role$syntax extends Syntax {
 public FileInputModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "FileInputModule", ")", "parameters", nt("FIParams"), "end"),
    p(nt("FIParams"), nt("InputFileName")),
    p(nt("FIParams"), nt("InputFileName"), nt("OrderByClause")),
    p(nt("InputFileName"), "inputFileName", "=", nt("FileNameId"), "inputSchema", "=", nt("SchemaName")),
    p(nt("OrderByClause"), "orderBy", "=", "[", nt("FInSimpleIdList"), "]"),
    p(nt("FInSimpleIdList"), nt("SimpleId"), ",", nt("FInSimpleIdList")),
    p(nt("FInSimpleIdList"), nt("SimpleId"))
   );
 }
}