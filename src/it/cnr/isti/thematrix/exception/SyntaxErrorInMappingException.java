package it.cnr.isti.thematrix.exception;

/**
 * TODO: document this custom exception class
 * 
 * @author massimo
 *
 */
public class SyntaxErrorInMappingException extends Exception
{
	private static final long serialVersionUID = 1L;

	public SyntaxErrorInMappingException()
	{	}

	public SyntaxErrorInMappingException(String message)
	{	super(message);	}

	public SyntaxErrorInMappingException(Throwable cause)
	{	super(cause);	}

	public SyntaxErrorInMappingException(String message, Throwable cause)
	{	super(message, cause);	}
}
