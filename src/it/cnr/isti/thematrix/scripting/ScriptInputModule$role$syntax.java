package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class ScriptInputModule$role$syntax extends Syntax {
 public ScriptInputModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "ScriptInputModule", ")", "parameters", nt("ScFileAndParams"), "inputName", "=", nt("ModuleId"), "expectedSchema", "=", nt("SchemaName"), "end"),
    p(nt("ScVarOrLiteralList"), nt("ConstantValue"), ",", nt("ScVarOrLiteralList")),
    p(nt("ScVarOrLiteralList"), nt("ConstantValue")),
    p(nt("ScFileAndParams"), nt("ScFileName")),
    p(nt("ScFileAndParams"), nt("ScFileName"), nt("ScParams")),
    p(nt("ScFileName"), "scriptFilename", "=", nt("FileNameId")),
    p(nt("ScParams"), "scriptParams", "=", "[", nt("ScVarOrLiteralList"), "]")
   );
 }
}