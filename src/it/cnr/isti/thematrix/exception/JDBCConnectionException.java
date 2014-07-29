package it.cnr.isti.thematrix.exception;

/**
 * TODO: document this custom exception class
 * @author massimo
 *
 */
public class JDBCConnectionException extends Exception
{
	private static final long serialVersionUID = 2853190409657217621L;

	public JDBCConnectionException()
	{	}

	public JDBCConnectionException(String message)
	{	super(message);	}

	public JDBCConnectionException(Throwable cause)
	{	super(cause);	}

	public JDBCConnectionException(String message, Throwable cause)
	{	super(message, cause);	}

}
