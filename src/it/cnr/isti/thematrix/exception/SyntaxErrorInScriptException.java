package it.cnr.isti.thematrix.exception;

/**
 * TODO: document this custom exception class
 * 
 * @author massimo
 *
 */
public class SyntaxErrorInScriptException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5307862692751991224L;

	public SyntaxErrorInScriptException()
	{	}

	public SyntaxErrorInScriptException(String message)
	{	super(message);	}

	public SyntaxErrorInScriptException(Throwable cause)
	{	super(cause);	}

	public SyntaxErrorInScriptException(String message, Throwable cause)
	{	super(message, cause);	}
}
