package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.List;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.filtermodule.support.*;
import it.cnr.isti.thematrix.scripting.modules.*;

public class SortModule$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

       String inputTable  = n.ntchild(1).getValue("moduleId"); 
       String schemaName  = n.ntchild(1).getValue("schemaName"); 
       
       //registerNewTable(destName);  // create field in symbol table

       List<String> fieldNames = new NonTerminalListIterator<String>(n.ntchild(2), "columnId");

       // for (String src, dest : sourceIdList, destIdList) {
       //   prepare <dest> symbol table by copying schema
       // }

       MatrixSort m = new MatrixSort(n.ntchild(0).getValue("moduleId").toString(), inputTable, schemaName, fieldNames);
       n.setValue("moduleContents",  m);
       TheMatrixSys.addModule(m);
       m.setup();

      
  }
}