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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Class for sorting (large) comma separated value text files. 
 * @author schuemie
 *
 */
public class NativeStringFileSorter {

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
  public static double minFreeMemFraction = 0.5;
  
  public static void sort(String filename, int columnIndex) {
    sort(filename, null, columnIndex, false);
  }

  public static void sort(String filename, String columnname) {
    sort(filename, new String[]{columnname}, -1, false);
  }

  public static void sort(String filename, String[] columnnames){
    sort(filename, columnnames, -1, false);
  }
  
  /**
   * If semiSortFirstVariable is set to true, the file will be sorted so all rows that have the same
   * value for the first column (in the given list of columnnames) are grouped together, but the order
   * of values is random.
   * @param filename
   * @param columnnames
   * @param semiSortFirstVariable
   */
  public static void sort(String filename, String[] columnnames, boolean semiSortFirstVariable){
    sort(filename, columnnames, -1, semiSortFirstVariable);
  }

  private static void sort(String filename, String[] columnnames, int columnIndex, boolean semiSortFirstVariable){
  	System.gc();
  	long availableMem = getFreeMem();//Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
  	long minFreeMem = Math.round(availableMem * minFreeMemFraction);
  	boolean hasHeader = (columnnames != null);

  	if (verbose) {
  		System.out.println("Starting sort");
  		if (chunckSize == -1)
  			System.out.println("Memory available for sorting: " + availableMem + " bytes. Min free = " + minFreeMem);
  	}

  	ReadCSVFile in = new ReadCSVFile(filename);
  	Iterator<List<String>> iterator = in.iterator();

  	List<String> header = null;
  	if (hasHeader)
  		header = iterator.next();

  	Comparator<List<String>> comparator = buildComparator(columnnames, columnIndex, header, semiSortFirstVariable);

  	int nrOfFiles = 0;
  	int sorted = 0;
  	while (iterator.hasNext()) {
  		ProgressHandler.reportProgress();
  		List<List<String>> tempRows; 
  		if (chunckSize == -1)
  			tempRows = readUntilMemFull(iterator, minFreeMem);
  		else 
  			tempRows = readRows(iterator, chunckSize);

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
  	mergeByBatches(nrOfFiles, filename, comparator, hasHeader);
  }

  private static void mergeByBatches(int nrOfFiles, String source, Comparator<List<String>> comparator, boolean hasHeader) {
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
        mergeFiles(sourceBase, from, to, targetFilename, comparator, hasHeader);
        deleteTempFiles(sourceBase, from, to);
        from = to;
      }
      nrOfFiles = newNrOfFiles;
      level++;
      sourceBase = targetBase;
    } while (nrOfFiles > 1);
  }

  private static void writeToDisk(List<String> header, List<List<String>> rows, String filename) {
    WriteCSVFile out = new WriteCSVFile(filename);
    if (header != null)
      out.write(header);
    for (List<String> row : rows)
      out.write(row);
    out.close();
  }

  private static List<List<String>> readRows(Iterator<List<String>> iterator, int nrOfRows) {
    List<List<String>> rows = new ArrayList<List<String>>();
    int i = 0;
    while (i++ < nrOfRows && iterator.hasNext())
      rows.add(iterator.next());
    return rows;
  }

  private static List<List<String>> readUntilMemFull(Iterator<List<String>> iterator, long minFreeMem) {
    List<List<String>> rows = new ArrayList<List<String>>();
    System.gc();
    long freeMem = Long.MAX_VALUE;
    while (freeMem > minFreeMem && iterator.hasNext()) {
      rows.add(iterator.next());
      freeMem = getFreeMem();     
    }
    return rows;
  }
  
  private static long getFreeMem(){
    return Runtime.getRuntime().freeMemory() + Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
  }

  private static Comparator<List<String>> buildComparator(String[] columnnames, int columnIndex, List<String> header, boolean semiSortFirstVariable) {
    int[] indexesToCompare = new int[]{columnIndex};
    if (columnnames != null){
      indexesToCompare = new int[columnnames.length];
      for (int i = 0; i < columnnames.length; i++)
        indexesToCompare[i] = getIndexForColumn(header, columnnames[i]);
    }
    if (semiSortFirstVariable){
    	if (verbose)
    		System.out.println("Semisorting on column: " + columnnames[0]);
    	return new RowComparatorSemiSort(indexesToCompare);
    } else
      return new RowComparator(indexesToCompare);
  }


  private static class RowComparator implements Comparator<List<String>> {
    private int[] indexes;
    public RowComparator(int[] indexes){
      this.indexes = indexes;
    }
    public int compare(List<String> o1, List<String> o2) {
      int result = 0;
      int i = 0;
      while (result == 0 && i < indexes.length){
        result = o1.get(indexes[i]).compareTo(o2.get(indexes[i]));
        i++;
      }
      return result;
    }
  }
  
  private static class RowComparatorSemiSort implements Comparator<List<String>> {
    private int[] indexes;
    private Random random = new Random();
    private Map<String, Integer> value2Random = new HashMap<String, Integer>();
    public RowComparatorSemiSort(int[] indexes){
      this.indexes = indexes;
    }
    public int compare(List<String> o1, List<String> o2) {
      //Compare first column semisorted:
      int rnd1 = mapToRandom(o1.get(indexes[0]));
      int rnd2 = mapToRandom(o2.get(indexes[0]));
      if (rnd1 > rnd2)
      	return 1;
      if (rnd1 < rnd2)
      	return -1;
      int result = 0;
      int i = 1;
      while (result == 0 && i < indexes.length){
        result = o1.get(indexes[i]).compareTo(o2.get(indexes[i]));
        i++;
      }
      return result;
    }
    
    private Integer mapToRandom(String value){
    	Integer randomValue = value2Random.get(value);
    	if (randomValue == null){
    		randomValue = random.nextInt();
    		value2Random.put(value, randomValue);
    	}
    	return randomValue;
    }
  }

  private static void deleteTempFiles(String base, int start, int end) {
    for (int i = start; i< end; i++)
      (new File(generateFilename(base, i))).delete();
  }
  
  private static String generateFilename(String base, int number){
    return base + "_" + number + ".tmp";
  }

  private static void mergeFiles(String sourceBase, int start, int end, String target, Comparator<List<String>> comparator, boolean hasHeader) {
    List<Iterator<List<String>>> tempFiles = new ArrayList<Iterator<List<String>>>();

    List<List<String>> filerows = new ArrayList<List<String>>();
    List<String> header = null;
    boolean done = true;  
    for (int i = start; i< end; i++){
      ReadCSVFile tempFile = new ReadCSVFile(generateFilename(sourceBase, i));
      Iterator<List<String>> iterator = tempFile.iterator();//.getIterator();
      if (hasHeader && iterator.hasNext()){
        if (tempFiles.size() == 0) //its the first one
          header = iterator.next();
        else
          iterator.next();
      }
      tempFiles.add(iterator);

      //initialize
      if (iterator.hasNext()){
        filerows.add(iterator.next());
        done = false;
      } else
        filerows.add(null);
    }
    WriteCSVFile out = new WriteCSVFile(target);
    if (hasHeader)
      out.write(header);
    while (!done){
      ProgressHandler.reportProgress();
      //Find best file to pick from:
      List<String> bestRow = null;
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
        out.write(bestRow);

        //get next from winning file:
        List<String> newRow;
        Iterator<List<String>> bestFileIterator = tempFiles.get(bestFile); 
        if (bestFileIterator.hasNext())
          newRow = bestFileIterator.next();
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
      result = StringUtilities.caseInsensitiveIndexOf(value, list);
    if (result == -1)
      throw (new RuntimeException("File sorter could not find column \"" + value + "\""));
    return result;
  }

}