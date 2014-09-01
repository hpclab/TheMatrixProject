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
package it.cnr.isti.thematrix.mapping.utils;

import it.cnr.isti.thematrix.common.Enums;
import it.cnr.isti.thematrix.common.Enums.CompressionType;
import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.meta.CsvDescriptor;
import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.erasmusmc.utilities.ReadCSVFile;
/**
 * Class to manage CSV files in TheMatrix. THIS DOCUMENTATION IS A STUB! <br>
 * 
 * Access the CSV file itself and its XML metafile / checksum, create/validate them. Provide methods to access the
 * elements of the CSV (header, columns, rows). The actual low-level access happens via the Jerboa
 * <code>ReadCSVFile</code> class. The class currently opens the disk file only when the data is needed. The public
 * <code>hasNext()</code> method can trigger this. <br>
 * The class manages an in-memory buffer of the header of the file and of its body (the actual data). Some methods only
 * act on the in-memory representation, other ones transfer between memory and file system.
 * 
 * Important: the content of the file is represented as a double-nested ArrayList of Strings :
 * <code>ArrayList<ArrayList<String>> content;</code>. The outer Array represents the <b>columns</b>, the inner one the
 * <b>rows</b>.
 * 
 * <br>
 * TODO check why on earth it was done this way, if reading and writing is not done column-wise.<br>
 * TODO the class provides both reading and writing functions, and makes lazy initialization of fields with unclear
 * order and constraints; its interface needs redesign with clearer separation of concerns
 * 
 * FIXME add dealing with file schemata from the new implementation of TheMatrix
 * FIXME better define the purpose of the class, possibly separating more clearly the read and write behaviour. 
 * 
 * TODO An optimization when using this class as an buffer between modules (e.g. before a sort); it should be possible
 * to check if there was data written to disk, so if the file is shorter than the batch size the following module can
 * directly read data from memory.
 * 
 * @author giacomo, iacopo, massimo
 * 
 */
public class CSVFile {
	private ReadCSVFile reader = null;

	private String fileName;
	private String path;
	private String completePath;
	private String version;

	private ArrayList<String> header;
	private ArrayList<ArrayList<String>> content;


	/**
	 * Contains the target header (IAD) when remapping data columns LIAD-> IAD, and when reading data from a CSV file on
	 * disc.<br>
	 * Two different uses related to the two behaviours of the class in reading/writing. (1) when mapping LIAD to IAD,
	 * the names will match with header in the initial part; if new fields are added, they will be at the end. (2) when
	 * reading back in memory a CSV file, the mapped header will match the header on disc, and the header will contain
	 * the same fields in the schema order, so not to break the rest of the code. Content will be permuted immediately
	 * before returning from loadBatch().
	 * 
	 * FIXME this will also need to change according to Neverlang-based schemata definitions
	 */
	private ArrayList<String> mappedHeader;

	// needed for interaction with schema management used in Neverlang code
	private DatasetSchema schema;

	/**
	 * the actual Iterator is provided by Jerboa -- ReadCSVFile class -> CSVFileIterator private class; has no remove
	 * operation
	 */
	private Iterator<List<String>> csvIterator = null;

	/**
	 * Internal cursor to implement iterator methods for accessing the data row by row
	 */
	private int rowCursor;
	
	/**
	 * The amount of rows we have read from the start, allows tracing data errors. Does
	 * not count the header line.
	 */
	private int fileRowCursor;
	
	// cache here the number of rows read into the buffer each time
	private int rowsInBuffer = -1;
	/**
	 * Field used to remember the initial batch size (i.e. buffer size) if we will be
	 * automatically reading the next one. Note that the CSVFile class has the
	 * size of the buffer as a parameter, while calling classes may have it
	 * defined as a constant.
	 */
	private int initialBatchSize;
	/**
	 * The number of (bogus?) rows we had to discard because of a mismatched field number. This count is kept for the
	 * whole file, not only the current in-memory buffer.
	 */
	private int discardedRowCount = 0;
	/**
	 * compression : indicates type of compression for read/write the file 
	 */
	private CompressionType compression = CompressionType.NONE;
	/**
	 * BufferedWriter can be built from DeflaterOutputStream
	 */
	private BufferedWriter bufferedWriter;
	/**
	 * zipOutStream: output Stream for zip file
	 */
	private ZipOutputStream zipOutStream;
	/**
	 * zipEntry: entry for zip file
	 */
	private ZipEntry zipEntry;
	
	/**
	 * Creates a new instance of <code>CSVFile</code>. Stores the necessary data, but does not open the file
	 * @param path 
	 * @param fileName 
	 * @param version 
	 */
	public CSVFile(String path, String fileName, String version) {
		this.completePath = path + fileName;
		this.path = path;
		this.fileName = fileName;
		this.content = new ArrayList<ArrayList<String>>();
		this.header = new ArrayList<String>();
		this.mappedHeader = new ArrayList<String>();
	}
	/**
	 * Creates a new instance of <code>CSVFile</code> setting the compression type. Stores the necessary data, but does not open the file
	 * @param path 
	 * @param fileName 
	 * @param version 
	 * @param compression 
	 */
	public CSVFile(String path, String fileName, String version, CompressionType compression) {
		
		this(path, fileName, version);
		this.compression=compression;
	}
	// FIXME add a constructor with either full path or relative path only
	// public CSVFile(String fullPathName, String version)
	// CSVFile(fullPathName,"",version);

	/**
	 * Return the whole specified column of values from the in-memory buffer. It MAY be asked an unexistent column, now
	 * the default behaviour is to bomb. For backward compatibiliy, currently calls the other getColumnValues with
	 * extend=false.
	 * 
	 * @deprecated
	 * 
	 * @param attribute
	 *            the IAD name of the column
	 * @return the whole specified column of values from the in-memory buffer
	 */
	public Collection<String> getColumnValues(String attribute) {
		return getColumnValues(attribute, null);
		// int numberColumn = this.header.indexOf(attribute);
		// ArrayList<String> result = null;
		// if (numberColumn > 0)
		// result = this.content.get(numberColumn);
		// else {
		// // we get the size from column 0
		// result = new ArrayList<String>(this.content.get(0).size());
		// }
		// return result;
	}

	/**
	 * Return the whole specified column of values from the in-memory buffer. It MAY be asked an unexistent column, if
	 * the extend parameter is not null, and the attribute is present in the mappedHeader, a fresh column of strings
	 * will be allocated with the provided default value, and returned. It assumes at least column 0 really exists, and
	 * that all columns are the same length. Note that the new column is not added to the header here, but inthe
	 * ValueRemapper.
	 * 
	 * FIXME_DESIGN refactor this behaviour which relies on lazy initialization, it makes the code a mess
	 * 
	 * @param attribute
	 *            the IAD name of the column
	 * @param defaultValue
	 *            default String for new column creation; disallowed if null
	 * @return the whole specified column of values from the in-memory buffer
	 */
	public Collection<String> getColumnValues(String attribute, String defaultValue) {
		int numberColumn = this.header.indexOf(attribute);
		ArrayList<String> result = null;
		if (numberColumn > 0)
			result = this.content.get(numberColumn);
		else if (defaultValue != null && (numberColumn = this.mappedHeader.indexOf(attribute)) > 0) {
			// then we return a default-valued new column
			// we get the size from column 0
			int size = this.content.get(0).size();
			result = new ArrayList<String>(size);
			for (int i = 0; i < size; i++)
				result.add(i, defaultValue);
		}
		else {
			LogST.logP(0, "ERROR : getColumnValues column " + attribute + " not found or creation disallowed");
		}
		return result;
	}

	/**
	 * Add (insert!) a collection of new values as a a specified column in the in-memory buffer. <br>
	 * 
	 * FIXME <b>Warning</b> The method will actually insert a column at the specified position, possibly moving existing
	 * columns after it. This doesn't seem to be right, and forces the addition of columns in increasing order!
	 * 
	 * TODO added option to set in the mappedHeader (extended), dangerous too if done out of order! Note that in this
	 * case columnName will be always be an IAD name
	 * 
	 * @param columnName
	 *            the column to replace.
	 * @param values
	 *            the collection of values.
	 */
	public void setColumnValues(String columnName, Collection<String> values) {
		int numberColumn = header.indexOf(columnName);
		if (numberColumn == -1) numberColumn = mappedHeader.indexOf(columnName);
		ArrayList<String> temp = new ArrayList<String>();
		temp.addAll(values);
		this.content.add(numberColumn, temp);
	}
	
	/**
	 * Reset in memory content of the CSV file.
	 * Call this function every time at the end of writing a batch of datas.
	 * This resolve the growth of content data structure in memory.
	 * 
	 */
	public void resetContent() {
		this.content = new ArrayList<ArrayList<String>>();
	}

	/**
	 * Add (insert!) a collection of new values as a a specified column in the in-memory buffer. <br>
	 * This will deal with Mapped Values (see mappedHeader). This version does NOT add columns, only replaces them.
	 * 
	 * @param columnName
	 *            the column to replace.
	 * @param values
	 *            the collection of values.
	 */
	public void setMappedColumnValues(String columnName, Collection<String> values) {
		int numberColumn = header.indexOf(columnName);
		// if it's a new mapped column, look in mappedHeader
		int numberColumnNew = mappedHeader.indexOf(columnName);
		if (numberColumn != -1 && numberColumn != numberColumnNew) {
			LogST.logP(0, "setMappedColumnValues : unknown column " + columnName);
			throw new Error("setMappedColumnValues : unknown column " + columnName);
		}
		if (numberColumn == -1) numberColumn = numberColumnNew;
		ArrayList<String> temp = new ArrayList<String>();
		temp.addAll(values);
		this.content.set(numberColumn, temp);
	}

	/**
	 * Add a new value to the end of the specified column. Create the column if it's not already present. <br>
	 * 
	 * If used properly, it basically adds values at the end, like with having a cursor at the end (BUT THERE ARE NO
	 * SANITY CHECKS).
	 * 
	 * FIXME remove dynamic initialization of columns and simplify the code; I don't think the routine safely works when not adding at the end.
	 * 
	 * @param numberColumn
	 *            column to add to
	 * @param value
	 *            new value
	 */
	public void setValue(int numberColumn, String value) {
		if (content.size() <= numberColumn) { // now 0 < column < size -1
			// LogST.logP(2,"colonna richiesta " + numberColumn + " lunghezza array: " + content.size());
			for (int i = 0; i <= numberColumn - content.size(); i++) {
				// LogST.logP(2,"si crea la colonna " + (content.size() + i));
// FIXME OLD		LogST.logP(0, "WARNING: CSVFile.setValue() adding new column " + i + " in File " + completePath);
					LogST.logP(2, "CSVFile.setValue() adding new column " + numberColumn + " with first value " + value + " in File " + completePath);
				ArrayList<String> temp = new ArrayList<String>();
				content.add(temp);
			}
		}
		/**
		 * I am removing this -1 as the only place where the function is called is IadCreator
		 * this.content.get(numberColumn - 1).add(value);
		 */
		this.content.get(numberColumn).add(value);
		// LogST.logP(2,"CSV File - added value: " +
		// this.content.get(numberColumn - 1).get(this.content.get(numberColumn
		// - 1).size() - 1)
		// + " in column n°" + (numberColumn - 1));
	}

	/**
	 * Method to add a whole row of values to the in-memory buffer
	 * @param values 
	 * 	must be the proper size, null is an acceptable value
	 */
	public void setValueRow(Collection<String> values) {
		if (values.size() == content.size()) {
			int i = 0;
			for (String s : values) {
				content.get(i).add(s);
				i++;
			}
		}
		else
			throw new Error("CSVFile.setValue() Row size mismatch in File " + completePath);
	}

	/**
	 * accessor method to file name
	 * 
	 * @return the relative name of the instance.
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * accessor method to row cursor
	 * 
	 * FIXME wrong! this is the cursor inside the current buffer, not in the whole file!
	 * 
	 * @return the cursor position
	 */
	public int getRowCursor()
	{
		return this.fileRowCursor;
	}
	
	/**
	 * accessor method to file path
	 * 
	 * @return the absolute path of the file
	 */
	public String getPath() {
		return this.path;
	}

	/***
	 * accessor method to completePath
	 * 
	 * @return String completePath
	 */
	public String getCompletePath() {
		return this.completePath;
	}

	/**
	 * Sets the header of the CSV file.
	 * 
	 * Store in the header String Collection the names of the columns.
	 * 
	 * FIXME when this happens, should not the (possibly existing) file reader and iterator be discarded? Or is it
	 * allowed to rename the columns?
	 * 
	 * @param header
	 *            A <code>Collection<String></code> of filed names to replace the current instance-stored header
	 */
	public void setHeader(Collection<String> header) {
		if (this.header.size() != 0) {
			this.header.clear();
		}
		for (String s : header) {
			// LogST.logP("CSV File - header value: " + s);
			this.header.add(s);
		}
		LogST.logP(2, "CSVFile.setHeader() : "+header.toString());// TODO we have a problem
	}

	/**
	 * Sets the mappedHeader of the CSV file. Store in the mappedHeader String Collection the names of the columns.
	 * 
	 * @param header
	 *            A <code>Collection<String></code> of filed names to replace the current instance-stored header
	 */
	public void setMappedHeader(Collection<String> header) {
		if (this.mappedHeader.size() != 0) {
			this.mappedHeader.clear();
		}
		for (String s : header) {
			// LogST.logP(2,"CSV File - header value: " + s);
			this.mappedHeader.add(s);
		}
		LogST.logP(2, "CSVFile.setMappedHeader() :" + header.toString());// TODO we have a problem
	}

	/**
	 * accessor method to the stored header for a CSV file. Header will be empty if the file has not been opened yet.
	 * 
	 * @return the instance-stored header ( a <code>Collection<String></code> )
	 */
	public Collection<String> getHeader() {
		return this.header;
	}

	/**
	 * accessor method to the stored header for a CSV file. Header will be empty if the file has not been opened yet.
	 * 
	 * @return the instance-stored header ( a <code>Collection<String></code> )
	 */
	public Collection<String> getMappedHeader() {
		return this.mappedHeader;
	}

	/**
	 * Method to (open and) check that the CSV file has a next row.
	 * 
	 * This method initializes the ReadCSVFile (see Jerboa docs) and related iterator objects, actually opening the file
	 * if not already open.
	 * 
	 * TODO Method exposes the same functionality of the (private) csvIterator.hasNext() with some overhead, so is not
	 * used within the class. Implicit file opening may cause unwanted side-effects.
	 * 
	 * FIXME deprecate this method and build a sensible iterator interface for this class
	 * 
	 * @return true if the CSV file has a next row.
	 */
	public boolean hasNext() {
		lazyReadInit();
		// if (reader == null) reader = new ReadCSVFile(completePath + ".csv");
		// if (csvIterator == null) csvIterator = reader.iterator();
		return csvIterator.hasNext();
	}

	/**
	 * Method to open a CSV file for reading
	 * 
	 * This method initializes the ReadCSVFile (see Jerboa docs) and related iterator objects, actually opening the
	 * file.
	 * 
	 * TODO rework hasNext to use this, or remove it; add sanity checks to avoid mixing read/write modes
	 * TODO refactor file extension as Class field ????
	 */
	private void lazyReadInit() {
		
		if (reader == null) reader = new ReadCSVFile(completePath + /*".csv"+*/ Enums.getFileExtension(this.compression), this.compression); // if not found -> stacktrace
		if (csvIterator == null) csvIterator = reader.iterator();
	}

	/**
	 * Awfully compare the definition of two headers.
	 * 
	 * @param h1
	 *            a collection of Strings with the field names
	 * @param h2
	 *            ditto
	 * @return true if the headers match
	 */
	private boolean compareHeaders(Collection<String> h1, Collection<String> h2) {
		// we trust that there are n duplicate fields to mess up things
		return (h1.size() == h2.size() && h1.containsAll(h2));
	}

	/***
	 * Loads in memory a batch of rows (of specified size) from a CSV file, into the CSVFile object. If the file is
	 * empty it almost silently turns into a no-op.<br>
	 * 
	 * Implementation assumes that header and mappedHeader are either both initialized or both undefined.
	 * 
	 * FIXME No header checking memory,: if the header is not in memory we assume to be at the beginning of the file; there is
	 * no checking for errors or returning exceptions!<br>
	 * FIXME improve open file management; right now, if the header is
	 * not there we assume the reading cursor must be initialized<br>
	 * 
	 * @param batchSize
	 *            The number of rows to be read.
	 */
	public void loadBatch(int batchSize) {
		LogST.logP(2, "CSV File - getting CsvReader");
		lazyReadInit();
		// first element should be the header if we have no header in memory
		/**
		 * FIXME what if I defined the header and the file does not match it?
		 * it simply bombs; but if the header is a permutation, we will reorder the data before returning
		 */
		initialBatchSize = batchSize; // remember this for the cursor
		if (this.header.isEmpty()) { // header and mappedHeader are both null or defined

			// class field initialization
			LogST.logP(1, "CSV File - loading mappedHeader");
			if (csvIterator.hasNext()) {
				List<String> header = csvIterator.next();
				this.mappedHeader.addAll(header);
				LogST.logP(1, "CSV File - mappedHeader loaded");

				// FIXME
				// this routine WILL use schema.attributes() in the future, when type info is available in the CSV
				// do the real schema comparison

				if (!compareHeaders(schema.fieldNames(), mappedHeader)) {
					LogST.logP(0, "ERROR: CSVFile.loadBatch() for file " + this.completePath
							+ " header does not match schema\n " + "header: " + mappedHeader.toString() + "\nschema: "
							+ schema.fieldNames());
					throw new Error("CSVFile.loadBatch() header does not match schema -- see timeline.txt");
				} else {
					LogST.logP(1, "CSVFile.loadBatch() for file " + this.completePath
							+ " mappedHeader matches schema\nmappedHeader: " + mappedHeader.toString() + "\nschema: "
							+ schema.fieldNames());
				}

				rowCursor = 0; fileRowCursor=0;
			}
			else
				LogST.logP(-1, "CSV File - No header found - file is empty");
		}

		// process the file body. If not first batch, we just continue from where the file iterator was left.

		int countRow = 0;
		if (this.content.size() == 0) {
			this.content = new ArrayList<ArrayList<String>>();
			for (int i = 0; i < this.mappedHeader.size(); i++) {
				content.add(new ArrayList<String>());
			}
		}
		else
			for (ArrayList<String> al : this.content)
				al.clear();
		if (this.content.size() != this.mappedHeader.size())
			throw new ArrayIndexOutOfBoundsException("loadBatch() mismatch between mappedHeader and CSVFile : " + mappedHeader.size()
					+ " in mappedHeader, " + content.size() + " in data");

		rowsInBuffer = 0;
		LogST.logP(3, "CSV File - trying to load " + batchSize + " row into the body");
		while (csvIterator.hasNext() && (countRow < batchSize)) {
			List<String> l = csvIterator.next();

			if (l.size() != this.mappedHeader.size()) {
				LogST.logP(-1, "Warning: CSVFile -- wrong number of fields " + l.size()
						+ "(unquoted commas?), discarded row " + l);
				discardedRowCount++; 
				fileRowCursor ++; // keep file position consistent
				continue; // skip this row, as it is incorrectly formatted
			}
			// throw new ArrayIndexOutOfBoundsException ("loadBatch() Too many columns for CSVFile :"+l.size()+
			// "in row"+l);

			int i = 0;
			for (String s : l) {
				this.content.get(i).add(s);
				i++;
			}
			countRow++;
		}
		rowCursor = 0; // reset reading cursor, but not fileRowCursor
		rowsInBuffer = countRow; // new content of the buffer

		
		LogST.logP(3, "CSV File - body loaded with " + rowsInBuffer + " rows");

		//reorder mappedHeader to header (who sets this?)
		header=schema.fieldNames();
		if (!header.equals(mappedHeader))
		{ // this can only be if the fields are permuted
			int i;
			ArrayList<ArrayList<String>> sortedContent= new ArrayList<ArrayList<String>>();
			for (String field : header)
			{
				i = mappedHeader.indexOf(field);
				if (i < 0) 
				{ 
					LogST.logP(-1, "CSVFile.loadBatch() reordering schema:\n"+schema.fieldNames()+"\n from header\n"+mappedHeader.toString());
					throw new IndexOutOfBoundsException(
							"CSVFile.loadBatch() unexpected field name in reordering");
				}
				sortedContent.add(content.get(i)); // place that data column in its place
			}
			content=sortedContent; //replace the main ArrayList
		}
		LogST.logP(1, "CSV File - schema reordered, csv structure ready for use");
	}

	/**
	 * Get the number of rows discarded so far when reading a CSV file, because of field number mismatches.
	 * @return the number of rows discarded so far when reading a CSV file
	 */
	public int getDiscardedRowCount() {
		return discardedRowCount;
	}

	/**
	 * Implements the read operation for an iterator over the data in memory; can trigger loading a new batch.
	 * 
	 * FIXME add schema handling and proper return value for method
	 * 
	 * @return a row in the data (as a List of Objects, needs to change).
	 */
	public List<String> next() {

		if (rowCursor < rowsInBuffer) {
			ArrayList<String> returnList = new ArrayList<String>();
			for (int field = 0; field < content.size(); field++) {
				returnList.add(content.get(field).get(rowCursor));
			}
			rowCursor++; fileRowCursor++;
			// strange issues debugging
			// if (rowCursor>115 && rowCursor <118)
			// {
			// LogST.logP(3, "CSVFile.next returning row "+rowCursor+" with "+returnList.size());
			// } //end of debug code
			return returnList; // FIXME unfinished - we still return empty List if schema is empty
		}
		else {
			// clear the buffer
			loadBatch(initialBatchSize); // load another batch the same size
			if (rowsInBuffer > 0) // if we got data
				return next(); // calls itself once
			else
				return null;
		}
	};

	/**
	 * Implements the next-row existence check to iterate over the the data in memory; can trigger loading a new batch.
	 * Will replace hasNext()
	 * 
	 * @return the next-row existence
	 * @throws NullPointerException 
	 */
	public boolean hasMore() throws NullPointerException {
		if (rowCursor < rowsInBuffer)
			return true; // simple case
		else if (csvIterator != null) // we need to check if we can load at least one row for next batch
			return csvIterator.hasNext();
		else
			throw new NullPointerException("csvIterator not initialized");
	};

	/**
	 * Dump the CSV file to disk, using path and file name already specified at CSVFile object creation time. Calls the
	 * more general form saveTo().
	 * 
	 * @param append
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void save(boolean append) throws NoSuchAlgorithmException, IOException, JAXBException {
		saveTo(null, null, append);
	}

	/**
	 * Save the data to a CSV file on disk; this really needs some more documentation. <br>
	 * 
	 * It creates the header for CSV with specified path and name, then dumps data to the file. Can append to an
	 * existing file, skipping header creation if the file exists already and skip BufferedWriter definition too. Note that existing file correctness is not
	 * questioned: in append mode, data without header will happily trail to a an existing but empty file or to a file
	 * with garbage content. <br>
	 * To fix the behaviour of mapping IAD attributes with no source LIAD attributes, a non-null mappedHeader is used
	 * instead of the ordinary header at save time.
	 * 
	 * FIXME There was an "undocumented feature": <b>only</b> in append mode the in-memory data are cleared after the
	 * save. So the first block of a long file is duplicated. The cleanup is now always enforced, need to double-check
	 * with the various callers if the new behavior is ok.<br>
	 * FIXME refactor the class to reduce the amount of special cases and duplicate functions.
	 * 
	 * @param path
	 *            path of the file.
	 * @param fileName
	 *            filename to use.
	 * @param append
	 *            append to existing file, and do generate the header only if the file does not actually exists.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws JAXBException
	 */
	// if append is true and file exists, the method assumes that header has
	// been
	// already written before

	public void saveTo(String path, String fileName, boolean append) throws NoSuchAlgorithmException, IOException,
			JAXBException {

		LogST.logP(1, "CSVFile.saveTo() called with |"+path+"|"+fileName+"|"+append+"\n on CSVfile "+this.getCompletePath());
		
		ArrayList<String> workHeader; // FIXME this intentionally shadows the class field, refactor
		try {
			if (this.header.size() == 0) throw new Error("CSVFile.saveTo() -- Empty header error");

			if (mappedHeader.size() > this.header.size())
				workHeader = this.mappedHeader;
			else
				workHeader = this.header;

			// Open file and write header if needed
			String extension = Enums.getFileExtension(this.compression);
			String finalPath = "";
			if (path == null || fileName == null) {
				finalPath = completePath;
			}
			else {
				finalPath = path + fileName;
			}
			
			if (!append) { // this is the correct behavior, either append or truncate
				LogST.logP(0, "CSV File - writing header");
				boolean exists;
				switch (this.compression){
					case NONE:
						LogST.logP(1, "CSV File - open BufferedWriter on file " + finalPath + /*".csv"+*/ extension);
						// we don't really use the fact that the file is existing anymore
						exists = new File(finalPath + extension).exists();
						LogST.logP(0, "CSV File - file " + finalPath + /*".csv"+*/ extension + " is existing: " + exists);
						bufferedWriter = new BufferedWriter(
			                     new OutputStreamWriter(
			                         new FileOutputStream(finalPath + /*".csv"+*/ extension, append)) 
			                     ,10000 // FIXME why 10000?
			                     );
						break;
					case GZIP:
						LogST.logP(1, "CSV File - open BufferedWriter on file " + finalPath + /*".csv"+*/ extension);
						// we don't really use the fact that the file is existing anymore
						exists = new File(finalPath + /*".csv"+*/ extension).exists();
						LogST.logP(0, "CSV File - file " + finalPath + /*".csv"+*/ extension+" is existing: " + exists);
						bufferedWriter = new BufferedWriter(
			                     new OutputStreamWriter(
			                         new GZIPOutputStream(new FileOutputStream(finalPath + /*".csv"+*/ extension, append))
			                     ),10000); // FIXME why 10000?
						break;
					case ZIP:
						LogST.logP(1, "CSV File - open BufferedWriter on file " + finalPath + /*".csv"+*/ extension);
						// we don't really use the fact that the file is existing anymore
						exists = new File(finalPath + /*".csv"+*/ extension).exists();
						LogST.logP(0, "CSV File - file " + finalPath + /*".csv"+*/ extension+" is existing: " + exists);
						zipOutStream = new ZipOutputStream(new FileOutputStream(finalPath + /*".csv"+*/ extension, append));
						zipEntry = new ZipEntry(this.fileName + ".csv");
						zipOutStream.putNextEntry(zipEntry);
						bufferedWriter = new BufferedWriter(
			                     new OutputStreamWriter(zipOutStream
			                         )
			                     ,10000); // FIXME why 10000?
						
						break;
				}
				// this generates the headers for the file AND for the logs
				String logM="";
				for (int i = 0; i < workHeader.size(); i++) {
					String h = workHeader.get(i);
					logM=logM+" ("+i+") "+h;
					bufferedWriter.write(h);
					if (i == workHeader.size() - 1) {
						bufferedWriter.write("\n");
					}
					else {
						bufferedWriter.write(",");
					}
				}
				LogST.logP(1, "CSV File built header :"+logM);
			}


			// Write data to the file

			// FIXME: ATTENZIONE SI ASSUME CHE TUTTE LE TABELLE CHE CONCORRONO
			// NELLA CREAZIONE DI UN NUOVO DATASET ABBIANO LO STESSO NUMERO DI
			// RIGHE SU CUI LAVORARE
			// PER QUESTO BASTA LA LUNGHEZZA DI UNA COLONNA POICHÈ SARÀ UGUALE
			// PER TUTTE
			if (!this.content.isEmpty()) {

				int rowCount = this.content.get(0).size();
				int columnCount = workHeader.size(); // intended aliasing
				LogST.logP(3, "CSV File - writing body");
				LogST.logP(3,
						"CSV File - header reports " + columnCount + " columns, content reports " + this.content.size()
								+ " columns " + rowCount + " rows");
				for (int i = 0; i < rowCount; i++) {
					// LogST.logP(2,"CSV File - row n°" + i + " maxRow " +
					// rowCount + " columnCount " + columnCount);
					for (int j = 0; j < columnCount; j++) {
						// LogST.logP(2,"content: " + this.content.size());
						// ArrayList<String> p = this.content.get(j);
						// LogST.logP(2,"p: " + p.size());
						String body = this.content.get(j).get(i);
						// LogST.logP(2,"CSV File - column n°" + j +
						// " value: " + body);
						if (body != null) 
							bufferedWriter.write(body);
						// bw.write("null"); MAPORCAP(&^%&*^%*&^%*&
						// else

						if (j == columnCount - 1) {
							bufferedWriter.write("\n");
						}
						else
							bufferedWriter.write(",");
					}
				}
				LogST.logP(3, "CSV File - wrote " + rowCount + " rows");

				// final cleanup

				LogST.logP(3, "CSV File - cleaning temp structure");
				// FIXME the if was removed, we ALWAYS clear the body data after a save operation
				// if (append) {
				for (ArrayList<String> al : this.content)
					al.clear();
				// }

			} // end if (content empty)

		}

		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Unexpected exception in CSVFile.save() for "+this.fileName+": " + e.toString());
		}
		bufferedWriter.flush();
	}

	/**
	 * Returns true if both the .csv (or .csv.gzip or .csv.zip) and .xml files exist for the specified dataset. datasetName must not be empty BUT
	 * it is not checked.
	 * If exist more files with the same name throw Error
	 * @param path
	 *            The path in which to search the dataset file.
	 * @param datasetName
	 *            The name of the dataset.
	 * @param ignoreXML 
	 * 
	 * @return a <code>Boolean</code> value, true if both conditions are met, false otherwise.
	 */
	public static boolean checkExistence(String path, String datasetName, boolean ignoreXML) {
		String tmp = path + datasetName;
		
		String csvPath = tmp + ".csv";
		String csvGzipPath = tmp + ".csv.gzip";
		String csvZipPath = tmp + ".csv.zip";
		String xmlPath = tmp + ".xml";

		File f = new File(xmlPath);
		File f1 = new File(csvPath);
		File f2 = new File(csvGzipPath);
		File f3 = new File(csvZipPath);
		int howMany = 0;
		howMany = (f1.exists()?1:0)+(f2.exists()?1:0)+(f3.exists()?1:0);
		if (ignoreXML || f.exists()) {
			if (howMany==1){
				return true;
			} else if (howMany ==0) {
				return false;
			} else {
				throw new Error("Duplicated file with name "+datasetName+" found");
			}
		}
		return false;
	}

	/**
	 * DOCUMENT ME
	 * 
	 * @param path
	 * @param datasetName
	 * @return
	 */
	public static boolean checkExistence(String path, String datasetName) {
		return checkExistence(path, datasetName, false);
	}

	/**
	 * Validates the checksum of the xml related to a CSV file. Heavy checksum computation can be disabled in the XML
	 * configuration.
	 * 
	 * @param path
	 * @param datasetName 
	 * @return true if control succeeds.
	 * @throws JAXBException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static boolean validateCheckSum(String path, String datasetName) throws JAXBException,
			NoSuchAlgorithmException, IOException {
		
		JAXBContext context = JAXBContext.newInstance(CsvDescriptor.class);
		Unmarshaller m = context.createUnmarshaller();
		CsvDescriptor csvDesc = (CsvDescriptor) m.unmarshal(new File(path + datasetName + ".xml"));
		String extension = Enums.extensionOfFileExists(path, datasetName);
		if (extension.equals("")) { throw new IOException ("CSVFile.validateCheckSum() missing file "+path+datasetName); }
		String checksum = computeChecksum(2, path + datasetName /*+ ".csv"*/+extension);

		if (checksum.equals(csvDesc.getMetaChecksumLight())) {
			if (ConfigSingleton.getInstance().theMatrix.getSkipCSVFullChecksums()) return true;
			checksum = computeChecksum(-1, path + datasetName /*+ ".csv"*/+extension);
			if (checksum.equals(csvDesc.getMetaChecksumHard())) { return true; }
		}

		return false;
	}

	/**
	 * Validates the checksum of the xml related to a CSV file with compression type parameter specified. Heavy checksum computation can be disabled in the XML
	 * configuration.
	 * 
	 * @param path
	 * @param datasetName 
	 * @param compressionType 
	 * @return true if control succeeds.
	 * @throws JAXBException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static boolean validateCheckSum(String path, String datasetName, CompressionType compressionType) throws JAXBException,
	NoSuchAlgorithmException, IOException {
	JAXBContext context = JAXBContext.newInstance(CsvDescriptor.class);
	Unmarshaller m = context.createUnmarshaller();
	CsvDescriptor csvDesc = (CsvDescriptor) m.unmarshal(new File(path + datasetName + ".xml"));
	
	String checksum = computeChecksum(2, path + datasetName + /*".csv"+*/Enums.getFileExtension(compressionType));
	if (checksum.equals(csvDesc.getMetaChecksumLight())) {
		if (ConfigSingleton.getInstance().theMatrix.getSkipCSVFullChecksums()) return true;
		checksum = computeChecksum(-1, path + datasetName + /*".csv"+*/Enums.getFileExtension(compressionType));
		if (checksum.equals(csvDesc.getMetaChecksumHard())) { return true; }
	}
	
	return false;
	}
	
	
	/**
	 * method to create a meta.xml file related to the CSV dump with compression type parameter specified.
	 * 
	 * FIXME deprecated as it misses the schema, which is now mandatory
	 * @param path 
	 * @param fileName 
	 * @param version 
	 * @param compressionType 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * 
	 * @throws JAXBException
	 */
	@Deprecated
	public static void createMetaXml(String path, String fileName, String version, CompressionType compressionType) 
			throws IOException, NoSuchAlgorithmException, JAXBException 
	{
		createMetaXml(path, fileName, version, new DatasetSchema(""), compressionType);
	}

	/**
	 * method to create a meta.xml file related to the CSV dump
	 * 
	 * FIXME deprecated as it misses the schema, which is now mandatory
	 * @param path 
	 * @param fileName 
	 * @param version 
	 * 
	 * @throws JAXBException
	 * @throws IOException if file doesn't exists
	 * @throws NoSuchAlgorithmException 
	 */
	@Deprecated
	public static void createMetaXml(String path, String fileName, String version) 
			throws IOException, NoSuchAlgorithmException, JAXBException 
	{
		createMetaXml(path, fileName, version, new DatasetSchema(""), CompressionType.NONE);
	}

	
	/**
	 * FIXME document this method
	 * 
	 * @param path
	 * @param fileName
	 * @param version
	 * @param schema
	 * @param compressionType
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws JAXBException
	 */
	public static void createMetaXml(String path, String fileName, String version, DatasetSchema schema, CompressionType compressionType)
			throws IOException, NoSuchAlgorithmException, JAXBException
	{
		String finalPath = path + fileName;
		String completeName = finalPath + /*".csv"+*/ Enums.getFileExtension(compressionType);
		
		// compute the checksum
		String sha1Hard = computeChecksum(-1, completeName);
		String sha1Light = computeChecksum(2, completeName);
		
		// set up the csv descriptor
		CsvDescriptor csvDesc = new CsvDescriptor();
		csvDesc.setChecksumHard(sha1Hard);
		csvDesc.setChecksumLight(sha1Light);
		csvDesc.setTheMatrixVersion(version);
		csvDesc.setTimestamp(new Date().toString());
		csvDesc.setJson(DatasetSchema.toJSON(schema));
		
				
		// write the actual content on disk
		JAXBContext context = JAXBContext.newInstance(CsvDescriptor.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);		
		marshaller.marshal(csvDesc, new File(finalPath + ".xml"));
	}
	
	
	
	/***
	 * Computes the checksum for the specific file. <br>
	 * 
	 * TODO directly passing the number of rows to check, or -1, is highly unsafe. Quick-fixed, refactor and change it
	 * with an enum or a boolean.
	 * 
	 * @param type
	 *            Specify the number of row you want to use to calculate checksum. Lightweight checking (2), or full
	 *            file checking (-1)
	 * @param filename
	 *            the file name over which to compute the checksum
	 * @return the MD5 checksum converted to a String
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String computeChecksum(int type, String filename) throws NoSuchAlgorithmException, IOException {

		if (type != 2 && type != -1) throw new NoSuchAlgorithmException("Illegal type value in computeChecksum");

		// fake md5 is returned if checksum computation is disabled
		if (type == 2 && ConfigSingleton.getInstance().theMatrix.getSkipCSVFullChecksums())
			return "00000000abadf00d00000000abadf00d";

		InputStream fis = new FileInputStream(filename);
		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead = 0;
		int count = 0;
		while (numRead != -1 && count != type) {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
			count++;
		}

		fis.close();
		String result = "";
		byte[] md5 = complete.digest();
		for (int i = 0; i < md5.length; i++) {
			result += Integer.toString((md5[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public String getVersion() {
		return version;
	}

	/**
	 * Set the reference schema for this CSVFile object; call this function before reading in CSV data if you want to
	 * check the schema matches the CSV header. Currently calling this function is <b>mandatory</b>.
	 * 
	 * @param s
	 */
	public void setSchema(DatasetSchema s) {
		schema = s;
	}
	
	/**
	 * @author marco
	 * Method closeFile. Close the CSV File and set bufferedWriter instance to null
	 * @throws IOException 
	 */
	public void closeFile() throws IOException{
		LogST.logP(1, "CSV File - closing BufferedWriter");
		switch (this.compression){ //FIXME what about GZIP ?
		case ZIP:
			zipOutStream.close();
			bufferedWriter.close();
			bufferedWriter=null;
			break;
		default:		// including case GZIP:
			bufferedWriter.close();
			bufferedWriter=null;
			break;
		}
		LogST.logP(0, "CSVFile.closeFile() done for "+this.fileName);
	}
}
