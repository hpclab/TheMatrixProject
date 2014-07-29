package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import java.util.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import it.cnr.isti.thematrix.common.Enums.*;

public class FileOutputModule$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {


      String moduleName = n.ntchild(0).getValue("moduleId");
      ChecksumType checksum = n.ntchild(2).getValue("checksum");
      CompressionType compression = n.ntchild(3).getValue("compression");

      MatrixModule m = new MatrixFileOutput(moduleName, (String)n.ntchild(1).getValue("moduleId"), (String)n.ntchild(1).getValue("schemaName"), checksum, compression);
      TheMatrixSys.addModule(m);
      m.setup();
      n.setValue("moduleContents",  m);

  	
  }
}