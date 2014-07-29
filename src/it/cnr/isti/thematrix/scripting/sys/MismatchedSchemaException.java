package it.cnr.isti.thematrix.scripting.sys;

/**
 * signals when 
 * @author edoardovacchi
 *
 */
@SuppressWarnings("serial")
public class MismatchedSchemaException extends RuntimeException {
	public MismatchedSchemaException(String recordName, String schemaName, String inputSchemaName) {
		super(recordName +" has schema "
                    +schemaName+ " but input expected "+inputSchemaName);
	}
}
