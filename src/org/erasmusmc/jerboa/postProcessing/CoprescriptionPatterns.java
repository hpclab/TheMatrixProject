package org.erasmusmc.jerboa.postProcessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.erasmusmc.jerboa.ATC2Name;
import org.erasmusmc.jerboa.modules.RelativeRiskModule;
import org.erasmusmc.utilities.CountingSetLong;
import org.erasmusmc.utilities.ReadCSVFileWithHeader;
import org.erasmusmc.utilities.Row;
import org.erasmusmc.utilities.WriteCSVFile;

public class CoprescriptionPatterns {

	public static String folder = "X:/";
	public static String sourceFile = folder + "AggregatebyATCcombination.txt";
	public static String targetFile = folder + "Coprescriptions.txt";
	
	
	public static void main(String[] args) {
		
		CountingSetLong<String> singleATCs = new CountingSetLong<String>();
		CountingSetLong<String> twoATCs = new CountingSetLong<String>();
		
		for (Row row : new ReadCSVFileWithHeader(sourceFile)){
			String atcString = row.get("ATC");
			if (atcString.length() != 0){
				long days = Long.parseLong(row.get("Days"));
				List<String> atcs = new ArrayList<String>();
				for (String atc : atcString.split("\\+"))
					if (atc.length() == 7)
						atcs.add(atc);
			  Collections.sort(atcs);
				for (String atc : atcs)
					singleATCs.add(atc, days);
				for (int i = 0; i < atcs.size() -1; i++)
					for (int j = i+1; j < atcs.size(); j++)
						twoATCs.add(atcs.get(i)+"\t"+atcs.get(j), days);
			}
		}
		WriteCSVFile out = new WriteCSVFile(targetFile);
		List<String> header = new ArrayList<String>();
		header.add("ATC1");
		header.add("Name1");
	  header.add("ATC2");
	  header.add("ATC2");
	  header.add("Days");
	  out.write(header);
	  ATC2Name atc2Name = new ATC2Name(RelativeRiskModule.class.getResourceAsStream("ATC_2008.xml"));
	  for (String atc : singleATCs){
	  	List<String> cells = new ArrayList<String>();
	  	cells.add(atc);
	  	cells.add(atc2Name.getName(atc));
	  	cells.add("");
	  	cells.add("");
	  	cells.add(Long.toString(singleATCs.getCount(atc)));
	  	out.write(cells);
	  }
	  for (String atcs : twoATCs){
	  	List<String> cells = new ArrayList<String>();
	  	String[] atc = atcs.split("\t");
	  	cells.add(atc[0]);
	  	cells.add(atc2Name.getName(atc[0]));
	  	cells.add(atc[1]);
	  	cells.add(atc2Name.getName(atc[1]));
	  	cells.add(Long.toString(twoATCs.getCount(atcs)));
	  	out.write(cells);
	  	
	  	cells = new ArrayList<String>();
	  	cells.add(atc[1]);
	  	cells.add(atc2Name.getName(atc[1]));
	  	cells.add(atc[0]);
	  	cells.add(atc2Name.getName(atc[0]));
	  	cells.add(Long.toString(twoATCs.getCount(atcs)));
	  	out.write(cells);
	  }
		out.close();
	}

}
