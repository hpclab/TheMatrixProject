package it.cnr.isti.thematrix.scripting.sys;

public class MissingModuleException extends RuntimeException {
	public MissingModuleException(String name) {
		super(name);
	}
}
