package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import it.cnr.isti.thematrix.scripting.modules.*;
import java.util.List;

public class NewDataModule$role$evaluation$11 implements SemanticAction {
  public void apply(ASTNode n) {

      // define symbol type
      Symbol<?> s = n.ntchild(1).getValue("type");
      Symbol<?> v = n.ntchild(2).getValue("value");
      s.setName( n.ntchild(0).getValue("columnId").toString() );
      s.setValue( v.value );
      n.setValue("columnDef",  s);
      //n.ntchild(0).setValue("defaultValue",  v);
    
  }
}