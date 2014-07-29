package org.erasmusmc.jerboa.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.ATC2NewATC;
import org.erasmusmc.jerboa.dataClasses.Prescription.ATCCode;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Module for mapping old ATC codes to new ATC codes. Takes any Jerboa file type as input that has an ATC column.
 * @author schuemie
 *
 */
public class ATCRecodingModule extends JerboaModule {

	public JerboaModule input;
	
	/**
	 * Contains absolute path to the ATC mapping file. The file should be in CSV format, with at least these columns:
	 * - "Previous ATC code"
	 * - "New ATC code"
	 * Optionally, the file can also specify:
	 * - "Year changed"
	 * 
	 * If the mapping file is not specified, the default table will be used from http://www.whocc.no/atc_ddd_alterations__cumulative_/atc_alterations/.
	 */
	public String mappingFile;
	
	/**
	 * Show the frequency of mappings per ATC code in the console.<BR>
	 * default = false
	 */
	public boolean showMappingStatisticsInConsole = false;
	
	private ATC2NewATC atc2NewATC;
	private int atcCol;
	private int remapCount;
	private int oldCount;
	private CountingSet<String> mappingCounts;
	private static final long serialVersionUID = 4814743649855580702L;
	
	public static void main(String[] args){
		ATCRecodingModule module = new ATCRecodingModule();
		String folder = "/home/data/IPCI/Study5/";
		module.process(folder+"AggregatebyATC.txt", folder+"AggregatebyATCRemapped.txt");
	}

	@Override
	protected void runModule(String outputFilename) {
		process(input.getResultFilename(), outputFilename);
	}

	public void process(String source, String target) {
		loadATCTable();
		remapCount = 0;
		oldCount = 0;
		mappingCounts = new CountingSet<String>();
		
		WriteCSVFile out = new WriteCSVFile(target);
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		List<String> header = iterator.next();
		atcCol = StringUtilities.caseInsensitiveIndexOf("ATC", header);
		out.write(header);
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			cells.set(atcCol, processATC(cells.get(atcCol)));
			out.write(cells);
		}		
		
		out.close();
		System.out.println("Processed " + oldCount + " ATC codes, " + remapCount + " were mapped to new ATC codes.");
		if (showMappingStatisticsInConsole)
		  mappingCounts.printCounts();
		
		//Dereference data objects:
		atc2NewATC = null;
		mappingCounts = null;
	}

	private String processATC(String atcString) {
	  List<ATCCode> atcCodes = new ArrayList<ATCCode>();	
  	for (String atcCode : atcString.split("\\+"))
  		atcCodes.add(new ATCCode(atcCode));
  	for (ATCCode atcCode : atcCodes){
  		oldCount++;
  		String newATC = atc2NewATC.getNewATC(atcCode.atc);
  		if (newATC != null){
  			mappingCounts.add(atcCode.atc + " -> " + newATC);
  			atcCode.atc = newATC;
  			remapCount++;
  		}
  	}
		return StringUtilities.join(atcCodes, "+");
	}

	private void loadATCTable() {
		if (mappingFile == null || mappingFile.length() == 0)
			atc2NewATC = new ATC2NewATC(ATCRecodingModule.class.getResourceAsStream("ATCremapping.csv"));
		else
			atc2NewATC = new ATC2NewATC(mappingFile);		
	}
}
