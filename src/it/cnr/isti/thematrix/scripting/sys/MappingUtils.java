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
package it.cnr.isti.thematrix.scripting.sys;

import it.cnr.isti.thematrix.configuration.LogST;

import java.io.PrintStream;

/**
 * Class collecting various static utility functions operating on the class hierarchy holding the mapping information
 * 
 * @author massimo
 * 
 */
public class MappingUtils {

	static PrintStream out = System.out;

	public static void setPrintStream(PrintStream p)
	{  out=p; }
	
	/**
	 * Prints all the schemata to the class PrintStream (by default System.out)
	 */
	public static void IADSchemataToString() {

		SchemaTable predefinedSchemata = TheMatrixSys.getPredefinedSchemata();

		for (String table : predefinedSchemata.keySet()) {
			DatasetSchema schema = predefinedSchemata.get(table);
			out.println(schema.name); // use name()
			for (String name : schema.keySet())
				out.println(schema.get(name).toString()); // use name() and type
		}
		return;
	}

	
	/**
	 * Prints all the schemata as XML to the class PrintStream (by default System.out)
	 * using void or simple mappings.
	 * 
	 * @param true to generate a file with only void mappings
	 */
	public static void IADSchemataToXMLMapping(boolean useVoid) {

		// if (!useVoid) {
		// LogST.logP(0,"WARNING IADSchemataToXMLMapping: SimpleMapping unsupported");
		// return;
		// }

		SchemaTable predefinedSchemata = TheMatrixSys.getPredefinedSchemata();

		out.println("\n\n\n---------------- Example mapping.xml file ----------------\n\n\n");

		out.println("<!-- Mapping template for the defined schemata, with "
				+(useVoid?"void mappings.  ":"simple mappings.")+"          -->\n"
				+ "<!-- Revision 26/04/2013                                                       -->\n"
				+ "<!-- Note that all tag contents are examples, they shall                       -->\n"
				+ "<!-- be customized according to your local database schema.                    -->\n"
				+ "<!-- Please refer to the documentation and report problems on the mailing list -->\n");

		out.println("<iadMapping>");
		for (String table : predefinedSchemata.keySet()) // this is not ordered unfortunately
		{
			DatasetSchema schema = predefinedSchemata.get(table);

			out.println("   <dataset name=\"" + schema.name + "\">");

//			out.println("   <tableSource>\n");
			
			// FIXME here the file name should be set properly
			out.println("      <joinName>" + schema.name.toUpperCase()
					+ "</joinName> <!-- CSV file name, can be changed -->");

			out.println("      <joinClause>"
					+ "</joinClause> <!-- fill in the source table, or the full join clause -->");
//			out.println("   </tableSource>");

			for (String name : schema.fieldNames()) { // same field order as their definition
				if (useVoid)
					printVoidMapping("      ", schema.get(name).name);
				else
					printSimpleMapping("      ", schema.get(name).name, schema.get(name).type.toString());
			}
			out.println("   </dataset>\n"); // extra newline
		}
		out.println("</iadMapping>");
		out.println("\n\n\n------------- End of Example mapping.xml file -------------\n\n\n");
		return;
	}

	
	/**
	 * Print the XML dump for a Void Mapping
	 * @param indent whitespace string used to provide indentation
	 * @param fieldName the name of the field defined by this mapping
	 */
	private static void printVoidMapping(String indent, String fieldName) {
		out.println(indent + "<voidMapping name=\"" + fieldName + "\">");
		out.println(indent + "</voidMapping>");
	}

	/**
 	 * Print the XML dump for a SimpleMapping
	 * @param indent whitespace string used to provide indentation
	 * @param fieldName the name of the field defined by this mapping
	 * @param comment info text to be put inside an XML comment inside the mapping tag (e.g. type) 
	 */
	private static void printSimpleMapping(String indent, String fieldName, String comment) {
		out.println(indent + "<simpleMapping name=\"" + fieldName + "\"> <!-- " + comment + " -->");
		out.println(indent + "   <sourceTable>INSERT SOURCE TABLE NAME</sourceTable>");
		out.println(indent + "   <sourceAttribute>INSERT SOURCE ATTRIBUTE NAME</sourceAttribute>");
		out.println(indent + "</simpleMapping>");
	}

	/**
	 * This method prints the expected size of all the defined schemata.
	 * 
	 * TODO to be refined to return the size exp/max for each schema to allow file size estimates.
	 * 
	 * @return
	 */
	public static int IADSchemataExpectedOutputSize() {
		SchemaTable predefinedSchemata = TheMatrixSys.getPredefinedSchemata();
		for (String table : predefinedSchemata.keySet()) {
			DatasetSchema schema = predefinedSchemata.get(table);
			LogST.logP(1, "Expected output size for schema " + schema.name); // use name()
			int sum = 0;
			for (String name : schema.keySet())
				sum += schema.get(name).expectedOutputSizeCSV(); // use name() and type
			LogST.logP(2," \t: " + sum + " bytes");
		}
		return 0;
	}

}
