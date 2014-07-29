package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import java.util.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import it.cnr.isti.thematrix.common.Enums.*;

public class FileOutputModule$role$evaluation$7 implements SemanticAction {
  public void apply(ASTNode n) {
 n.setValue("compression",  CompressionType.NONE); 
  }
}