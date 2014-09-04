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

import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.utils.DataType;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.google.code.externalsortingjava.ExternalSort;


/**
 * @author marco
 *
 *	FIXME delete param semiSortFirstVariable
 */
public class NewFileSort 
{
	public static boolean caseSensitiveColumnNames = false;
	
	/**
	 * only for testing
	 * @param args
	 */
	public static void main(String[] args)
	{
		String filename = "iad/OUTPAT_RED.csv";
		String outFilename = "/mnt/DATA/OUTPAT_RED_OUTPUT.csv";
		String[] columnnames = null;
		columnnames = new String[]{"VALUE"};
		int columnIndex = 2;
//		sort(filename, outFilename, columnnames);
	}

		
	
	/**
	 * Interface of sort method for writing of the output in different file and for sorting on one attribute.
	 * @param filename : the name of the file (path included) of the input file.
	 * @param outputFilename: the name of the output file (path included).
	 * @param columnname : name of the attribute for sorting.
	 */
/**	public static void sort(String filename, String outputFilename, String columnname) 
	{
		sort(filename, outputFilename, new String[]{columnname}, -1, null, false);
	}

	commented out, may be used in the future if we add the dataset schema to the parameters
**/
	/**
	 * Interface of sort method for writing of the output in the source file and for sorting on more attributes.
	 * @param filename : the name of the file (path included) of the input file (the output file is the same).
	 * @param columnnames : Array of Strings with the names of the attributes for sorting.
	 */
	public static void sort(String filename, String[] columnnames, DatasetSchema datasetSchema)
	{
		sort(filename, filename, columnnames, datasetSchema);
	}
	

	/**
	 * Interface of sort method for writing of the output in different file and for  sorting on one attribute.
	 * @param filename  the name of the file (path included) of the input file (the output file is the same).
	 * @param outputFilename  the name of the output file (path included).
	 * @param columnnames  Array of Strings with the names of the attributes for sorting.
	 * TODO add documentation for attributes param
	 */
	public static void sort(String filename, String outputFilename, String[] columnnames, DatasetSchema datasetSchema)
	{
		boolean hasHeader = (columnnames != null);
		Iterator<List<ASCIIString>> iterator = new ReadCSVASCIIFile(filename).iterator();
		int numHeader=0;
	  	List<ASCIIString> header = null;
	  	String headerString = "";
	  	if (hasHeader)
	  		if (iterator.hasNext()){
	  			numHeader=1;
	  			header = iterator.next();
	  		  	for(int i=0; i<header.size();i++)
	  		  		if (i==0) headerString += header.get(i);
	  		  		else headerString += "," +header.get(i);
	  		}
	  	else
	  		throw new RuntimeException("Header row expected but not found in file: " + filename);
	  	
	  	//force to skip one line
	  	//numHeader=1;
	  	// ???
	  	
	  	
	  	Comparator<String> comparator = new CSVComparator(columnnames, header, datasetSchema);

		String output = outputFilename;
		
		try
		{
			long before = System.currentTimeMillis();
			List<File> l = ExternalSort.sortInBatch(new File(filename), comparator,numHeader,new File(Dynamic.getIadPath()));
			
			ExternalSort.mergeSortedFiles(l, new File(output), comparator,headerString);
			long after = System.currentTimeMillis();
			
			long seconds = (after-before) / (1000);
			LogST.logP(1, "NewFileSorter.sort() Sort done in "+filename+": Time elapsed (seconds) "+seconds);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	

}

class CSVComparator implements Comparator<String> {

	private int[] orderingSeq = null;
	public static boolean caseSensitiveColumnNames;
	private DataType[] attributesType;
	private List<ASCIIString> headerList;

	public CSVComparator(String[] columnnames, List<ASCIIString> header, DatasetSchema datasetSchema){
		int[] indexesToCompare;
		
		if (columnnames == null) {
			LogST.logP(0, "NewFileSort CSVComparator() called with empty columnames");
			throw new Error ("NewFileSort CSVComparator() called with empty columnames");
		}

		indexesToCompare = new int[columnnames.length];
		attributesType = new DataType[columnnames.length];
		for (int i = 0; i < columnnames.length; i++){
			indexesToCompare[i] = getIndexForColumn(header, new ASCIIString(columnnames[i]));
			attributesType[i] = datasetSchema.get((Object) columnnames[i]).type;
		}
		headerList = header;
		orderingSeq = indexesToCompare;
	}
	
	@Override
	public int compare(String csv1, String csv2) {
		String delims = ",";
		String[] firstTokens = csv1.split(delims,headerList.size());
		String[] secondTokens = csv2.split(delims,headerList.size());
		/*
		StringBuilder DBA = new StringBuilder(1024);
		for(String s : firstTokens) {
			DBA.append("|@|");
			DBA.append(s);
		}
		StringBuilder DBB = new StringBuilder(1024);
		for(String s : secondTokens) {
			DBB.append("|@|");
		    DBB.append(s);
		}
		
		LogST.logP(0, "compare first  :"+DBA.toString());
		LogST.logP(0, "compare second :"+DBB.toString());
			*/	
		// we must have exactly the right number of elements in both rows
		if (firstTokens.length < orderingSeq.length || secondTokens.length < orderingSeq.length || firstTokens.length != secondTokens.length )
		{
			LogST.logP(-1, "New Sorter compare called with short / mismatched records :"+ orderingSeq.length+" "+firstTokens.length+" "+secondTokens.length);
			throw new Error("New Sorter compare called with short / mismatched records :"+ orderingSeq.length+" "+firstTokens.length+" "+secondTokens.length);
		}
			
		int checks = orderingSeq.length;
		
		int res = 0;
		
		for(int i=0;i<checks;i++){
			
			Comparable i1 = firstTokens[orderingSeq[i]];
			Comparable i2 = secondTokens[orderingSeq[i]];
			if (i1.toString().equals("") && i2.toString().equals("")) res =0;
			else if (i1.toString().equals("")) res= 1; 
			else if (i2.toString().equals("")) res=-1;
			else{
				try{
	
					switch(attributesType[i]) {
						case STRING:
							res = i1.compareTo(i2);
							break;
						case INT:
							i1 = Integer.parseInt((String)i1);
							i2 = Integer.parseInt((String)i2);
							res = i1.compareTo(i2);
							break;
						case FLOAT:
							i1 = Float.parseFloat((String)i1);
							i2 = Float.parseFloat((String)i2);
							res = i1.compareTo(i2);
							break;
						default:
							LogST.logP(0, "Error NewFileSort.compare() : CSV value not permitted");
					}
				} catch (Exception e){
					LogST.logException(e);
					throw new Error("New Sorter compare data error? Got exception "+e.toString());
				}
			}
			if (res != 0) return res;
		}
		
		return res;
		
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
