package it.cnr.isti.thematrix.scripting;
import dexter.lexter.QualifiedToken;
import neverlang.runtime.*;
import neverlang.runtime.dexter.ASTNode;
import neverlang.utils.*;
import it.cnr.isti.thematrix.scripting.sys.*;
import it.cnr.isti.thematrix.scripting.utils.*;
import java.util.List;

public class Main$role$evaluation$0 implements SemanticAction {
  public void apply(ASTNode n) {
 
			List<MatrixModule> moduleList = AttributeList.collectFrom(n.ntchild(0), "moduleContents"); 
			
//			for (MatrixModule m: moduleList) {
//				m.reset();
//			}
			
			
			for (MatrixModule m: moduleList) {
				m.exec();
			}
			
			TheMatrixSys.setCurrentModule(moduleList.get(moduleList.size()-1));
		
  }
}