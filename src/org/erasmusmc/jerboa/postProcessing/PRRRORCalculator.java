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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.erasmusmc.jerboa.calculations.Disproportionality;
import org.erasmusmc.jerboa.calculations.Disproportionality.Result;
import org.erasmusmc.utilities.CountingSetLong;
import org.erasmusmc.utilities.ReadCSVFileWithHeader;
import org.erasmusmc.utilities.Row;
import org.erasmusmc.utilities.WriteCSVFile;

public class PRRRORCalculator {

	public static boolean compareAgainstSubATC = false;
	public static int subATCLevel = 4;
	public static enum Method {PRR,ROR,NONE};
	public static Method method = Method.PRR;
	public static Set<Signal> limitToSignals = null;

	public static void process(String source, String target){
		WriteCSVFile out = new WriteCSVFile(target); 
		out.write(generateHeader());
		CountingSetLong<Signal> drugAndEventCounts = new CountingSetLong<Signal>();
		CountingSetLong<Signal> drugNoEventCounts = new CountingSetLong<Signal>();
		if (compareAgainstSubATC) {
			for (Row row : new ReadCSVFileWithHeader(source)){
				String atc = row.get("ATC");
				String eventType = row.get("EventType");
				String subATC = atc.substring(Math.min(atc.length(), subATCLevel));
				Signal signal = new Signal(subATC, eventType);
				long a = Long.parseLong(row.get("w00"));
				drugAndEventCounts.add(signal, a);
				long c = Long.parseLong(row.get("w01"));
				drugNoEventCounts.add(signal, c);
			}
		}

		for (Row row : new ReadCSVFileWithHeader(source)){
			String atc = row.get("ATC");
			String eventType = row.get("EventType");
			if (limitToSignals == null || limitToSignals.contains(new Signal(atc,eventType))){
				long a = Long.parseLong(row.get("w00"));
				long b = Long.parseLong(row.get("w10"));
				long c = Long.parseLong(row.get("w01"));
				long d = Long.parseLong(row.get("w11"));
				if (compareAgainstSubATC){
					String subATC = atc.substring(Math.min(atc.length(), subATCLevel));
					Signal subSignal = new Signal(subATC, eventType);
					d = drugNoEventCounts.getCount(subSignal)- c;
					b = drugAndEventCounts.getCount(subSignal) - a;
				}  			
				Result result;
				if (method == Method.PRR)
					result = Disproportionality.proportionalReportingRatio(a, b, c, d);
				else if (method == Method.ROR)
					result = Disproportionality.reportingOddsRatio(a, b, c, d);
				else
					result = null;
				double expected = (double)(a+c)*(double)(a+b)/(double)(a+b+c+d);
				List<String> cells = new ArrayList<String>();
				cells.add(atc);
				cells.add(eventType);
				if (method != Method.NONE){
					cells.add(Double.toString(result.ratio));
					cells.add(Double.toString(result.p));
					cells.add(Double.toString(result.ci95down));
					cells.add(Double.toString(result.ci95up));
				}
				cells.add(Long.toString(a));
				cells.add(Long.toString(a));
				cells.add(Long.toString(b));
				cells.add(Long.toString(c));
				cells.add(Long.toString(d));
				cells.add(Double.toString(expected));
				out.write(cells);
			}
		}  	 
		out.close();	 
	}

	private static List<String> generateHeader() {
		List<String> header = new ArrayList<String>();
		header.add("ATC");
		header.add("EventType");
		if (method != Method.NONE){
			header.add("RR");
			header.add("P");
			header.add("CI95down");
			header.add("Ci95up");
		}
		header.add("Events");
		header.add("w00");
		header.add("w10");
		header.add("w01");
		header.add("w11");
		header.add("expected");
		return header;
	}
}
