package org.erasmusmc.jerboa.postProcessing.poolDBs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erasmusmc.jerboa.calculations.MetaAnalysis.Model;
import org.erasmusmc.jerboa.calculations.MetaAnalysis.Result;
import org.erasmusmc.jerboa.postProcessing.Signal;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.ReadCSVFileWithHeader;
import org.erasmusmc.utilities.Row;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

public class MetaAnalysis {

	public static enum ParameterType {STANDARD_ERROR,CI95UP};
	public String limitSetFile = null;

	private List<String> dbs = new ArrayList<String>();
	private Map<Signal,Map<String,Data>> signal2DB2Data = new HashMap<Signal, Map<String,Data>>();

	public static void main(String[] args){
		//String folder = "C:/home/data/Pooled/Version 3.0 (Silver)/";
		//MetaAnalysis analysis = new MetaAnalysis();
		//analysis.process(folder+"AllRRLevel7.csv", folder+"MAtest.csv", "RRmh", "CI95up");
		
		String folder = "C:/home/schuemie/Research/SignalGenerationCompare/SimpleExperiment/BHM/";
		MetaAnalysis analysis = new MetaAnalysis();
		analysis.add(exp(folder+"BHM_Aarhus.csv"), "Aarhus", "mu", "se", ParameterType.STANDARD_ERROR);
		analysis.add(exp(folder+"BHM_ARS.csv"), "ARS", "mu", "se", ParameterType.STANDARD_ERROR);
		analysis.add(exp(folder+"BHM_HealthSearch.csv"), "HealthSearch", "mu", "se", ParameterType.STANDARD_ERROR);
		analysis.add(exp(folder+"BHM_IPCI.csv"), "IPCI", "mu", "se", ParameterType.STANDARD_ERROR);
		analysis.add(exp(folder+"BHM_Lombardy.csv"), "Lombardy", "mu", "se", ParameterType.STANDARD_ERROR);
		analysis.add(exp(folder+"BHM_Pedianet.csv"), "Pedianet", "mu", "se", ParameterType.STANDARD_ERROR);
		analysis.add(exp(folder+"BHM_Pharmo.csv"), "Pharmo", "mu", "se", ParameterType.STANDARD_ERROR);
		analysis.write(folder + "BHM_MA.csv");
	}

	private static String exp(String source) {
		String target = source.replace(".", "_Exp.");
		WriteCSVFile out = new WriteCSVFile(target);
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next();
		out.write(header);
		int muCol = header.indexOf("mu");
		int seCol = header.indexOf("se");
		while (iterator.hasNext()){
			List<String> row = iterator.next();
			if (Double.parseDouble(row.get(muCol)) == 0)
				continue;
			row.set(muCol, Double.toString(Math.exp(Double.parseDouble(row.get(muCol)))));
			row.set(seCol, Double.toString(Math.exp(Double.parseDouble(row.get(seCol)))));
			out.write(row);
		}
		out.close();
		return target;
	}

	/**
	 * Use when results are in several files
	 * @param source
	 * @param rrHeader
	 * @param parameterHeader
	 */
	public void add(String source, String database, String rrHeader, String parameterHeader, ParameterType parameterType){
		Set<Signal> limitSet = loadLimitSet();
		dbs.add(database);
		for (Row row : new ReadCSVFileWithHeader(source)){
			String rrString = row.get(rrHeader);
			String parameterString = row.get(parameterHeader);
			if (StringUtilities.isNumber(rrString) && StringUtilities.isNumber(parameterString)){
				Signal signal = new Signal(row.get("ATC"), row.get("EventType"));
				if (limitSet == null || limitSet.contains(signal)){
					Map<String,Data> db2Data = signal2DB2Data.get(signal);
					if (db2Data == null){
						db2Data = new HashMap<String, MetaAnalysis.Data>();
						signal2DB2Data.put(signal, db2Data);
					}
					Data data = new Data();
					data.rr = Double.parseDouble(rrString);
					if (parameterType == ParameterType.STANDARD_ERROR)
					  data.standardError = Double.parseDouble(parameterString);
					else
						data.standardError = org.erasmusmc.jerboa.calculations.MetaAnalysis.ci95up2StandardError(Double.parseDouble(parameterString), data.rr);
					db2Data.put(database, data);
				}
			}
		}
	}

	/**
	 * Use when results are in several files
	 * @param target
	 */
	public void write(String target){
		WriteCSVFile out = new WriteCSVFile(target);
		List<String> header = new ArrayList<String>();
		header.add("ATC");
		header.add("EventType");
		header.add("DB count");
		header.add("p (homogeneity)");
		header.add("RR (fixed)");
		header.add("CI95down (fixed)");
		header.add("CI95up (fixed)");
		header.add("p (fixed)");
		header.add("RR (random)");
		header.add("CI95down (random)");
		header.add("CI95up (random)");
		header.add("p (random)");

		out.write(header);
		for (Signal signal : signal2DB2Data.keySet()){
			Map<String, Data> db2Data = signal2DB2Data.get(signal);
			List<String> row = new ArrayList<String>();
			row.add(signal.atc);
			row.add(signal.eventType);
			List<String> dbsWithData = new ArrayList<String>(db2Data.keySet());
			row.add(Integer.toString(dbsWithData.size()));

			Double[] rrs = new Double[dbsWithData.size()];
			Double[] ses = new Double[dbsWithData.size()];
			for (int i = 0; i < dbsWithData.size(); i++){
				Data data = db2Data.get(dbsWithData.get(i));
				rrs[i] = data.rr;
				ses[i] = data.standardError;
			}

			Result resultFixed = org.erasmusmc.jerboa.calculations.MetaAnalysis.analyse(rrs, ses, Model.FIXED);
			row.add(Double.toString(resultFixed.pForHomogeneity));
			row.add(Double.toString(resultFixed.rr));
			row.add(Double.toString(resultFixed.ci95Down));
			row.add(Double.toString(resultFixed.ci95Up));
			row.add(Double.toString(resultFixed.p));

			Result resultRandom = org.erasmusmc.jerboa.calculations.MetaAnalysis.analyse(rrs, ses, Model.RANDOM);		
			row.add(Double.toString(resultRandom.rr));
			row.add(Double.toString(resultRandom.ci95Down));
			row.add(Double.toString(resultRandom.ci95Up));
			row.add(Double.toString(resultRandom.p));

			out.write(row);
		}
		out.close();
	}

	/**
	 * Use when all results are in a single input file
	 * @param source
	 * @param target
	 * @param rrHeaderStart
	 * @param ci95upStart
	 */
	public void process(String source, String target, String rrHeaderStart, String parameterStart, ParameterType parameterType) {
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next();
		Map<String,Integer> db2rrCol = findColumns(rrHeaderStart, header);
		Map<String,Integer> db2parameterCol = findColumns(parameterStart, header);
		dbs = new ArrayList<String>(db2rrCol.keySet());
		int atcCol = header.indexOf("ATC");
		int eventTypeCol = header.indexOf("EventType");

		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			Signal signal = new Signal(cells.get(atcCol), cells.get(eventTypeCol));
			for (int i = 0; i < dbs.size(); i++){
				String rrString = cells.get(db2rrCol.get(dbs.get(i)));
				String parameterString = cells.get(db2parameterCol.get(dbs.get(i)));
				if (StringUtilities.isNumber(rrString) && StringUtilities.isNumber(parameterString)){
					Map<String,Data> db2Data = signal2DB2Data.get(signal);
					if (db2Data == null){
						db2Data = new HashMap<String, MetaAnalysis.Data>();
						signal2DB2Data.put(signal, db2Data);
					}
					Data data = new Data();
					data.rr = Double.parseDouble(rrString);
					if (parameterType == ParameterType.STANDARD_ERROR)
					  data.standardError = Double.parseDouble(parameterString);
					else
						data.standardError = org.erasmusmc.jerboa.calculations.MetaAnalysis.ci95up2StandardError(Double.parseDouble(parameterString), data.rr);
					db2Data.put(dbs.get(i), data);
				}
			}
		}
		write(target);
	}

	private Set<Signal> loadLimitSet() {
		if (limitSetFile == null)
			return null;
		else {
			Set<Signal> limitSet = new HashSet<Signal>();
			for (Row row : new ReadCSVFileWithHeader(limitSetFile))
				limitSet.add(new Signal(row.get("ATC"),row.get("EventType")));
			return limitSet;
		}
	}

	private Map<String, Integer> findColumns(String headerStart,	List<String> header) {
		Map<String, Integer> db2col = new HashMap<String, Integer>();
		for (int i = 0; i < header.size(); i++){
			String head = header.get(i);
			if (head.startsWith(headerStart+"(")){
				String db = head.substring(headerStart.length()).replace("(", "").replace(")","").trim();
				db2col.put(db, i);
			}
		}

		return db2col;
	}

	private class Data {
		double rr;
		double standardError;
	}

}

