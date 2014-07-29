package it.cnr.isti.thematrix.scripting.sys;

public class UndefinedFunctionException extends RuntimeException {
	public UndefinedFunctionException(String reason) {
		super(reason);
	}
}
