package org.erasmusmc.jerboa.postProcessing;

import java.util.Iterator;
import java.util.List;

import org.erasmusmc.jerboa.dataClasses.AggregatedDataFileReader;
import org.erasmusmc.utilities.WriteCSVFile;

public class TableExtractor {

	public static void main(String[] args) {
		process ("/home/data/OSIM/AggregatedData.txt","/home/data/OSIM/", false, true, false);
	}
	
	public static void process(String inputFile, String outputFolder, boolean table1, boolean table2, boolean table3){
		AggregatedDataFileReader in = new AggregatedDataFileReader(inputFile);
		if (table1)
		  writeFile(in, "Aggregate by ATC combination", outputFolder + "table1.txt");
		if (table2)
		  writeFile(in, "Aggregate by ATC", outputFolder + "table2.txt");
		if (table3)
		  writeFile(in, "Aggregate", outputFolder + "table3.txt");
	}

	private static void writeFile(AggregatedDataFileReader in, String tableName, String outputFilename) {
		in.moveToTable(tableName);
		WriteCSVFile out = new WriteCSVFile(outputFilename);
		Iterator<List<String>> iterator = in.getStringIterator();
		while (iterator.hasNext())
			out.write(iterator.next());
		out.close();
	}

}
