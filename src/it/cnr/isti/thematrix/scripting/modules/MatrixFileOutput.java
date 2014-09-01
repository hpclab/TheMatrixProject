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
package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.common.Enums;
import it.cnr.isti.thematrix.common.Enums.ChecksumType;
import it.cnr.isti.thematrix.common.Enums.CompressionType;
import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import it.cnr.isti.thematrix.scripting.utils.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class defining the behaviour of an output module. It currently builds a file
 * name and opens a CSV file in <code>setup()</code>, then pulls an input row to
 * disk at each <code>next()</code> call. By convention the name of the module
 * is the same as name of the output file. Suffix-trimmed field baseName is used
 * to ensure the CSVFile doesn't add double suffixes.
 * 
 * FIXME this class does NOT write a file if no data rows are present (next() is
 * never called ) but should instead write a file containing the header (and
 * generate the xml descriptor).
 * 
 * This class will need to be reworked a bit to save the file to either the
 * results directory or the iad directory. Reworking needed because the file
 * name is currently decided at setup() time, so it can be changed only if the
 * module is created manually. <br>
 * INTEDED BEHAVIOUR: if the module is defined within the script, it saves to
 * results/ ; if it is added automatically, it saves to iad/. In both cases the
 * cache will be able to track the file. <br>
 * CURRENT BEHAVIOUR: all is saved to IAD, which is the default. Code is already
 * in place for changing the default.
 * 
 * Behaviour modified to treat the ".csv" suffix in a special way: module will
 * no longer create ".csv.csv" files. <br>
 * FIXME patching in progress, the xml descriptor becomes mandatory (always
 * present) and contains also schema infomration
 * 
 * @author massimo
 */
public class MatrixFileOutput extends MatrixModule 
{
	private static final long serialVersionUID = 209888237452139707L;

	private MatrixModule inputModule;
	CSVFile outputCSV = null;
	/**
	 * number of rows in the internal file writer buffer (CSVFile) for the current batch
	 */
	private int rowsInBatch = 0;
	/**
	 * number of the current iterations (i.e. number of the file batch we are writing, with 0 the first) 
	 */
	private int iterationCount = 0;
	/**
	 * number of rows we already pushed to the file writer (CSVFile) since the beginning of data
	 */
	
	/**
	 * true if hasMore() has never been called, exploited to detect empty
	 * streams and save the file even if next() is never called
	 */
	private boolean atFirst = true;
	
	private int rowsTotal = 0;
	private String baseName; // stripped of .csv suffix
	private boolean postProcessed = false;
	// remember last hasMore to avoid trashing the module chain
	private boolean cachedHasMore = false;
	private boolean cacheValid = false;

	/**
	 * If true, the module will use the results path instead of the IAD path.
	 * Currently unused, will be set to true only for module which are
	 * automatically created after the script parsing (thus usually being true
	 * only for the load scripts). We will then need to change the default value
	 * to true.
	 */
	private boolean saveToResultsPath = true;

	/**
	 * doChecksum : indicates type of checksum for the file
	 */
	private ChecksumType doChecksum = ChecksumType.MD5;

	/**
	 * doCompression : indicates type of compression for read/write the file
	 */
	private CompressionType doCompression = CompressionType.NONE;

	// Constructor for no checksum and no compression
	public MatrixFileOutput(String name, MatrixModule inputModule) {
		this(name, inputModule, inputModule.getSchema().name,
				ChecksumType.NONE, CompressionType.NONE);
	}

	// Constructor for specifying the module by name
	public MatrixFileOutput(String name, String inputModule, String schema, ChecksumType checksum, CompressionType compression) {
		this(name, TheMatrixSys.getModule(inputModule), schema, checksum,
				compression);
	}

	// Constructor for specifying the module with the object
	public MatrixFileOutput(String name, MatrixModule inputModule,
			String schema, ChecksumType checksum, CompressionType compression) {
		super(name);
		this.inputModule = inputModule;
		this.inputModule.schemaMatches(schema);
		
		// set schema of the module
		this.setSchema(inputModule.getSchema());
		
		this.inputModule.addConsumer(this);
		this.doCompression = compression;
		this.baseName = Enums.getBaseNameFile(name);		
	}

	/**
	 * Call this method before setup to enable/disable use of the Results path.
	 * Note that this can only be used on modules manually created after the
	 * eval, because the interpreter always calls setup().
	 * 
	 * @param b
	 *            value stored in field <code>saveToResultsPath</code>
	 */
	public void setSaveToResultsPath(boolean b) 
	{
		// throws WARNING if file is already initialized
		if (outputCSV != null) 
		{
			LogST.logP(0, this.getClass().getSimpleName()+".setSaveToResultsPath() WARNING : ignored, " +
					"file was already initialized for module" + name);
		}
		
		LogST.logP(0, this.getClass().getSimpleName()+".setSaveToResultsPath() set to: "+b);
		saveToResultsPath = b;
	}

	@Override
	public void setup() 
	{
		/*
		 * NOTE: the code that was here has been moved into the method
		 * beforeFirstHasMore().
		 */
	}

	/*
	 * As the name suggests, this method is to be called before the first hasMore.
	 * It creates the file and writes the header. 
	 */
	private void beforeFirstHasMore()
	{
		LogST.logP(0, this.getClass().getSimpleName()+".saveToResultsPath: "+saveToResultsPath);
		
		if (saveToResultsPath) {
			outputCSV = new CSVFile(Dynamic.getResultsPath(), this.baseName,
					"", this.doCompression);
		} else { // default case now
			outputCSV = new CSVFile(Dynamic.getIadPath(), this.baseName, "",
					this.doCompression);
		}

		LogST.logP(0, this.getClass().getSimpleName()+".beforeFirstHasMore() created outputCSV in "+ outputCSV.getPath());
		
		// create a header for the file starting from the schema
		List<Symbol<?>> schema = inputModule.getSchema().attributes();
		Collection<String> newHeader = new ArrayList<String>();
		for (Symbol<?> field : schema) {
			newHeader.add(field.name);
		}
		outputCSV.setHeader(newHeader);
		/*
		 * FIXME current version of the header does not save column type
		 * information; this is needed if we want to avoid having to fully
		 * specify all the schemata in all the scripts (poor maintainability)
		 * and if we want (in the future) provide automatic column selection
		 * based on name and type information (original plan).
		 */
	}
	
	@Override
	public void exec() {
	}

	@Override
	public List<Symbol<?>> attributes() {
		return this.inputModule.attributes();
	}

	@Override
	public Symbol<?> get(Object name) {
		return super.get(name);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeInput(MatrixModule m) {
		if (outputCSV != null)
			throw new Error(
					"MatrixFileOutput.changeInput() not allowed after setup() "
							+ this.name);
		inputModule = m;

		// FIXME what if we are after setup() ? do we need to do setHeader and the like? 
		this.inputModule.schemaMatches(inputModule.getSchema().name); 
	}

	/**
	 * I am not terribly fond of all this computation in a boolean method TODO
	 * check whether it's really the best way
	 * 
	 * 
	 */
	@Override
	public boolean hasMore() 
	{
		if (atFirst)
			this.beforeFirstHasMore();
		
		if (cacheValid)
			return cachedHasMore;
		
		cacheValid = true;
		cachedHasMore = inputModule.hasMore();
		
		/**
		 * if cachedHasMore is false at the beginning of the file, we must
		 * create the empty file anyway, and its XML descriptor
		 **/
		if(cachedHasMore==false && atFirst) { 
			LogST.logP(0, "MatrixFileOutput.hasMore() triggered writeEmptyFile() for file "+this.baseName);
			writeEmptyFile(); 
		}
		
		atFirst=false;
		return cachedHasMore;
	}

	/**
	 * This method internally substitutes the hasMore() call, letting the call
	 * through only if we don't already have a value stored.
	 * 
	 * @return cached value of hasMore(), if still valid.
	 */
	private boolean hadMore() {
		if (cacheValid)
			return cachedHasMore;
		else
			return hasMore();
	}

	/**
	 * The next method is calling an extra hasMore each round (for safety) which
	 * will become a problem as OutputModules now can chain due to the
	 * BufferFileModule, so the amount of extra useless call can get quadratic
	 * in the lenght of the chain. We avoid this by recording the result of the
	 * last hasMore call and returning it until the next next().
	 * 
	 */
	@Override
	public void next() 
	{
		// prepare one batch of rows; I am assuming the order is kept the same
		// as in the Schema
		if (hadMore()) {
			inputModule.next();
			cacheValid = false;
			int columnCount = 0;

			for (Symbol<?> field : inputModule.attributes()) {
				outputCSV.setValue(columnCount,
						StringUtil.symbolToString(field));
				columnCount++;
				// maybe add some checks here? if one column skips we mangle all
				// the data
			}
		}
		if (rowsInBatch >= Dynamic.prefetchCSVSize || !(hadMore())) {
			try {
				outputCSV.save(iterationCount > 0); // truncate file at the
													// first iteration <=>
													// append only after
				// FIXME what if we have 0 rows in batch but we are at EOF?
				// check within CSVFile what happens

				rowsTotal += rowsInBatch;
				rowsInBatch = 0;
				iterationCount++;

				if (!(hadMore())) {
					// close the streams
					outputCSV.closeFile();
					
					// write the xml
					CSVFile.createMetaXml(outputCSV.getPath(), outputCSV.getFileName(), TheMatrixSys.TheMatrixProgramVersion, 
							this.inputModule.getSchema(), doCompression);

				}

			} catch (Exception e) {
				LogST.logP(0, "CSV output - Unexpected I/O Exception " + e);
				LogST.logException(e);
				throw new Error(e);
			}
		}
		rowsInBatch++;
	}

	/**
	 * This method will write an empty file in case the input is empty (we still
	 * need the header and the xml descriptor for future use)
	 */
	private void writeEmptyFile() {
		try {
			if (outputCSV == null) throw new NullPointerException("MatrixFileOutput.outputCSV is null");

			// we are not appending, create the file and closes it
			//FIXME either add xml code, or create a writeheader function in CSV file
			outputCSV.save(false);
			outputCSV.closeFile();

			CSVFile.createMetaXml(outputCSV.getPath(), outputCSV.getFileName(), TheMatrixSys.TheMatrixProgramVersion, 
					this.inputModule.getSchema(), doCompression);
			

		} catch (Exception e) {
			LogST.logP(0, "CSV output - Unexpected I/O Exception " + e + " " + e.getMessage());
			LogST.logException(e);
			throw new Error(e);
		}
		
	}
	

	
	/**
	 * The total number of records written so far by this Output module
	 * @return The total number of records written so far by this Output module
	 * 
	 */
	public int getRowsTotal() {
		return rowsTotal;
	}

	/**
	 * Tells if the module has been added/edited during postprocessing in such a
	 * way that it needs to be ignored afterwards. Quick and dirty
	 * 
	 * FIXME quick notes about postprocessing the module graph
	 * 
	 * the method isPostProcessed() returns true if there was a previous call to
	 * set PortProcessed for this module; default behaviour is thus having it
	 * false.
	 * 
	 * @return true if this module is marked as a result of postprocessing.
	 */
	public boolean isPostProcessed() {
		return postProcessed;
	}

	/**
	 * set the postProcessed flag, telling further postprocessing to ignore this
	 * output module
	 */
	public void setPostProcessed() {
		// should check that we are not too late to set it... but it's a hack
		// anyway
		postProcessed = true;
	}

}
