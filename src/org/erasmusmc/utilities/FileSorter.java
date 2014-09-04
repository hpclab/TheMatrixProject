/*
 * Copyright (c) Erasmus MC
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.erasmusmc.utilities;

import it.cnr.isti.thematrix.configuration.LogST;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

// removed reporting via progress handler --  this makes the code incompatible with Jerboa GUI
//import org.erasmusmc.jerboa.ProgressHandler;

/**
 * Class for sorting (large) comma separated value text files.
 * 
 * FIXME ISSUE: it uses Read / WriteCSVASCIIFile objects, which perform full CSV
 * quoting, while do not hold values subject to quoting (related to memory
 * fragmentation in *CSVASCIIFile stringBuilders).
 * 
 * @author schuemie
 * 
 */
public class FileSorter {
	public static boolean verbose = true; // false; we go through our logging class anyway
	public static boolean caseSensitiveColumnNames = false;

	/**
	 * THIS VARIABLE STATES THE MAX NUMBER OF FILES TO MERGE AT THE SAME TIME, NOT THE MAX NUMBER OF FILES GENERATED. And does not need to be public.
	 * 
	 * REFACTORED
	 */
	static final int maxMergedTempFiles = 8; // 1000; so many open files and buffer can crash the machine

	/**
	 * The number of tempFiles that we create in the worst case, before giving up. More than e.g. 1000 temporary file
	 * and we give up (no sorting stuff over 1TB, see <code>maxMemChunk</code> , or over 1000 times the available memory in
	 * the heap).
	 */
	static final int maxCreatedTempFiles = 1000; //  
		
	/**
	 * Chunksize is the number of rows sorted in memory in one time. Higher chunk size leads to better performance but
	 * requires more memory. Set chunksize to -1 to let the system determine the largest chunk size based on available
	 * memory.
	 */
	public static int chunckSize = -1; // this is in _rows_
	public static double minFreeMemFraction = 0.25; // used to be 0.25; possibly not safe
	public static long maxMemChunk = 1 << 30; // avoid temporary files larger than 1 GB
	public static int checkMemInterval = 100000; // check the free memory every this many rows; 100krow -> 1MB to 10MB
	
	public static boolean checkIfAlreadySorted = false; // true; default changed as it is actually almost never true and
														// heavy to check for big files
	public static int maxCheckRows = 10000000; // Check only first 10000000 rows to see if already sorted

	public static void main(String[] args) {
		verbose = true;
		sort("/home/data/Simulated/Prescriptions.txt", "Duration");
	}

	public static void sort(String filename, int columnIndex) {
		sort(filename, null, columnIndex, false);
	}

	public static void sort(String filename, String columnname) {
		sort(filename, new String[]{columnname}, -1, false);
	}

	public static void sort(String filename, String[] columnnames) {
		sort(filename, columnnames, -1, false);
	}

	/**
	 * If semiSortFirstVariable is set to true, the file will be sorted so all rows that have the same value for the
	 * first column (in the given list of columnnames) are grouped together, but the order of values is random.
	 * 
	 * @param filename
	 * @param columnnames
	 * @param semiSortFirstVariable
	 */
	public static void sort(String filename, String[] columnnames, boolean semiSortFirstVariable) {
		sort(filename, columnnames, -1, semiSortFirstVariable);
	}

	private static void sort(String filename, String[] columnnames, int columnIndex, boolean semiSortFirstVariable) {
		/* self protection */
		boolean hasReadZeroLines = false;
		int previousLogLevel = LogST.getLogLevel();
		if (previousLogLevel<2) LogST.enable(2);
		/**/
		LogST.logP(0, "FileSorter.sort(..) on file " + filename + " is about to call GC.");
		System.gc();
		LogST.logP(0, "FileSorter.sort(..): GC just finished.");

		long availableMem = getFreeMem();// Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() +
											// Runtime.getRuntime().freeMemory();
		long minFreeMem = Math.round(availableMem * minFreeMemFraction);
		// FIXME we should have 1 here not 2, and safer code in the reading routine
		// we assume that rows are 256 byte long, and add a 2* as a safety measure
		if (availableMem * minFreeMemFraction > 2 * maxMemChunk)
			chunckSize = (int) Math.min(maxMemChunk >> 8, Integer.MAX_VALUE);

		LogST.logP(0,
				"FileSorter.sort() memory parameters avail " + String.valueOf(availableMem >> 20) + " MB minFree "
						+ String.valueOf(minFreeMem >> 20) + " MB maxMemChunck " + String.valueOf(maxMemChunk >> 20)
						+ " MB chunckSize in rows " + chunckSize);

		/** assertions **/
		if (chunckSize != -1 && chunckSize < 10000) {
			LogST.logP(-1, "FileSorter chunkSize is " + chunckSize + " stopping computation");
			throw new RuntimeException("FileSorter internal error, chunkSize " + chunckSize);
		}
		if (availableMem < 1 << 24) {
			LogST.logP(-1, "FileSorter available memory is " + availableMem + " stopping computation");
			throw new OutOfMemoryError("FileSorter internal error, less than 16MB available memory: " + availableMem);
		}
		/** **/
		boolean hasHeader = (columnnames != null);

		if (verbose) {
			LogST.logP(0, "FileSorter.sort(..): starting sort");
			if (chunckSize == -1)
				LogST.logP(0,
						"FileSorter.sort(..): Memory available for sorting: " + String.valueOf(availableMem >> 20)
								+ " MB. Min free = " + String.valueOf(minFreeMem >> 20) + " MB");
		}

		Iterator<List<ASCIIString>> iterator = new ReadCSVASCIIFile(filename).iterator();

		List<ASCIIString> header = null;
		if (hasHeader) if (iterator.hasNext())
			header = iterator.next();
		else
			throw new RuntimeException("Header row expected but not found in file: " + filename);

		Comparator<List<ASCIIString>> comparator = buildComparator(columnnames, columnIndex, header,
				semiSortFirstVariable);

		if (checkIfAlreadySorted) {
			if (isSorted(iterator, comparator))
				return;
			else {
				iterator = new ReadCSVASCIIFile(filename).iterator();
				iterator.next(); // skip header
			}
		}

		int nrOfFiles = 0;
		int sorted = 0;
		while (iterator.hasNext()) {
			// ProgressHandler.reportProgress();
			LogST.logP(2, "FileSorter.sort() inside local sorting, at beginning");
			List<List<ASCIIString>> tempRows;
			if (chunckSize == -1)
				tempRows = readUntilMemFull(iterator, minFreeMem);
			else
				tempRows = readRows(iterator, chunckSize);

			/** self-check and assertions **/
			if (hasReadZeroLines && tempRows.size() == 0) {
				LogST.logP(-1, "FileSorter read 0 lines twice, stopping computation");
				throw new RuntimeException("FileSorter internal error - empty file about to be generated ");
			}
			if (tempRows.size() == 0) hasReadZeroLines = true;
			/** **/

			// sort the rows
			// ProgressHandler.reportProgress();
			/**
			 * FIXME this code should not actually be executed if the file is going to be empty!!
			 */

			LogST.logP(2, "FileSorter.sort() local sorting, after readUntilMemFull / readRows");

			Collections.sort(tempRows, comparator);

			LogST.logP(2, "FileSorter.sort() local sorting, after Collections.sort()");
			
			String tempFilename = generateFilename(filename, nrOfFiles++);

			LogST.logP(2, "FileSorter.sort() local sorting, generated filename "+tempFilename);

			// write temp file to disk
			// ProgressHandler.reportProgress();
			LogST.logP(2, "FileSorter.sort() local sorting, about to write "+tempRows.size()+" rows to tempfile "+tempFilename);
			writeToDisk(header, tempRows, tempFilename);

			sorted += tempRows.size();
			if (verbose) LogST.logP(0, "FileSorter.sort(..): -Sorted " + sorted + " lines");

			if (nrOfFiles > maxCreatedTempFiles) {
				LogST.logP(-1, "FileSorter created too many files, "+ nrOfFiles+ " stopping computation");
				throw new RuntimeException("FileSorter internal error - empty file about to be generated ");
			}

		}
		mergeByBatches(nrOfFiles, filename, comparator, hasHeader);
		
		LogST.enable(previousLogLevel); //DEBUG restore previous level
	}

	private static boolean isSorted(Iterator<List<ASCIIString>> iterator, Comparator<List<ASCIIString>> comparator) {
		if (verbose) LogST.logP(0, "FileSorter.isSorted(..): Checking whether already sorted");
		List<ASCIIString> previous = null;
		if (iterator.hasNext()) {
			previous = iterator.next();
		}
		int i = 0;
		while (iterator.hasNext()) {
			List<ASCIIString> row = iterator.next();
			if (comparator.compare(previous, row) > 0) {
				if (verbose) LogST.logP(0, "FileSorter.isSorted(..): File not yet sorted");
				return false;
			}
			previous = row;
			i++;
			if (i > maxCheckRows) break;
		}
		if (verbose) LogST.logP(0, "FileSorter.isSorted(..): File already sorted");
		return true;
	}

	/**
	 * Merge together a number of temporary files, with common base name, specified comparator.
	 * NOW obeys the maxMergedTempFiles value, does not open more than that many files.
	 *  
	 * @param nrOfFiles
	 * @param source
	 * @param comparator
	 * @param hasHeader
	 */
	private static void mergeByBatches(int nrOfFiles, String source, Comparator<List<ASCIIString>> comparator,
			boolean hasHeader) {
		int level = 0;
		String sourceBase = source;
		String targetBase;
		do {
			if (verbose) LogST.logP(0, "FileSorter.mergeByBatches(..) outer cycle merging " + nrOfFiles + " files");

			int newNrOfFiles = 0;
			int from = 0;
			targetBase = source + "_" + level;
			while (from < nrOfFiles) {
				String targetFilename;
				if (nrOfFiles <= maxMergedTempFiles)
					targetFilename = source;
				else
					targetFilename = generateFilename(targetBase, newNrOfFiles++);

				int to = Math.min(from + maxMergedTempFiles, nrOfFiles);

				LogST.logP(2, "FileSorter.mergeByBatches(..) inner cycle calling mergeFiles");
				mergeFiles(sourceBase, from, to, targetFilename, comparator, hasHeader);

				LogST.logP(2, "FileSorter.mergeByBatches(..) inner cycle calling deleteTempFiles("+sourceBase+", "+from+", "+to+")");
				deleteTempFiles(sourceBase, from, to);
				from = to;
			}
			nrOfFiles = newNrOfFiles;
			level++;
			sourceBase = targetBase;
			LogST.logP(0, "FileSorter.mergeByBatches outer cycle end");
		} while (nrOfFiles > 1);
	}

	private static void writeToDisk(List<ASCIIString> header, List<List<ASCIIString>> rows, String filename) {
		WriteCSVASCIIFile out = new WriteCSVASCIIFile(filename);
		if (header != null) out.write(header);
		for (List<ASCIIString> row : rows)
			out.write(row);
		out.close();
	}

	private static List<List<ASCIIString>> readRows(Iterator<List<ASCIIString>> iterator, int nrOfRows) {
		List<List<ASCIIString>> rows = new ArrayList<List<ASCIIString>>();
		int i = 0;
		while (i++ < nrOfRows && iterator.hasNext())
			rows.add(iterator.next());
		return rows;
	}

	/**
	 * Read in lines to be sorted in-memory from the source, until the memory is close enough to being full.
	 * 
	 * @param iterator
	 * @param minFreeMem
	 * @return
	 */
	private static List<List<ASCIIString>> readUntilMemFull(Iterator<List<ASCIIString>> iterator, long minFreeMem) {
		List<List<ASCIIString>> rows = new ArrayList<List<ASCIIString>>();

		int oldLevel = LogST.getLogLevel(); //TESTING
		LogST.enable(2);
		
		LogST.logP(0, "FileSorter.readUntilMemFull(..): about to call GC.");
		System.gc();
		LogST.logP(0, "FileSorter.readUntilMemFull(..): GC just finished.");

		long freeMem = getFreeMem();
		long lastFreeMem = freeMem;
		int readRows =0; //how many rows we have read since last free mem check
		
		while (freeMem > minFreeMem && iterator.hasNext()) {
			rows.add(iterator.next());
			readRows++;
			/**
			 * FIXME THIS IS A PERFORMANCE BUG
			 * 
			 * should run straight and call getFreeMem once every 1k, 10K or maybe 100K rows
			 */
			if( readRows >= checkMemInterval) {
				lastFreeMem = freeMem;
				freeMem = getFreeMem();
				LogST.logP(2, "FileSorter.readUntilMemFull(..): redRows "+readRows+" freeMem "+freeMem);
				// if the estimate of the needed space is larger than the boundary, exit loop
				// we should check that we don't have freed too much memory... unlikely anyway
				long lostMemory = lastFreeMem-freeMem;
				double avgRowLen = ((double) lostMemory) / readRows;	
				double nextEsteem = avgRowLen * checkMemInterval;
				LogST.logP(2,"inside check: nextEsteem "+ ((long) nextEsteem)+" freeMem "+freeMem+" minFreeMem "+minFreeMem);
				if (nextEsteem > freeMem-minFreeMem) {
					LogST.logP(0,"closing this run");
					break;
				}
				else
					readRows=0;
			}

		}
		LogST.logP(0, "FileSorter.readUntilMemFull(..): has read "+rows.size()+" rows");
		LogST.logP(0, "FileSorter.readUntilMemFull(..): exiting with free memory "+getFreeMem());

		LogST.enable(oldLevel); // TESTING

		return rows;
	}

	private static long getFreeMem() {
		return Runtime.getRuntime().freeMemory() + Runtime.getRuntime().maxMemory()
				- Runtime.getRuntime().totalMemory();
	}

	private static Comparator<List<ASCIIString>> buildComparator(String[] columnnames, int columnIndex,
			List<ASCIIString> header, boolean semiSortFirstVariable) {
		int[] indexesToCompare = new int[]{columnIndex};
		if (columnnames != null) {
			indexesToCompare = new int[columnnames.length];
			for (int i = 0; i < columnnames.length; i++)
				indexesToCompare[i] = getIndexForColumn(header, new ASCIIString(columnnames[i]));
		}
		if (semiSortFirstVariable) {
			if (verbose) LogST.logP(0, "FileSorter.buildComparator(..): Semisorting on column: " + columnnames[0]);
			return new RowComparatorSemiSort(indexesToCompare);
		}
		else
			return new RowComparator(indexesToCompare);
	}

	private static class RowComparator implements Comparator<List<ASCIIString>> {
		private int[] indexes;
		public RowComparator(int[] indexes) {
			this.indexes = indexes;
		}
		public int compare(List<ASCIIString> o1, List<ASCIIString> o2) {
			int result = 0;
			int i = 0;
			while (result == 0 && i < indexes.length) {
				result = o1.get(indexes[i]).compareTo(o2.get(indexes[i]));
				i++;
			}
			return result;
		}
	}

	private static class RowComparatorSemiSort implements Comparator<List<ASCIIString>> {
		private int[] indexes;
		private Random random = new Random();
		private Map<ASCIIString, Integer> value2Random = new HashMap<ASCIIString, Integer>();
		public RowComparatorSemiSort(int[] indexes) {
			this.indexes = indexes;
		}
		public int compare(List<ASCIIString> o1, List<ASCIIString> o2) {
			// Compare first column semisorted:
			int rnd1 = mapToRandom(o1.get(indexes[0]));
			int rnd2 = mapToRandom(o2.get(indexes[0]));
			if (rnd1 > rnd2) return 1;
			if (rnd1 < rnd2) return -1;
			int result = 0;
			int i = 1;
			while (result == 0 && i < indexes.length) {
				result = o1.get(indexes[i]).compareTo(o2.get(indexes[i]));
				i++;
			}
			return result;
		}

		private Integer mapToRandom(ASCIIString value) {
			Integer randomValue = value2Random.get(value);
			if (randomValue == null) {
				randomValue = random.nextInt();
				value2Random.put(value, randomValue);
			}
			return randomValue;
		}
	}

	private static void deleteTempFiles(String base, int start, int end) {
		for (int i = start; i < end; i++)
			(new File(generateFilename(base, i))).delete();
	}

	private static String generateFilename(String base, int number) {
		return base + "_" + number + ".tmp";
	}

	private static void mergeFiles(String sourceBase, int start, int end, String target,
			Comparator<List<ASCIIString>> comparator, boolean hasHeader) {
		List<Iterator<List<ASCIIString>>> tempFiles = new ArrayList<Iterator<List<ASCIIString>>>();

		List<List<ASCIIString>> filerows = new ArrayList<List<ASCIIString>>();

		List<ASCIIString> header = null;
		boolean done = true;

		LogST.logP(0, "FileSorter.mergeFiles() merging runs "+start+" to "+String.valueOf(end-1)+" into file "+target);
		
		for (int i = start; i < end; i++) {
			ReadCSVASCIIFile tempFile = new ReadCSVASCIIFile(generateFilename(sourceBase, i));
			Iterator<List<ASCIIString>> iterator = tempFile.getIterator();
			if (hasHeader && iterator.hasNext()) {
				if (tempFiles.size() == 0) // its the first one
					header = iterator.next();
				else
					iterator.next();
			}
			tempFiles.add(iterator);

			// initialize
			if (iterator.hasNext()) {
				filerows.add(iterator.next());
				done = false;
			}
			else
				filerows.add(null);
		}
		WriteCSVASCIIFile out = new WriteCSVASCIIFile(target);

		LogST.logP(0, "FileSorter.mergeFiles() merging run starts");

		
		if (hasHeader) out.write(header);
		while (!done) {
			// ProgressHandler.reportProgress();

			// Find best file to pick from:
			List<ASCIIString> bestRow = null;
			int bestFile = -1;
			for (int i = 0; i < filerows.size(); i++) {
				if (bestRow == null || (filerows.get(i) != null && comparator.compare(filerows.get(i), bestRow) < 0)) {
					bestRow = filerows.get(i);
					bestFile = i;
				}
			}
			if (bestRow == null)
				done = true;
			else {
				// write it to file:
				out.write(bestRow);

				// get next from winning file:
				List<ASCIIString> newRow;
				Iterator<List<ASCIIString>> bestFileIterator = tempFiles.get(bestFile);
				if (bestFileIterator.hasNext())
					newRow = bestFileIterator.next();
				else
					newRow = null;
				filerows.set(bestFile, newRow);
			}
		}
		out.close();
		LogST.logP(2, "FileSorter.mergeFiles() runs merged, file closed");

	}

	private static int getIndexForColumn(List<ASCIIString> list, ASCIIString value) throws RuntimeException {
		int result;

		if (caseSensitiveColumnNames)
			result = list.indexOf(value);
		else
			result = caseInsensitiveIndexOf(value, list);

		if (result == -1) throw (new RuntimeException("File sorter could not find column \"" + value + "\""));

		return result;
	}

	public static int caseInsensitiveIndexOf(ASCIIString value, List<ASCIIString> list) {
		ASCIIString queryLC = value.toLowerCase();

		for (int i = 0; i < list.size(); i++) {
			ASCIIString string = list.get(i);
			if (string.toLowerCase().equals(queryLC)) return i;
		}

		return -1;
	}
}