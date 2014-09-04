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

import org.erasmusmc.jerboa.calculations.Power;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

public class PowerCalculations {
	public static String irColumnName = "Incidence (/ 100000 person years)";
	public static double alpha = 0.05;
	public static double power = 0.8;
	
	public static void main(String[] args) {
		String folder = "C:/home/schuemie/Research/SignalGenerationCompare/Pharmo - Gold/";
		process(folder+"incidenceRatesPharmo_Gold.csv",folder+"PowerCalculations.csv", new double[]{2,3,4});
	}
	
	public static void process(String source, String target, double[] rrs){
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		WriteCSVFile out = new WriteCSVFile(target);
		List<String> header = iterator.next();
		int irCol = header.indexOf(irColumnName);
		if (irCol == -1)
			throw new RuntimeException("Could not find IR column in " + source);
		header.add("alpha");
		header.add("power");
		for (double rr : rrs)
			header.add("Required days (RR="+rr+")");
		out.write(header);
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			cells.add(Double.toString(alpha));
			cells.add(Double.toString(power));
			double ir = Double.parseDouble(cells.get(irCol))/(double)100000; //IR was specified per 100.000 years, should be per year
			for (double rr : rrs)
				cells.add(Double.toString(Power.exposureNeeded(alpha, power, rr, ir)));
			out.write(cells);
		}
		
		out.close();
		
	}
}
