package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import java.util.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;

public class FileInputModule$role$evaluation$5 implements SemanticAction {
  public void apply(ASTNode n) {

      n.setValue("inputFileName",  n.ntchild(0).getValue("inputFileName"));
      n.setValue("inputSchema",  n.ntchild(0).getValue("inputSchema"));
      n.setValue("orderByList",  n.ntchild(1).getValue("orderByList"));
    
  }
}