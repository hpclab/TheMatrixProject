package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.List;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;

public class FilterModule$role$evaluation$2 implements SemanticAction {
  public void apply(ASTNode n) {

        // sets the input to the freshly-created module
        MatrixFilter m = n.getValue("moduleContents");
        String inputModule = n.ntchild(1).getValue("moduleId");
        String inputSchema = n.ntchild(1).getValue("schemaName");
        m.addInput(inputModule, inputSchema);
     
  }
}