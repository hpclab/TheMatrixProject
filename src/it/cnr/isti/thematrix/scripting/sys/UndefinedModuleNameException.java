package it.cnr.isti.thematrix.scripting.sys;

public class UndefinedModuleNameException extends RuntimeException {
	public UndefinedModuleNameException(String reason) {
		super(reason);
	}
}
