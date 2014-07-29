package it.cnr.isti.thematrix.scripting.sys;

@SuppressWarnings("serial")
public class MissingFieldException extends MissingSymbolException {

	MatrixModule origin;
	public MissingFieldException(String attributeName, MatrixModule m) {
		super(attributeName + " in module "+m.name);
		this.origin = m;
	}
	
	public MatrixModule getOrigin() {
		return origin;
	}

}
