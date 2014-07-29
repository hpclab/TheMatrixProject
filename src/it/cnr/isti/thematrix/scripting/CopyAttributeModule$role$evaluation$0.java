package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import java.util.List;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;

public class CopyAttributeModule$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {

       String moduleId = n.ntchild(0).getValue("moduleId");

       String sourceName = n.ntchild(1).getValue("moduleId"); 
       String sourceSchema = n.ntchild(1).getValue("schemaName");
       String destName  = n.ntchild(2).getValue("moduleId"); 
       String destSchema = n.ntchild(2).getValue("schemaName");
       
       //registerNewTable(destName);  // create field in symbol table

       List<String> sourceIdList = AttributeList.collectFrom(n.ntchild(3), "columnId");
       List<String> destIdList   = AttributeList.collectFrom(n.ntchild(4), "columnId");


       // for (String src, dest : sourceIdList, destIdList) {
       //   prepare <dest> symbol table by copying schema
       // }

       MatrixCopyAttribute m = new MatrixCopyAttribute(moduleId, sourceName, sourceSchema, destName, destSchema, sourceIdList, destIdList);
       n.setValue("moduleContents",  m);
       TheMatrixSys.addModule(m); 
       m.setup();

       
      
  }
}