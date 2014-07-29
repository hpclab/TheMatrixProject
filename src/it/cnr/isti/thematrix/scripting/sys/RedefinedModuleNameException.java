package it.cnr.isti.thematrix.scripting.sys;

public class RedefinedModuleNameException extends RuntimeException {
	public RedefinedModuleNameException(String reason) {
		super(reason);
	}
}
