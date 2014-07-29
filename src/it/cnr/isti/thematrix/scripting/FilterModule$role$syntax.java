package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class FilterModule$role$syntax extends Syntax {
 public FilterModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "FilterModule", ")", "inputs", nt("ModuleImport"), "parameters", nt("FilterParameterList"), "end"),
    p(nt("FilterParameterList"), nt("Conditions"), nt("BooleanExpr")),
    p(nt("Conditions"), "conditions", "=", "[", nt("ConditionSpecifierList"), "]"),
    p(nt("ConditionSpecifierList"), nt("ConditionSpecifier"), ";", nt("ConditionSpecifierList")),
    p(nt("ConditionSpecifierList"), nt("ConditionSpecifier")),
    p(nt("ConditionSpecifier"), "{", nt("ColumnId"), nt("ComparisonOp"), nt("Value"), "}"),
    p(nt("BooleanExpr"), "boolExpr", "=", nt("BoolExprType")),
    p(nt("BoolExprType"), "AND"),
    p(nt("BoolExprType"), "OR"),
    p(nt("BoolExprType"), "XOR"),
    p(nt("ComparisonOp"), "="),
    p(nt("ComparisonOp"), "!="),
    p(nt("ComparisonOp"), "<"),
    p(nt("ComparisonOp"), "<="),
    p(nt("ComparisonOp"), ">"),
    p(nt("ComparisonOp"), ">="),
    p(nt("ComparisonOp"), "matches"),
    p(nt("FilterParameterList"), nt("Conditions")),
    p(nt("FilterParameterList"), nt("Conditions"), nt("BooleanExpr"), nt("FilterType")),
    p(nt("FilterParameterList"), nt("Conditions"), nt("FilterType")),
    p(nt("FilterType"), "filterType", "=", "keep"),
    p(nt("FilterType"), "filterType", "=", "discard")
   );
 }
}