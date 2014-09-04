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
