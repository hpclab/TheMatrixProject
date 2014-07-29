package it.cnr.isti.thematrix.mapping.utils;

import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.mapping.utils.ProcOutpatEnum.Type_Outpat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.TypeConstraintException;

import org.supercsv.exception.SuperCsvConstraintViolationException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
/**
 * Class to load proc_Outpat.csv into HashMap.
 * @author marco d
 */
public class ProcOutpatStatics {

	private static Map<String,Type_Outpat> map = null;
	private static Map<String,Type_Outpat> map_without_dots = null;
	
	/**
	 * Resolves the TYPE_OUTPAT of the given PROC_CODE;
	 * Returns empty string if the value is missing or null.
	 * Handles the case of not existing values writing in Log.
	 * 
	 * @param proc_cod a valid PROC_CODE value
	 * @return the Type_outpat value associated to the parameter
	 */
	public static String getTYPE_OUTPAT(String proc_cod)
	{
		if (map == null)
			loadFromCSV();
	
		Type_Outpat tOutpat = map.get(proc_cod.trim());

		if (tOutpat == null)
			tOutpat = map_without_dots.get(proc_cod.trim());
		
		
		if (tOutpat == null) 
		{
				LogST.logP(1, "getTYPE_OUTPAT unrecognized value \""+proc_cod+"\"");
				return "";
		}
		
		return tOutpat.name();
	}
		
	/**
	 * Performs the actual load of the file from the disk
	 * 
	 */
	private static void loadFromCSV()
	{		
		String CSVpath = Dynamic.getLookupsPath()+"proc_OUTPAT.csv";
		ICsvBeanReader beanReader = null;
		String[] header = null;
		
		// init the hashmap
		map = new HashMap<String,Type_Outpat>();
		map_without_dots = new HashMap<String, ProcOutpatEnum.Type_Outpat>();
		
		try 
	    {
			beanReader = new CsvBeanReader(new FileReader(CSVpath), CsvPreference.STANDARD_PREFERENCE);
			header = beanReader.getHeader(true);
			       
			ProcOutpatBean record;
			while((record = beanReader.read(ProcOutpatBean.class, header, ProcOutpatBean.processors)) != null )
			{
				if (ProcOutpatEnum.isInEnum(record.getTYPE_OUTPAT()))
				{
					map.put(record.getPROC_COD(), ProcOutpatEnum.getType_Outpat(record.getTYPE_OUTPAT()));
					map_without_dots.put(record.getPROC_COD().replaceAll("\\.", ""), ProcOutpatEnum.getType_Outpat(record.getTYPE_OUTPAT()));
				}
				else{
					LogST.logP(0, "ProcOutpatStatics.loadFromCSV() : record : "+record+" has an invalid TYPE_OUTPAT");
					//TODO: define a proper Exception for this case
					throw new TypeConstraintException("ProcOutpatStatics.loadFromCSV(): unknown value of TYPE_OUTPAT in "+CSVpath);
				}
			}
        }
        catch (IOException e) {
        	LogST.logP(3, "ProcOutpatStatics.loadFromCSV(): Error when reading/opening product code mapping file - "+e.getMessage());
        	throw new Error("Cant open "+CSVpath);
        }
        catch (SuperCsvConstraintViolationException e1){
        	LogST.logP(0, "ProcOutpatStatics.loadFromCSV() Error null value encountered in "+CSVpath);
        	throw new Error(e1.getMessage());
        }
        finally {
        	if(beanReader != null) {
        		try {
        			beanReader.close();
				} catch (IOException e){e.printStackTrace();}
        	}
        }
	        
        LogST.logP(1, "Read "+map.size()+" from the product file: "+CSVpath);
        if (map.size() == 0)
        	LogST.logP(2, "ProcOutpatStatics.loadFromCSV() WARNING: Product file seems empty, check your configuration");
	}		
}
