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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.erasmusmc.jerboa.ProgressHandler;

/**
 * Class for sorting (large) comma separated value text files. 
 * @author schuemie
 *
 */
public class ASCIIStringFileSorter {

  /**
   *  Chunksize is the number of rows sorted in memory in one time. Higher chunk size leads to
   *  better performance but requires more memory. 
   *  Set chunksize to -1 to let the system determine the largest chunk size based on available
   *  memory. 
   */
  public static int chunckSize = -1;
  public static boolean verbose = false;
  public static boolean caseSensitiveColumnNames = false;
  public static int maxNumberOfTempFiles = 1000;
  public static double minFreeMemFraction = 0.25;
  
  public static void sort(String filename, int columnIndex) {
    sort(filename, null, columnIndex);
  }

  public static void sort(String filename, String columnname) {
    sort(filename, new String[]{columnname}, -1);
  }

  public static void sort(String filename, String[] columnnames){
    sort(filename, columnnames, -1);
  }
  
  private static void sort(String filename, String[] columnnames, int columnIndex){
  	System.gc();
  	long availableMem = getFreeMem();//Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
  	long minFreeMem = Math.round(availableMem * minFreeMemFraction);
  	boolean hasHeader = (columnnames != null);

  	if (verbose) {
  		System.out.println("Starting sort");
  		if (chunckSize == -1)
  			System.out.println("Memory available for sorting: " + availableMem + " bytes. Min free = " + minFreeMem);
  	}

  	ReadTextFile in = new ReadTextFile(filename);
  	Iterator<String> iterator = in.iterator();

  	String header = null;
  	if (hasHeader)
  		header = iterator.next();

  	Comparator<Row> comparator = new RowComparator();
  	int[] indexesToCompare = getIndexes(columnIndex, columnnames, header);

  	int nrOfFiles = 0;
  	int sorted = 0;
  	while (iterator.hasNext()) {
  		ProgressHandler.reportProgress();
  		List<Row> tempRows; 
  		if (chunckSize == -1)
  			tempRows = readUntilMemFull(iterator, indexesToCompare, minFreeMem);
  		else 
  			tempRows = readRows(iterator, indexesToCompare, chunckSize);

  		// sort the rows
  		ProgressHandler.reportProgress();
  		Collections.sort(tempRows,comparator);

  		String tempFilename = generateFilename(filename, nrOfFiles++);

  		// write temp file to disk
  		ProgressHandler.reportProgress();
  		writeToDisk(header, tempRows, tempFilename);

  		sorted += tempRows.size();
  		if (verbose) 
  			System.out.println("-Sorted " + sorted + " lines");
  	}
  	mergeByBatches(nrOfFiles, filename, comparator, hasHeader, indexesToCompare);
  }

  private static int[] getIndexes(int columnIndex, String[] columnnames, String header) {
  	int[] indexesToCompare = new int[]{columnIndex};
  	List<String> headerList = line2columns(header);
    if (columnnames != null){
      indexesToCompare = new int[columnnames.length];
      for (int i = 0; i < columnnames.length; i++)
        indexesToCompare[i] = getIndexForColumn(headerList, columnnames[i]);
    }
    return indexesToCompare;
	}

	private static void mergeByBatches(int nrOfFiles, String source, Comparator<Row> comparator, boolean hasHeader, int[] indexesToCompare) {
    int level = 0;
    String sourceBase = source;
    String targetBase;
    do{
      if (verbose)
        System.out.println("Merging " + nrOfFiles + " files");
      
      int newNrOfFiles = 0;
      int from = 0;
      targetBase = source + "_" + level;
      while (from < nrOfFiles){
        String targetFilename;      
        if (nrOfFiles <= maxNumberOfTempFiles)
          targetFilename = source;
        else
          targetFilename = generateFilename(targetBase, newNrOfFiles++);
          
        int to = Math.min(from + maxNumberOfTempFiles, nrOfFiles);
        mergeFiles(sourceBase, from, to, targetFilename, comparator, hasHeader, indexesToCompare);
        deleteTempFiles(sourceBase, from, to);
        from = to;
      }
      nrOfFiles = newNrOfFiles;
      level++;
      sourceBase = targetBase;
    } while (nrOfFiles > 1);
  }

  private static void writeToDisk(String header, List<Row> rows, String filename) {
    WriteTextFile out = new WriteTextFile(filename);
    if (header != null)
      out.writeln(header);
    for (Row row : rows)
      out.writeln(row.toString());
    out.close();
  }

  private static List<Row> readRows(Iterator<String> iterator, int[] indexesToCompare, int nrOfRows) {
  	List<Row> rows = new ArrayList<Row>();
    int i = 0;
    while (i++ < nrOfRows && iterator.hasNext())
    	rows.add(new Row(iterator.next(), indexesToCompare));
    return rows;
  }

  private static List<Row> readUntilMemFull(Iterator<String> iterator, int[] indexesToCompare, long minFreeMem) {
    List<Row> rows = new ArrayList<Row>();
    System.gc();
    long freeMem = Long.MAX_VALUE;
    while (freeMem > minFreeMem && iterator.hasNext()) {
      rows.add(new Row(iterator.next(), indexesToCompare));
      freeMem = getFreeMem();     
    }
    return rows;
  }
  
  private static long getFreeMem(){
    return Runtime.getRuntime().freeMemory() + Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
  }

  private static class RowComparator implements Comparator<Row> {
 
  	public int compare(Row o1, Row o2) {
  		return o1.key.compareTo(o2.key);
    }
  }
  
  private static void deleteTempFiles(String base, int start, int end) {
    for (int i = start; i< end; i++)
      (new File(generateFilename(base, i))).delete();
  }
  
  private static String generateFilename(String base, int number){
    return base + "_" + number + ".tmp";
  }

  private static void mergeFiles(String sourceBase, int start, int end, String target, Comparator<Row> comparator, boolean hasHeader, int[] indexesToCompare) {
    List<Iterator<String>> tempFiles = new ArrayList<Iterator<String>>();

    List<Row> filerows = new ArrayList<Row>();
    String header = null;
    boolean done = true;  
    for (int i = start; i< end; i++){
      ReadTextFile tempFile = new ReadTextFile(generateFilename(sourceBase, i));
      Iterator<String> iterator = tempFile.getIterator();
      if (hasHeader && iterator.hasNext()){
        if (tempFiles.size() == 0) //its the first one
          header = iterator.next();
        else
          iterator.next();
      }
      tempFiles.add(iterator);

      //initialize
      if (iterator.hasNext()){
        filerows.add(new Row(iterator.next(),indexesToCompare));
        done = false;
      } else
        filerows.add(null);
    }
    WriteTextFile out = new WriteTextFile(target);
    if (hasHeader)
      out.writeln(header);
    while (!done){
      ProgressHandler.reportProgress();
      //Find best file to pick from:
      Row bestRow = null;
      int bestFile = -1;
      for (int i = 0; i < filerows.size(); i++){
        if (bestRow == null || (filerows.get(i) != null && comparator.compare(filerows.get(i), bestRow) < 0)){
          bestRow = filerows.get(i);
          bestFile = i;
        }
      }
      if (bestRow == null)
        done = true;
      else {
        //write it to file:
        out.writeln(bestRow.toString());

        //get next from winning file:
        Row newRow;
        Iterator<String> bestFileIterator = tempFiles.get(bestFile); 
        if (bestFileIterator.hasNext())
          newRow = new Row(bestFileIterator.next(),indexesToCompare);
        else
          newRow = null;
        filerows.set(bestFile, newRow);
      }
    }
    out.close();
  }

  private static int getIndexForColumn(List<String> list, String value) throws RuntimeException {
    int result;
    if (caseSensitiveColumnNames)
      result = list.indexOf(value);
    else
      result = caseInsensitiveIndexOf(value, list);
    if (result == -1)
      throw (new RuntimeException("File sorter could not find column \"" + value + "\""));
    return result;
  }
  
  public static int caseInsensitiveIndexOf(String value, List<String> list){
  	String queryLC = value.toLowerCase();
    for (int i = 0; i < list.size(); i++){
    	String string = list.get(i);
      if (string.toLowerCase().equals(queryLC))
        return i;
    }
    return -1;
  }
  
  private static class Row extends ASCIIString{
  	public ASCIIString key;
  	
  	public Row(String string, int[] indexesToCompare) {
			super(string);
			List<String> columns = line2columns(string);
			StringBuilder sb = new StringBuilder();
			for (int index : indexesToCompare){
			  sb.append(columns.get(index));
			  sb.append('\t');
			}
			key = new ASCIIString(sb.toString());
		}
  }
  
  private static List<String> line2columns(String line){
    List<String> columns = StringUtilities.safeSplit(line, ',');
    for (int i = 0; i < columns.size(); i++){
      String column = columns.get(i);
      if (column.startsWith("\"") && column.endsWith("\"") && column.length() > 1)
        column = column.substring(1, column.length()-1);
      column = column.replace("\\\"", "\"");
      columns.set(i, column);
    }
    return columns;
  } 
}