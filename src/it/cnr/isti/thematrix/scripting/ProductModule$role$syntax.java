package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class ProductModule$role$syntax extends Syntax {
 public ProductModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "ProductModule", ")", "inputs", nt("ModuleImport"), nt("ModuleImport"), "parameters", "IDfield", "=", nt("ColumnId"), nt("ManyPrFunctions"), "end"),
    p(nt("ManyPrFunctions"), nt("PrFunctionSpec")),
    p(nt("ManyPrFunctions"), nt("PrFunctionSpec"), nt("BooleanExpr")),
    p(nt("PrFunctionSpec"), "functions", "=", "[", nt("PrFunctionList"), "]"),
    p(nt("PrFunctionList"), nt("PrFunction"), ",", nt("PrFunctionList")),
    p(nt("PrFunctionList"), nt("PrFunction")),
    p(nt("PrFunction"), nt("SimpleId"), "(", nt("PrArgList"), ")"),
    p(nt("PrArg"), nt("Literal")),
    p(nt("PrArg"), nt("QualifiedColumnId")),
    p(nt("PrArgList"), nt("PrArg"), ",", nt("PrArgList")),
    p(nt("PrArgList"), nt("PrArg")),
    p(nt("QualifiedColumnId"), nt("ModuleId"), ".", nt("ColumnId"))
   );
 }
}