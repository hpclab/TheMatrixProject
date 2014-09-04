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

import java.util.List;

import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

public class RemoveLinesWithNegativeNumbers {

  public static String folder = "/home/schuemie/Research/Jerboa/";
  public static String source = folder + "Prescriptions.txt";
  public static String target = folder + "Prescriptions_fixed.txt";
	public static void main(String[] args) {
		WriteCSVFile out = new WriteCSVFile(target);
		int keepCount = 0;
		int removeCount = 0;
		for (List<String> row : new ReadCSVFile(source)){
			boolean negative = false;
			for (String cell : row)
				if (cell.startsWith("-"))
					negative = true;
			if (negative)
				removeCount++;
			else {
				keepCount++;
				out.write(row);
			}
		}
		out.close();
		
		System.out.println("Keep: " + keepCount + ", Removed: " + removeCount);
	}

}
