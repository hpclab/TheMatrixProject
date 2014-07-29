package it.cnr.isti.thematrix.exception;

public class DatasetRowLengthMismatch extends Exception
{
	private static final long serialVersionUID = -7918497624318852359L;

	public DatasetRowLengthMismatch(String message)
	{
		super(message);
	}

	public DatasetRowLengthMismatch(Throwable cause)
	{
		super(cause);
	}

	public DatasetRowLengthMismatch(String message, Throwable cause)
	{
		super(message, cause);
	}

}
