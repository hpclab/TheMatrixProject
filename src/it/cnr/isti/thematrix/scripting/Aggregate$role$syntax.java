package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class Aggregate$role$syntax extends Syntax {
 public Aggregate$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "AggregateModule", ")", "inputs", nt("ModuleImport"), "parameters", "isInputSorted", "=", nt("Boolean"), "groupBy", "=", "[", nt("ColumnIdList"), "]", "functions", "=", "[", nt("AggregateFuncList"), "]", "results", "=", "[", nt("ColumnDefList"), "]", "end"),
    p(nt("AggregateFuncList"), nt("AggregateFunc"), ",", nt("AggregateFuncList")),
    p(nt("AggregateFuncList"), nt("AggregateFunc")),
    p(nt("AggregateFunc"), nt("SimpleId"), "(", nt("ColumnId"), ")")
   );
 }
}