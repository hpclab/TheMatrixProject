package it.cnr.isti.thematrix.exception;

/**
 * This exception means a missing/unsupported driver was needed. 
 * It is raised at runtime, if a driver for a specific DBMS is not configured, 
 * if it is not available in the current version of the program, 
 * or if unimplemented code still exists along some execution path which depends on the DBMS choice.
 * The last case is the real worrying bug.
 * 
 * @author massimo
 *
 */
public class UnsupportedDatabaseDriverException extends Exception
{
	private static final long serialVersionUID = 1L;

	public UnsupportedDatabaseDriverException()
	{	}

	public UnsupportedDatabaseDriverException(String message)
	{	super(message);	}

	public UnsupportedDatabaseDriverException(Throwable cause)
	{	super(cause);	}

	public UnsupportedDatabaseDriverException(String message, Throwable cause)
	{	super(message, cause);	}

}
