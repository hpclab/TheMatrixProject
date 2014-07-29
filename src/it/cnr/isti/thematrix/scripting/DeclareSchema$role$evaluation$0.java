package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import java.util.List;

public class DeclareSchema$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

			String schemaName = n.ntchild(0).getValue("schemaName");
			List<Symbol<?>> schemaSymbols = AttributeList.collectFrom(n.ntchild(1), "columnDef");
			SchemaTable schemata = TheMatrixSys.getPredefinedSchemata();
			DatasetSchema schema = schemata.create(schemaName);
			schema.putAll(schemaSymbols);
		
  }
}