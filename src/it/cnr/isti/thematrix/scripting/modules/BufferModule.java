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

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.DatasetRecord;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;

/**
 * Module implements single-writer, multiple-reader buffering (with stream replication) between TheMatrix modules. The
 * module is inserted whenever appropriate by postprocessing the module graph before script execution.<br>
 * 
 * The readers interface to the buffer via a BufferProxyModule, so they can be identified. Ordinary
 * <code>hasMore()</code> and <code>next()</code> won't work here.<br>
 * 
 * Module name does not start with TheMatrix to underline that this module is NOT available in the programming syntax.
 * Implements a circular buffer wit multiple read-from pointers in an array of record references, uses TheMatrix
 * <code>hasMore(); next()</code> conventions. Does call the input when a reader is out of data, keeps each row in the
 * buffer until all readers are part it. It currently checks, but may deadlock if the buffer space is overrun by one
 * reader.
 * 
 * Invariant: for the slowest reader, we have <br>
 * (reader=writer) pointers means buffer is empty, and reader location is invalid;<br>
 * writer=reader+bufferSizeMax-1 means buffer full; we waste one position this way, but it's safer.<br>
 * 
 * Note that we use the consumers List of MatrixModule in a non-standard way, to keep track of the receivers when
 * calling their setAll(). <br>
 * 
 * Notes: circular reference buffer; shall be constrainted to 2^i bufferSize; shall keep integer pointers; define internal
 * methods to compare, round indexes, update/compute bufferSize; <br>
 * Shall delete a reference only if each reader called next() on it; <br>
 * Shall report if the buffer is overrun.
 * 
 * @author massimo
 * 
 */
public class BufferModule extends MatrixModule {

	/* hashmap of receivers, computed from source module */
	/* number of receivers, to avoid calling bufferSize() all the time */
	private int numReaders;

	/**
	 * Single index for writing in to the buffer; points to a free location in the buffer; grow from 0 to MAXINT, it is
	 * wrapped around at the buffer bufferSize when accessing the data. Invariants:
	 * <code>Wpointer<=Rpointer[i]<=Wpointer+bufferMaxSize </code> for any i.
	 */
	private int Wpointer;

	/**
	 * Array of indexes, of bufferSize numReaders. They grow from 0 to MAXINT, wrapping around at the bufferSize of the
	 * buffer. Each points to the active (valid) location, unless it is equal to Wpointer.
	 */
	private int Rpointer[];

	/* the minimum of the reading pointers, constantly updated after each read operation */
	private int leastRpointer;

	/* array of received data (we MUST make a copy here) */
	private DatasetRecord buffer[];

	/* the bufferSize of the buffer (active part with data) */
	// private int bufferSize; might same some additions... whatever, this is Java.

	/* the bufferSize of the underlying buffer array must be a power of two */
	private int bufferMaxSize;
	/* the bit mask to wrap-around indexes in the array */
	private int bufferMask;

	/* our input */
	private final MatrixModule input;

	/**
	 * Constructor that builds a Buffer Module AND adds it to the module chain, assumes the module chain is already
	 * built by the neverlang parser. Will patch its source module, generate the necessary Proxy modules, link them to
	 * the buffer and to their receivers.
	 * 
	 * @param name
	 *            Name of the BufferModule
	 * @param inputTable
	 *            Name of the input module we use as source (it also provides the receivers list)
	 * @param size
	 *            Size of the local buffer in number of records
	 */
	public BufferModule(String name, String inputTable, int size) {
		super(name);

		// initialize the local buffer and reated variables
		int t = size;
		if (t < 0 || (t & (t - 1)) != 0) {
			LogST.logP(0, "BufferModule() size " + size + " not a power of 2");
			throw new OutOfMemoryError("BufferModule() size " + size + " not a power of 2");
		}

		this.bufferMaxSize = size;
		this.bufferMask = size - 1; // needs bufferSize==2^i
		this.buffer = new DatasetRecord[bufferMaxSize]; // empty DatasetRecords

		// redundant, for clarity
		this.Wpointer = 0;
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

		LogST.logP(1, "BufferModule() "+this.name +" added "); 
		
		// extract readers table from input and process them
		this.numReaders = input.getConsumers().size();
		Rpointer = new int[numReaders]; // all zeroes, and we are fine with that
		int id = 0;
		// generate Proxies and connect them here
		for (MatrixModule m : input.getConsumers()) {
			LogST.logP(1, "BufferModule() "+ this.name +" adding new proxy to " + m.name);

			// generate a proxy from m to here with identifier id
			BufferProxyModule pm = new BufferProxyModule(this.name, id, m.name);
			this.addConsumer(pm); // same order, so we can call consumers.get(id);
			id++;

			// FIXME (domanda Edoardo) shall we add us (YES) and/or the proxies in TheMatrixSys list of modules?

			// get the module dest and patch it; throws if method not overridden by the concrete module
			m.changeInput(pm);
			LogST.logP(1, "BufferModule() ... added " + m.name);
		}

		// patch the source module to only have this buffer in the receivers; link _to_ source is this.input
		input.getConsumers().clear();
		input.addConsumer(this);
	}

	/**
	 * Compute the amount of valid data in the buffer for a specified reader
	 **/
	private int bufferSize(int caller) {
		// (Wpointer - Rpointer[caller]) < bufferMaxSize sempre, quindi & bufferMask qui e' inutile
		return (Wpointer - Rpointer[caller]);
	}

	/**
	 * Compute the number of buffer locations available for the writer.
	 **/
	private int bufferWriterSize() {
		// if this is not < bufferMaxSize-1, we got a problem
		if (!(Wpointer - leastRpointer < bufferMaxSize - 1))
			throw new OutOfMemoryError("BufferModule.bufferWriteSize() buffer overfull");
		return (Wpointer - leastRpointer);
	}

	/**
	 * Check if the writer has space to write in the buffer. Called each time a reader is advancing with respect to the
	 * other ones, hence we need to get and buffer one additional data row.
	 * 
	 * @return true if we have space.
	 */
	private boolean mayWriteInBuffer() {
		return (bufferWriterSize() < bufferMaxSize - 1);
	}

	/**
	 * A method to update the length of the buffer after a read and return it. Will increment the given Rpointer and
	 * update the minimum value leastRpointer. Shall NOT be called if the Rpointer[i] == Wpointer.
	 * 
	 * FIXME now done in an ugly, trivial way, has cost O(numReaders) at each mimimum stream change --> O(numReaders)
	 * worst case.
	 **/
	private void stepRpointer(int reader) {
		// DEBUG: if read buffer empty crash
		if (Rpointer[reader] == Wpointer)
			throw new OutOfMemoryError("BufferModule.stepRpointer() reader buffer is empty");

		// we are increasing Rpointer, we may need to update the minimum pointer
		if (Rpointer[reader] != leastRpointer) {
			(Rpointer[reader])++;
			return;
		} // easy case
		for (int i = 0; i < numReaders; i++) // we may have other pointers at the minimum
		{
			if (i == reader) continue;
			if (Rpointer[i] == leastRpointer) {
				(Rpointer[reader])++;
				return;
			} // we found another minimum
		}
		// since we reach here, that was the only minimum; so we increment both
		Rpointer[reader]++;
		leastRpointer++;
		return;
	}

	@Override
	public void setup() {
		LogST.logP(0, "BufferModule.setup() for " + this.name);
	}

	@Override
	public void exec() {
		LogST.logP(2, "NO-OP BufferModule.exec() for " + this.name);
	}

	@Override
	public void reset() {
		throw new Error("BufferModule.reset(void) disallowed");
	}

	/**
	 * Implements the hasMore() functionality for a given BufferProxyModule which identifies by passing an integer
	 * 
	 * @param caller
	 *            the id of the caller
	 * @return the hasMore value
	 */
	public boolean hasMore(int caller) {
		printDebug("hasMore()");
		// if caller has more in local buffer
		if (bufferSize(caller) > 0) return true;

		// we have nothing ready; if buffer non full (we could read) ask our own input
		if (mayWriteInBuffer()) return input.hasMore();

		// else: no data for reader, and buffer is full; we have a running reader!
		// we should raise error and "gracefully crash" = throw new Error()
		// in the future, either trigger buffering to file, or buffer resize, or thread suspend
		LogST.logP(0, "BufferModule.hasMore() buffer overfull");
		throw new OutOfMemoryError("BufferModule.hasMore() buffer overfull");
	}

	/**
	 * Implements the next() functionality for a given BufferProxyModule, which identifies itself by passing an integer
	 * index.
	 * 
	 * @param caller
	 *            the id of the caller
	 */
	public void next(int caller) {
		printDebug("next()");
		// if caller has no more, but buffer full --> raise error and "gracefully crash"
		if (bufferSize(caller) == 0 && !mayWriteInBuffer()) // gracefully crash
		{
			LogST.logP(0, "BufferModule.next() buffer overfull");
			throw new OutOfMemoryError("BufferModule.next() buffer overfull");
		}

		// if caller has no more in buffer, and buffer not full, read from input
		if (bufferSize(caller) == 0 && mayWriteInBuffer()) { // we shall do a next() and a copy
			input.next(); // what if we get an error here?
			DatasetRecord r = DatasetRecord.emptyRecord(input);
			r.setAll(input);
			buffer[Wpointer & bufferMask] = r; //input.deepCopy();
			Wpointer++; // this increases bufferSize(caller), leads to next if()
		}

		// if caller has more in local buffer, we can copy local to caller
		if (bufferSize(caller) > 0) {
			LogST.logP(0, "BufferModule test data" + buffer[Rpointer[caller] & bufferMask].toString());
			LogST.logP(0, "BufferModule dest.before data" + this.getConsumers().get(caller).toString());
			this.getConsumers().get(caller).setAll(buffer[Rpointer[caller] & bufferMask]); // push the data
			LogST.logP(0, "BufferModule dest.after data" + this.getConsumers().get(caller).toString());
			stepRpointer(caller); // step the counter; buffer may become virtually empty here
		}

	}

	/**
	 * Print out main internal vars to ease debugging.
	 * @param s 
	 */
	public void printDebug(String s) {
		String z="";
		for (int i=0; i<Rpointer.length; i++) { z += Integer.toString(Rpointer[i])+" "; }
		LogST.logP(0, "BufferModule "+s+"\t DEBUG Wp "+Wpointer+"\t leastRP "+leastRpointer+"\t readers "+z);
//		LogST.logP(0, "BufferModule DEBUG ");
//		LogST.logP(0, "BufferModule DEBUG ");
//		LogST.logP(0, "BufferModule DEBUG ");
	}
	
	/**
	 * @see it.cnr.isti.thematrix.scripting.sys.MatrixModule#hasMore()
	 * 
	 *      This method is not active, for the buffer we have a separate method which identifies the caller (a
	 *      BufferProxyModule).
	 */
	@Override
	public boolean hasMore() {
		LogST.logP(0, "BufferModule.hasMore() erroneously called");
		// return false;
		// TODO
		throw new Error("BufferModule.hasMore(void) disallowed");
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
		LogST.logP(0, "BufferModule.next() erroneously called");
		// TODO
		throw new Error("BufferModule.next(void) disallowed");
	}

}
