package org.erasmusmc.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Class for shuffling (i.e. put in random order) comma separated value text files. 
 * @author schuemie
 *
 */
public class FileShuffler {

  /**
   *  Chunksize is the number of rows shuffled in memory in one time. Higher chunk size leads to
   *  better performance but requires more memory. 
   *  Set chunksize to -1 to let the system determine the largest chunk size based on available
   *  memory. 
   */
  public static int chunckSize = -1;
  public static boolean verbose = false;
  public static int maxNumberOfTempFiles = 1000;
  public static double minFreeMemFraction = 0.5;
  
  private static Random random = new Random();
  
  public static void main(String[] args){
  	verbose = true;
  	shuffle("x:/Study5/Mergedata.txt");
  }

  public static void shuffle(String filename){
  	shuffle(filename,true);
  }

  public static void shuffle(String filename, boolean hasHeader){
  	System.gc();
  	long availableMem = getFreeMem();
  	long minFreeMem = Math.round(availableMem * minFreeMemFraction);

  	if (verbose) {
  		System.out.println("Starting shuffle");
  		if (chunckSize == -1)
  			System.out.println("Memory available for shuffling: " + availableMem + " bytes. Min free = " + minFreeMem);
  	}

  	ReadCSVFile in = new ReadCSVFile(filename);
  	Iterator<List<String>> iterator = in.iterator();

  	List<String> header = null;
  	if (hasHeader)
  		header = iterator.next();

  	int nrOfFiles = 0;
  	int shuffled = 0;
  	while (iterator.hasNext()) {
  		ProgressHandler.reportProgress();
  		List<List<String>> tempRows; 
  		if (chunckSize == -1)
  			tempRows = readUntilMemFull(iterator, minFreeMem);
  		else 
  			tempRows = readRows(iterator, chunckSize);

  		// shuffle the rows
  		ProgressHandler.reportProgress();
  		Collections.shuffle(tempRows);

  		String tempFilename = generateFilename(filename, nrOfFiles++);

  		// write temp file to disk
  		ProgressHandler.reportProgress();
  		writeToDisk(header, tempRows, tempFilename);

  		shuffled += tempRows.size();
  		if (verbose) 
  			System.out.println("-Shuffled " + shuffled + " lines");
  	}
  	mergeByBatches(nrOfFiles, filename, hasHeader);
  }

  private static void mergeByBatches(int nrOfFiles, String source, boolean hasHeader) {
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
        mergeFiles(sourceBase, from, to, targetFilename, hasHeader);
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

  private static void deleteTempFiles(String base, int start, int end) {
    for (int i = start; i< end; i++)
      (new File(generateFilename(base, i))).delete();
  }
  
  private static String generateFilename(String base, int number){
    return base + "_" + number + ".tmp";
  }

  private static void mergeFiles(String sourceBase, int start, int end, String target, boolean hasHeader) {
    List<Iterator<List<String>>> tempFiles = new ArrayList<Iterator<List<String>>>();

    List<String> header = null; 
    for (int i = start; i < end; i++){
      ReadCSVFile tempFile = new ReadCSVFile(generateFilename(sourceBase, i));
      Iterator<List<String>> iterator = tempFile.iterator();//.getIterator();
      if (hasHeader && iterator.hasNext()){
        if (tempFiles.size() == 0) //its the first file
          header = iterator.next();
        else
          iterator.next();
      }
      if (iterator.hasNext())
        tempFiles.add(iterator);
    }
    WriteCSVFile out = new WriteCSVFile(target);
    if (hasHeader)
      out.write(header);
    while (tempFiles.size() != 0){
      ProgressHandler.reportProgress();
      //Find random file to pick from:
      int i = random.nextInt(tempFiles.size());
      Iterator<List<String>> iterator = tempFiles.get(i);
      out.write(iterator.next());
      if (!iterator.hasNext())
      	tempFiles.remove(i);
    }
    out.close();
  }
}