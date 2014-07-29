package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import java.util.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;

public class FileInputModule$role$evaluation$11 implements SemanticAction {
  public void apply(ASTNode n) {

      n.setValue("orderByList",  AttributeList.collectFrom(n.ntchild(0), "simpleId"));
    
  }
}