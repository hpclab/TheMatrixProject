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
