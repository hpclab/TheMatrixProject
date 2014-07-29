package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.common.Enums.ChecksumType;
import it.cnr.isti.thematrix.common.Enums.CompressionType;
import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.mapping.utils.TempFileManager;
import it.cnr.isti.thematrix.scripting.sys.DatasetRecord;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Module implements single-writer, multiple-reader buffering on a disk file (with stream replication) between TheMatrix
 * modules. The module is inserted whenever appropriate by postprocessing the module graph before script execution, and
 * is designed to share the same interface and use of BufferModule as well as the BufferProxyModule class. You can still
 * set a limit for the buffer size, but the check is currently unimplemented.<br>
 * 
 * The readers interface to the buffer via a BufferProxyModulem so they can be identified. Ordinary
 * <code>hasMore()</code> and <code>next()</code> won't work here.<br>
 * 
 * Module name does not start with TheMatrix to underline that this module is NOT available in the programming syntax.
 * Implements buffer wit multiple read-from pointers in a temporary file; uses TheMatrix <code>hasMore(); next()</code>
 * conventions; needs to serialize and deserialize, builds additional file input and output modules to reuse safe code.
 * Does call the input when a reader asks data, and pulls the whole input in one go to the temporary file..
 * 
 * Invariant: for the slowest reader, we have <br>
 * (reader=writer) pointers means buffer is empty, and reader location is invalid;<br>
 * writer=reader+bufferSizeMax-1 means buffer full; we waste one position this way, but it's safer.<br>
 * 
 * Notes on modules: we use the consumers List of MatrixModule in a non-standard way, to keep track of the receivers
 * when calling their setAll() ; the output module that wirtes on disk is marked with setPostProcessed to avoid
 * interference with the ordinary output pull mechanism<br>
 * 
 * Notes: keeps integer "pointers" e.g. offsets in the file; defines internal methods to compare, round indexes,
 * update/compute bufferSize; but implementation is not working for the BufferFileModule<br>
 * 
 * @author massimo
 * 
 */

public class BufferFileModule extends MatrixModule {

	/* hashmap of receivers, computed from source module */
	/* number of receivers, to avoid calling bufferSize() all the time */
	private int numReaders;

	/**
	 * Single index for writing i.e. offset in file; grows from 0 to MAXINT. Invariants:
	 * <code>writePointer<=readPointer[i]<=writePointer+bufferMaxSize </code> for any i.
	 */
	private int writePointer;

	/**
	 * Array of indexes, i.e. offsets in the file. They grow from 0 to MAXINT. Each points to the active (valid)
	 * location, unless it is equal to writePointer.
	 */
	private int readPointer[];

	/* the minimum of the reading pointers, constantly updated after each read operation */
	private int leastRpointer;

	/* array of received data (we MUST make a copy here) */
	private DatasetRecord buffer[];
		
	/**
	 * We use a standard module to write to disk
	 */
	MatrixModule writerModule =null;
	
	/**
	 * We use an array of standard readers to read the file buffer
	 */
	MatrixModule readerModules[];

	/* the bufferSize of the buffer (active part with data) */
	// private int bufferSize; might same some additions... whatever, this is Java.

	/* the bufferSize of the underlying buffer array must be a power of two */
	private int bufferMaxSize;

	/* our input */
	private final MatrixModule input;
	

	/**
	 * Constructor that builds a Buffer Module AND adds it to the module chain,
	 * assumes the module chain is already built by the neverlang parser. Will
	 * patch its input source module, generate the necessary Proxy modules, link
	 * them to the buffer and to their receivers. The BufferFileModule also
	 * builds standard input and output file modules to reuse them for
	 * reading/writing the temporary file.
	 * 
	 * FIXME most of the stuff done in the constructor is actually
	 * postprocessing the module graph, and should use common functions defined
	 * somewhere else
	 * 
	 * @param name
	 *            Name of the BufferFileModule
	 * @param inputTable
	 *            Name of the input module we use as source (it also provides
	 *            the receivers list)
	 * @param size
	 *            Size of the local buffer in number of records
	 */
	public BufferFileModule(String name, String inputTable, int size) {
		super(name);

		// initialize the local buffer and related variables

		if (size<=0) { size = Integer.MAX_VALUE-1; }
				
		// redundant, for clarity FIXME no longer needed?
		this.writePointer = 0;
		this.leastRpointer = 0;

		// relate to our input
		// we get the schema from input -- check if it may be defined differently at each one of the readers
		this.input = TheMatrixSys.getModule(inputTable);

		// FIXME (domanda Edoardo) if schema may be defined differently at each one of the readers we should check
		// compatibility here
		// input.schemaMatches(schemaName); ??

		// it seems we need to get the schema from our input in order to get it initialized in the proxies
		this.setSchema(input.getSchema());

		TheMatrixSys.addModule(this); // this seems unavoidable

		/****************** initialize the file name  ************/
		//set compression of buffer file
		
		File writeFile = TempFileManager.newTempFile();

		/****************************** initialize the readers *******************************/
		/**
		 * we can do that for input modules it works this way: file existence is checked on first read request (the
		 * mandatory hasmore()) and at that point we shall hook in the code reading the whole input to disk.
		 */
		// extract readers table from input and process them
		this.numReaders = input.getConsumers().size();
		readPointer = new int[numReaders]; // all zeroes, and we are fine with that
		readerModules = new MatrixModule[numReaders];
		int id = 0;
		// generate Proxies and connect them here
		/** 
		 * we build chains m-pm-im of m module, pm proxy module, im input module, 
		 * but the proxy will pass by this buffer in order to reach the right input
		 *
		 */
		for (MatrixModule m : input.getConsumers()) 
		{
					
			LogST.logP(1, "BufferFileModule() "+ this.name +" adding proxy to " + m.name);			
			// generate a proxy from m to here with identifier id
			BufferProxyModule pm = new BufferProxyModule(this.name, id, m.name);
			this.addConsumer(pm); // same order, so we can call consumers.get(id);
			TheMatrixSys.addModule(pm);
			LogST.logP(1, "BufferFileModule() "+ this.name +" added proxy "+pm.name+" to " + m.name);
			
			
			// changes the input on the target module m.
			// the method called is different whether is a module
			// with a single input or if has more
			if (m.hasOneInput())
				m.changeInput(pm);
			else
				m.substituteInput(input, pm);
			
			
			LogST.logP(1, "BufferFileModule() " + this.name +" adding new FileInput to " + pm.name);
			
			/**
			 * FIXME the 3rd parameter seems to be redundant, refactor MatrixFileInput
			 */
			// Create a file input module for each proxy; we set it to use a temporary file with the last parameter
			MatrixModule im = new MatrixFileInput(this.name+"_file_"+writeFile.getName()+"_"+id, writeFile.getName(), this.getSchema(), this.getSchema().fieldNames(), true);

			/*  FIXME why this difference?
			this.readSortedModule = new MatrixFileInput(this.name, tempFile.getName(), this.getSchema(),
					(List<String>) Collections.EMPTY_LIST);
			readSortedModule.setup();
			 */	
			
			// keep it
			readerModules[id] = im;
			// set up graph links so that we can work out what happens 

			im.addConsumer(pm); //each input module is consumed by its proxy

			// dependency on the writer module is addressed later

			TheMatrixSys.addModule(im);
			
			im.setup();
			LogST.logP(0, "BufferFileModule() "+this.name+" added input module "+im.name); 
			id++;
		}

		/****** back to the file writer module *********/
		
		// patch the source module to only have the writer of this buffer in the receivers; link _to_ source is this.input
		input.getConsumers().clear();
		// matrix file output adds itself to the preceding module!! (is this the correct behaviour?)
		// we create it last, and thus add it to the chain, because we need the input consumers list before.
		writerModule = new MatrixFileOutput (writeFile.getName(), this.input.name, this.input.getSchema().name, ChecksumType.NONE,Dynamic.bufferCompression);
		TheMatrixSys.addModule(writerModule); 	
		// input.addConsumer(writerModule); // redundant = crashes!
		LogST.logP(1, "BufferFileModule() "+this.name +" added "); 
		
		((MatrixFileOutput) writerModule).setPostProcessed(); // don't pull results from here
		((MatrixFileOutput) writerModule).setLogicalConsumer(this); //tell the system our intended output is a Buffer

		// InputModules of Proxies logically depend on the temp file
		writerModule.setLogicalConsumers(Arrays.asList(readerModules)); 
		
		writerModule.setup();
		LogST.logP(1, "BufferFileModule() "+this.name +" setup() done."); 
	}

	/**
	 * Compute the amount of valid data in the buffer for a specified reader
	 **/
	private int bufferSize(int caller) {
		// (writePointer - readPointer[caller]) < bufferMaxSize sempre, quindi & bufferMask qui e' inutile
		return (writePointer - readPointer[caller]);
	}

	/**
	 * Compute the number of buffer locations available for the writer.
	 **/
	private int bufferWriterSize() {
		// if this is not < bufferMaxSize-1, we got a problem
		if (!(writePointer - leastRpointer < bufferMaxSize - 1)) {
			LogST.logP(-1, "BufferFileModule.stepRpointer() buffer overfull for module"+this.name);
			throw new Error("BufferFileModule.bufferWriteSize() buffer overfull");
		}
		return (writePointer - leastRpointer);
	}

	/**
	 * A method to update the length of the buffer after a read and return it. Will increment the given readPointer and
	 * update the minimum value leastRpointer. Shall NOT be called if the readPointer[i] == writePointer.
	 * 
	 * FIXME now done in an ugly, trivial way, has cost O(numReaders) at each mimimum stream change --> O(numReaders)
	 * worst case.
	 **/
	private void stepRpointer(int reader) {
		// DEBUG: if read buffer empty crash
		if (readPointer[reader] == writePointer) {
			LogST.logP(-1, "BufferFileModule.stepRpointer() on empty read buffer - module "+this.name+" reader "+reader);
			throw new Error("BufferFileModule.stepRpointer() reader buffer is empty");
		}
		// we are increasing readPointer, we may need to update the minimum pointer
		if (readPointer[reader] != leastRpointer) {
			(readPointer[reader])++;
			return;
		} // easy case
		for (int i = 0; i < numReaders; i++) // we may have other pointers at the minimum
		{
			if (i == reader) continue;
			if (readPointer[i] == leastRpointer) {
				(readPointer[reader])++;
				return;
			} // we found another minimum
		}
		// since we reach here, that was the only minimum; so we increment both
		readPointer[reader]++;
		leastRpointer++;
		return;
	}

	@Override
	public void setup() {
		LogST.logP(0, "BufferFileModule.setup() for " + this.name);
	}

	@Override
	public void exec() {
		LogST.logP(2, "NO-OP BufferFileModule.exec() for " + this.name);
	}

	@Override
	public void reset() {
		throw new Error("BufferFileModule.reset(void) disallowed");
	}

	
	/**
	 * This call will attempt to read the whole input in the temporary file. It shall be called at run-time when input
	 * is first needed, not at setup() time.
	 */
	private void prepareBufferFile()
	{
		//open the file for writing
		while (writerModule.hasMore()) {
			writerModule.next();
			writePointer++;
		}
		LogST.logP(0, "BufferFileModule.prepareBufferFile() red " + writePointer + " rows from" + writerModule.name);
	}
	
	/**
	 * Implements the hasMore() functionality for a given BufferProxyModule which identifies by passing an integer
	 * 
	 * @param caller
	 *            the id of the caller
	 * @return the hasMore value
	 */
	public boolean hasMore(int caller) {
		printDebug("BufferFileModule "+this.name+" hasMore("+caller+")");
		// this means no data yet in the temporary file
		if (bufferSize(caller) == 0)
			prepareBufferFile();

		// if caller has data in local buffer, we already have data; but we ask the input module anyway in order to ensure that it is correclty initialized
		if (bufferSize(caller) > 0) return this.readerModules[caller].hasMore();

		// else: no data for reader, is the file empty?
		LogST.logP(0, "BufferFileModule "+this.name+" hasMore("+caller+") buffer is empty.");
		return false;
		//		throw new OutOfMemoryError("BufferFileModule.hasMore() buffer overfull");
	}

	/**
	 * Implements the next() functionality for a given BufferProxyModule, which identifies itself by passing an integer
	 * index.
	 * 
	 * @param caller
	 *            the id of the caller
	 */
	public void next(int caller) {
		printDebug("next("+caller+")");

		// it is mandatory to call hasMore() before next, so we know the file has been created
		
		// if caller has no data in buffer
		if (bufferSize(caller) > 0 ) { // we shall do a next() and a copy
			readerModules[caller].next(); // what if we get an error here?
			DatasetRecord r = DatasetRecord.emptyRecord(input); // FIXME is it the same emptyRecord(this) ?
			r.setAll(readerModules[caller]); // here we have the data row
			//LogST.logP(3,"Caller "+caller+" just read read: \n\t"+r); // Too much output
			this.getConsumers().get(caller).setAll(r); // push the data
			stepRpointer(caller); // step the counter; buffer may become virtually empty here
			return;
		}

		LogST.logP(-1, "BufferFileModule.next("+caller+") past last data for module "+this.name);
		printDebug("next("+caller+")");
		throw new Error("Read from empty input");
	}

	/**
	 * Print out main internal vars to ease debugging.
	 */
	public void printDebug(String s) {
		String z="";
		for (int i=0; i<readPointer.length; i++) { z += Integer.toString(readPointer[i])+" "; }
		LogST.logP(3, "BufferFileModule "+s+"\t DEBUG Wp "+writePointer+"\t leastRP "+leastRpointer+"\t readers "+z);
	}
	
	/**
	 * @see it.cnr.isti.thematrix.scripting.sys.MatrixModule#hasMore()
	 * 
	 *      This method is not active, for the buffer we have a separate method which identifies the caller (a
	 *      BufferProxyModule).
	 */
	@Override
	public boolean hasMore() {
		LogST.logP(0, "BufferFileModule.hasMore() erroneously called");
		throw new Error("BufferFileModule.hasMore(void) disallowed");
	}

	/**
	 * 
	 * @see it.cnr.isti.thematrix.scripting.sys.MatrixModule#next()
	 * 
	 *      This method is not active, for the buffer we have a separate method which identifies the caller (a
	 *      BufferProxyModule).
	 */
	@Override
	public void next() {
		LogST.logP(0, "BufferFileModule.next() erroneously called");
		throw new Error("BufferFileModule.next(void) disallowed");
	}

}
