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
