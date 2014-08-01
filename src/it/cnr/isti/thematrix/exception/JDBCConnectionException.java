/*
 * Copyright (c) 2010-2014 "HPCLab at ISTI-CNR"
 *
 * This file is part of TheMatrix.
 *
 * TheMatrix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
