
package com.google.code.externalsortingjava;
// filename: ExternalSort.java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Deflater;


/**
* Goal: offer a generic external-memory sorting program in Java.
* 
* It must be : 
*  - hackable (easy to adapt)
*  - scalable to large files
*  - sensibly efficient.
*
* This software is in the public domain.
*
* Usage: 
*  java com/google/code/externalsorting/ExternalSort somefile.txt out.txt
* 
* You can change the default maximal number of temporary files with the -t flag:
*  java com/google/code/externalsorting/ExternalSort somefile.txt out.txt -t 3
*
* For very large files, you might want to use an appropriate flag to allocate
* more memory to the Java VM: 
*  java -Xms2G com/google/code/externalsorting/ExternalSort somefile.txt out.txt
*
* By (in alphabetical order) 
*   Philippe Beaudoin, Eleftherios Chetzakis, Jon Elsas,  Christan Grant,
*    Daniel Haran, Daniel Lemire, Sugumaran Harikrishnan, Jerry Yang,
*  First published: April 2010
* originally posted at 
*  http://lemire.me/blog/archives/2010/04/01/external-memory-sorting-in-java/
*/
public class ExternalSort {
	
	static int DEFAULTMAXTEMPFILES = 1024;
	 
	// we divide the file into small blocks. If the blocks
	// are too small, we shall create too many temporary files. 
	// If they are too big, we shall be using too much memory. 
	public static long estimateBestSizeOfBlocks(File filetobesorted, int maxtmpfiles) {
		long sizeoffile = filetobesorted.length() * 2;
		/**
		* We multiply by two because later on someone insisted on counting the memory
		* usage as 2 bytes per character. By this model, loading a file with 1 character
		* will use 2 bytes.
		*/ 
		// we don't want to open up much more than maxtmpfiles temporary files, better run
		// out of memory first.
		long blocksize = sizeoffile / maxtmpfiles + (sizeoffile % maxtmpfiles == 0 ? 0 : 1) ;
		
		// on the other hand, we don't want to create many temporary files
		// for naught. If blocksize is smaller than half the free memory, grow it.
		long freemem = Runtime.getRuntime().freeMemory();
		if( blocksize < freemem/2) {
		    blocksize = freemem/2;
		} 
		return blocksize;
	}

	/**
	 * This will simply load the file by blocks of x rows, then
	 * sort them in-memory, and write the result to  
	 * temporary files that have to be merged later.
	 * 
	 * @param file some flat  file
	 * @param cmp string comparator 
	 * @return a list of temporary flat files
	 */
	public static List<File> sortInBatch(File file, Comparator<String> cmp) throws IOException {		return sortInBatch(file, cmp,DEFAULTMAXTEMPFILES,Charset.defaultCharset(),null,false);	}
	
	/**
	 * This will simply load the file by blocks of x rows, then
	 * sort them in-memory, and write the result to  
	 * temporary files that have to be merged later.
	 * 
	 * @param file some flat  file
	 * @param cmp string comparator 
	 * @return a list of temporary flat files
	 */
	public static List<File> sortInBatch(File file, Comparator<String> cmp, int NumHeader) throws IOException {
		return sortInBatch(file, cmp,DEFAULTMAXTEMPFILES,Charset.defaultCharset(),null,false,NumHeader);
	}
	/**
	 * This will simply load the file by blocks of x rows, then
	 * sort them in-memory, and write the result to  
	 * temporary files that have to be merged later.
	 * 
	 * @param file some flat  file
	 * @param cmp string comparator 
	 * @return a list of temporary flat files
	 */
	public static List<File> sortInBatch(File file, Comparator<String> cmp, int NumHeader,File tmpdirectory) throws IOException {
		return sortInBatch(file, cmp,DEFAULTMAXTEMPFILES,Charset.defaultCharset(),tmpdirectory ,false,NumHeader);
	}
	
	/**
	 * This will simply load the file by blocks of x rows, then
	 * sort them in-memory, and write the result to  
	 * temporary files that have to be merged later.
	 * 
	 * @param file some flat  file
	 * @param cmp string comparator 
	 * @param distinct Pass <code>true</code> if duplicate lines should be discarded. 
	 * @return a list of temporary flat files
	 */
	public static List<File> sortInBatch(File file, Comparator<String> cmp, boolean distinct) throws IOException {
		return sortInBatch(file, cmp,DEFAULTMAXTEMPFILES,Charset.defaultCharset(),null,distinct);
	}
	

    /**
     * This will simply load the file by blocks of x rows, then
     * sort them in-memory, and write the result to 
     * temporary files that have to be merged later. You can
     * specify a bound on the number of temporary files that
     * will be created.
     * 
     * @param file some flat  file
     * @param cmp string comparator 
     * @param maxtmpfiles maximal number of temporary files
     * @param cs character set to use  (can use Charset.defaultCharset()) 
     * @param tmpdirectory location of the temporary files (set to null for default location)
     * @param distinct Pass <code>true</code> if duplicate lines should be discarded.
     * @param numHeader number of lines to preclude before sorting starts 
     * @param usegzip use gzip compression for the temporary files
     * @return a list of temporary flat files
     */
    public static List<File> sortInBatch(File file, Comparator<String> cmp, int maxtmpfiles, Charset cs, File tmpdirectory, boolean distinct, int numHeader, boolean usegzip) throws IOException {
        List<File> files = new ArrayList<File>();
        BufferedReader fbr = new BufferedReader(new InputStreamReader(new FileInputStream(file),cs));
        long blocksize = estimateBestSizeOfBlocks(file,maxtmpfiles);// in bytes

        try{
            List<String> tmplist =  new ArrayList<String>();
            String line = "";
            try {
                int counter = 0;
                while(line != null) {
                    long currentblocksize = 0;// in bytes
                    while((currentblocksize < blocksize) 
                    &&(   (line = fbr.readLine()) != null) ){ // as long as you have enough memory
                        if (counter < numHeader) {
                            counter++;
                            continue;
                        }
                        tmplist.add(line);
                        //ram usage estimation, not very accurate, still more realistic that the simple 2 * String.length
                        currentblocksize += StringSizeEstimator.estimatedSizeOf(line);
                    }
                    files.add(sortAndSave(tmplist, cmp, cs, tmpdirectory, distinct,usegzip));
                    tmplist.clear();
                }
            } catch(EOFException oef) {
                if(tmplist.size()>0) {
                    files.add(sortAndSave(tmplist, cmp, cs, tmpdirectory, distinct,usegzip));
                    tmplist.clear();
                }
            }
        } finally {
            fbr.close();
        }
        return files;
    }

	/**
	 * This will simply load the file by blocks of x rows, then
	 * sort them in-memory, and write the result to 
	 * temporary files that have to be merged later. You can
	 * specify a bound on the number of temporary files that
	 * will be created.
	 * 
	 * @param file some flat  file
	 * @param cmp string comparator 
	 * @param maxtmpfiles maximal number of temporary files
	 * @param cs character set to use  (can use Charset.defaultCharset()) 
     * @param tmpdirectory location of the temporary files (set to null for default location)
     * @param distinct Pass <code>true</code> if duplicate lines should be discarded.
	 * @return a list of temporary flat files
	 */
	public static List<File> sortInBatch(File file, Comparator<String> cmp, int maxtmpfiles, Charset cs, File tmpdirectory, boolean distinct,int numHeader) throws IOException {
		return sortInBatch(file, cmp, maxtmpfiles, cs, tmpdirectory, distinct, numHeader,false);
	}

	/**
	 * This will simply load the file by blocks of x rows, then
	 * sort them in-memory, and write the result to 
	 * temporary files that have to be merged later. You can
	 * specify a bound on the number of temporary files that
	 * will be created.
	 * 
	 * @param file some flat  file
	 * @param cmp string comparator 
	 * @param maxtmpfiles maximal number of temporary files
	 * @param cs character set to use  (can use Charset.defaultCharset()) 
     * @param tmpdirectory location of the temporary files (set to null for default location)
     * @param distinct Pass <code>true</code> if duplicate lines should be discarded.
	 * @return a list of temporary flat files
	 */
	public static List<File> sortInBatch(File file, Comparator<String> cmp, int maxtmpfiles, Charset cs, File tmpdirectory, boolean distinct) throws IOException {
		return sortInBatch(file, cmp, maxtmpfiles, cs, tmpdirectory, distinct, 0,false);
	}
     /**
     * Sort a list and save it to a temporary file 
     *
     * @return the file containing the sorted data
     * @param tmplist data to be sorted
     * @param cmp string comparator
     * @param cs charset to use for output (can use Charset.defaultCharset())
     * @param tmpdirectory location of the temporary files (set to null for default location)
     * @param distinct Pass <code>true</code> if duplicate lines should be discarded.
     */ 
	public static File sortAndSave(List<String> tmplist, Comparator<String> cmp, Charset cs, File tmpdirectory, boolean distinct, boolean usegzip) throws IOException  {
		Collections.sort(tmplist,cmp);  
		File newtmpfile = File.createTempFile("sortInBatch", "flatfile", tmpdirectory);
		newtmpfile.deleteOnExit();
		OutputStream out = new FileOutputStream(newtmpfile);
	    int ZIPBUFFERSIZE = 2048;
		if(usegzip) out = new GZIPOutputStream(out,ZIPBUFFERSIZE){{def.setLevel(Deflater.BEST_SPEED);}};
		BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(out,cs));
		String lastLine = null;
		try {
			for(String r : tmplist) {
				// Skip duplicate lines
				if( !distinct || !r.equals(lastLine) ){
					fbw.write(r);
					fbw.newLine();
					lastLine=r;
				}
			}
		} finally {
			fbw.close();
		}
		return newtmpfile;
	}
	
	/**
     * Sort a list and save it to a temporary file 
     *
     * @return the file containing the sorted data
     * @param tmplist data to be sorted
     * @param cmp string comparator
     * @param cs charset to use for output (can use Charset.defaultCharset())
     * @param tmpdirectory location of the temporary files (set to null for default location)
     */ 
	public static File sortAndSave(List<String> tmplist, Comparator<String> cmp, Charset cs, File tmpdirectory) throws IOException  {
		return sortAndSave(tmplist, cmp, cs, tmpdirectory,false,false);
	}
	
	/**
	 * This merges a bunch of temporary flat files 
	 * @param ??? files ???
	 * @param ??? outputfile  ???
     * @return The number of lines sorted. (P. Beaudoin)
	 */
	public static int mergeSortedFiles(List<File> files, File outputfile, final Comparator<String> cmp) throws IOException {
		return mergeSortedFiles(files, outputfile, cmp, Charset.defaultCharset()); 
	}
	
	public static int mergeSortedFiles(List<File> files, File outputfile, final Comparator<String> cmp, String header) throws IOException {
		return mergeSortedFiles(files, outputfile, cmp, Charset.defaultCharset(),false,false,false,header); 
	}
	/**
	 * This merges a bunch of temporary flat files 
	 * @param files
	 * @param output file
     * @return The number of lines sorted. (P. Beaudoin)
	 */
	public static int mergeSortedFiles(List<File> files, File outputfile, final Comparator<String> cmp, boolean distinct) throws IOException {
		return mergeSortedFiles(files, outputfile, cmp, Charset.defaultCharset(), distinct); 
	}
	
    /**
    * This merges a bunch of temporary flat files 
    * @param files The {@link List} of sorted {@link File}s to be merged.
    * @param Charset character set to use to load the strings
    * @param distinct Pass <code>true</code> if duplicate lines should be discarded. (elchetz@gmail.com)
    * @param outputfile The output {@link File} to merge the results to.
    * @param cmp The {@link Comparator} to use to compare {@link String}s.
    * @param cs The {@link Charset} to be used for the byte to character conversion.
    * @param append Pass <code>true</code> if result should append to {@link File} instead of overwrite.  Default to be false for overloading methods.
    * @param usegzip assumes we used gzip compression for temporary files
    * @return The number of lines sorted. (P. Beaudoin)
    * @since v0.1.4
    */
	public static int mergeSortedFiles(List<File> files, File outputfile, final Comparator<String> cmp, Charset cs, boolean distinct, boolean append, boolean usegzip) throws IOException {
	       PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(11, 
	                new Comparator<BinaryFileBuffer>() {
	                  @Override
	                public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {
	                    return cmp.compare(i.peek(), j.peek());
	                  }
	                }
	            );
	            for (File f : files) {
	                BinaryFileBuffer bfb = new BinaryFileBuffer(f,cs,usegzip);
	                pq.add(bfb);
	            }
	            BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputfile, append),cs));
	            int rowcounter = 0;
	            String lastLine=null;
	            try {
	                while(pq.size()>0) {
	                    BinaryFileBuffer bfb = pq.poll();
	                    String r = bfb.pop();
	                    // Skip duplicate lines
	                    if( !distinct || !r.equals(lastLine) ){
	                        fbw.write(r);
	                        fbw.newLine();
	                        lastLine=r;
	                    }
	                    ++rowcounter;
	                    if(bfb.empty()) {
	                        bfb.fbr.close();
	                        bfb.originalfile.delete();// we don't need you anymore
	                    } else {
	                        pq.add(bfb); // add it back
	                    }
	                }
	            } finally { 
	                fbw.close();
	                for(BinaryFileBuffer bfb : pq ) bfb.close();
	            }
	            return rowcounter;
	}
	
	 /**
	    * This merges a bunch of temporary flat files adding the header to final file
	    * @param files The {@link List} of sorted {@link File}s to be merged.
	    * @param Charset character set to use to load the strings
	    * @param distinct Pass <code>true</code> if duplicate lines should be discarded. (elchetz@gmail.com)
	    * @param outputfile The output {@link File} to merge the results to.
	    * @param cmp The {@link Comparator} to use to compare {@link String}s.
	    * @param cs The {@link Charset} to be used for the byte to character conversion.
	    * @param append Pass <code>true</code> if result should append to {@link File} instead of overwrite.  Default to be false for overloading methods.
	    * @param usegzip assumes we used gzip compression for temporary files
	    * @return The number of lines sorted. (P. Beaudoin)
	    * @since v0.1.4
	    */
	public static int mergeSortedFiles(List<File> files, File outputfile, final Comparator<String> cmp, Charset cs, boolean distinct, boolean append, boolean usegzip, String header) throws IOException {
	       PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(11, 
	                new Comparator<BinaryFileBuffer>() {
	                  @Override
	                public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {
	                    return cmp.compare(i.peek(), j.peek());
	                  }
	                }
	            );
	            for (File f : files) {
	                BinaryFileBuffer bfb = new BinaryFileBuffer(f,cs,usegzip);
	                pq.add(bfb);
	            }
	            BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputfile, append),cs));
	            //write header
                fbw.write(header);
                fbw.newLine();
	            int rowcounter = 0;
	            String lastLine=null;
	            try {
	                while(pq.size()>0) {
	                    BinaryFileBuffer bfb = pq.poll();
	                    String r = bfb.pop();
	                   
	                    if (r != null) {
	                    	/**
	                    	 * FIXME Broken quick patch : if(r!=null) prevents an
	                    	 * exception, but distinct may NOT work at EOF (may lose last line)
	                    	 */
	                    	// Skip duplicate lines
	                    	if( !distinct || !r.equals(lastLine) ){
	                    		fbw.write(r);
	                    		fbw.newLine();
	                    		lastLine=r;
	                    	}
	                    	++rowcounter;
	                    }
	                    if(bfb.empty()) {
	                        bfb.fbr.close();
	                        bfb.originalfile.delete();// we don't need you anymore
	                    } else {
	                        pq.add(bfb); // add it back
	                    }
	                }
	            } finally { 
	                fbw.close();
	                for(BinaryFileBuffer bfb : pq ) bfb.close();
	            }
	            return rowcounter;
	}
	
	/**
	 * This merges a bunch of temporary flat files 
	 * @param files The {@link List} of sorted {@link File}s to be merged.
	 * @param Charset character set to use to load the strings
	 * @param distinct Pass <code>true</code> if duplicate lines should be discarded. (elchetz@gmail.com)
	 * @param outputfile The output {@link File} to merge the results to.
	 * @param cmp The {@link Comparator} to use to compare {@link String}s.
	 * @param cs The {@link Charset} to be used for the byte to character conversion.
     * @return The number of lines sorted. (P. Beaudoin)
     * @since v0.1.2
	 */
	public static int mergeSortedFiles(List<File> files, File outputfile, final Comparator<String> cmp, Charset cs, boolean distinct) throws IOException {
		return mergeSortedFiles(files, outputfile, cmp, cs, distinct, false,false);
	}

	/**
	 * This merges a bunch of temporary flat files 
	 * @param files
	 * @param output file
	 * @param Charset character set to use to load the strings
     * @return The number of lines sorted. (P. Beaudoin)
	 */
	public static int mergeSortedFiles(List<File> files, File outputfile, final Comparator<String> cmp, Charset cs) throws IOException {
		return mergeSortedFiles(files,  outputfile, cmp,  cs, false);
	}
	
	public static void displayUsage() {
		System.out.println("java com.google.externalsorting.ExternalSort inputfile outputfile");
		System.out.println("Flags are:");
		System.out.println("-v or --verbose: verbose output");
		System.out.println("-d or --distinct: prune duplicate lines");
		System.out.println("-t or --maxtmpfiles (followed by an integer): specify an upper bound on the number of temporary files");
		System.out.println("-c or --charset (followed by a charset code): specify the character set to use (for sorting)");
		System.out.println("-z or --gzip: use compression for the temporary files");
		System.out.println("-H or --header (followed by an integer): ignore the first few lines");
		System.out.println("-s or --store (following by a path): where to store the temporary files");
		System.out.println("-h or --help: display this message");
	}

	public static void main(String[] args) throws IOException {
		boolean verbose = false;
		boolean distinct = false;
		int maxtmpfiles = DEFAULTMAXTEMPFILES;
		Charset cs = Charset.defaultCharset();
		String inputfile=null, outputfile=null;
		File tempFileStore = null;
		boolean usegzip = false;
		int headersize = 0;
		for(int param = 0; param<args.length; ++param) {
			if(args[param].equals("-v") ||  args[param].equals("--verbose")){
			  verbose = true;
			} else if ((args[param].equals("-h") || args[param].equals("--help"))) {
				displayUsage();
				return;
			} else if ((args[param].equals("-d") || args[param].equals("--distinct"))) {
				distinct=true;
			} else if ((args[param].equals("-t") ||  args[param].equals("--maxtmpfiles")) && args.length>param+1) {
				param++;
			    maxtmpfiles = Integer.parseInt(args[param]);
				if(headersize < 0) {
					System.err.println("maxtmpfiles should be positive");
				}
			} else if ((args[param].equals("-c") || args[param].equals("--charset")) && args.length>param+1) {
				param++;
				cs = Charset.forName(args[param]);
			} else if ((args[param].equals("-z") || args[param].equals("--gzip"))) {
				usegzip = true;
			} else if ((args[param].equals("-H") || args[param].equals("--header")) && args.length>param+1) {
				param++;
				headersize = Integer.parseInt(args[param]);
				if(headersize < 0) {
					System.err.println("headersize should be positive");
				}
			} else if ((args[param].equals("-s") || args[param].equals("--store")) && args.length>param+1) {
				param++;
				tempFileStore = new File(args[param]);
			} else {
				if(inputfile == null) 
				  inputfile = args[param];
				else if (outputfile == null)
				  outputfile = args[param];
				else System.out.println("Unparsed: "+args[param]); 
			}
		}
		if(outputfile == null) {
			System.out.println("please provide input and output file names");
			displayUsage();
			return;
		}
		Comparator<String> comparator = new Comparator<String>() {
			@Override
			public int compare(String r1, String r2){
				return r1.compareTo(r2);}};
		List<File> l = sortInBatch(new File(inputfile), comparator, maxtmpfiles,cs,tempFileStore,distinct,headersize, usegzip) ;
		if(verbose) System.out.println("created "+l.size()+" tmp files");
		mergeSortedFiles(l, new File(outputfile), comparator,cs, distinct,false, usegzip);
	}

	
}


class BinaryFileBuffer  {
	public static int BUFFERSIZE = 2048;
	public BufferedReader fbr;
	public File originalfile;
	private String cache;
	private boolean empty;
	
	public BinaryFileBuffer(File f, Charset cs, boolean usegzip) throws IOException {
		this.originalfile = f;
		InputStream in = new FileInputStream(f);
		if(usegzip) {
			this.fbr = new BufferedReader(new InputStreamReader(new GZIPInputStream(in, BUFFERSIZE),cs));
		} else {
			this.fbr = new BufferedReader(new InputStreamReader(in,cs));
		}
		reload();
	}
	
	public boolean empty() {
		return this.empty;
	}
	
	private void reload() throws IOException {
		try {
          if((this.cache = this.fbr.readLine()) == null){
            this.empty = true;
            this.cache = null;
          }
          else{
            this.empty = false;
          }
      } catch(EOFException oef) {
        this.empty = true;
        this.cache = null;
      }
	}
	
	public void close() throws IOException {
		this.fbr.close();
	}
	
	
	public String peek() {
		if(empty()) return null;
		return this.cache.toString();
	}
	public String pop() throws IOException {
	  String answer = peek();
		reload();
	  return answer;
	}
	
	
}
