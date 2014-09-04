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

import java.io.File;

import org.erasmusmc.utilities.ReadTextFile;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteTextFile;

public class TableSplitter {

  /**
   * Reverses the work of the table merger module
   * @param args
   */
	public static void main(String[] args) {
		split("/home/schuemie/Desktop/test/Mergeaggregatedtables.txt");
	}
	
	public static void split(String sourceFile){
		String targetFolder = new File(sourceFile).getParent() + "/";
		split(sourceFile, targetFolder);
	}
	
	public static void split(String sourceFile, String targetFolder){
		WriteTextFile out = null;
		for (String line : new ReadTextFile(sourceFile)){
			if (line.startsWith("*** Start of file ")){
				if (out != null)
					out.close();
				String filename = targetFolder + StringUtilities.findBetween(line, "*** Start of file ", " ***");
				System.out.println("Creating " + filename);
				out = new WriteTextFile(filename);
			} else {
				if (out != null)
					out.writeln(line);
			}
		}
		out.close();
	}

}
