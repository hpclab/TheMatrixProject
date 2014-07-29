package it.cnr.isti.thematrix.mapping.utils;

import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

/**
 * This class is a singleton.
 * @author carlini
 */
public class ProductCodeStatics 
{	
	
	private static Map<String,RecordBean> map = null;
	

	/**
	 * Resolves the ATC of the given product code;
	 * Returns empty string if the value is missing or null.
	 * Handles the case of not existing values writing in Log
	 * 
	 * @param code
	 * @return
	 */
	public static String getAtc(String product_code)
	{
		if (map == null)
			loadFromCSV();

		RecordBean bean = map.get(fixCode(product_code));
		
		if ((bean == null) || (bean.getAtc() == null)) {
			LogST.logP(1, "WARNING: getAtc unrecognized value \""+product_code+"\"");
			return "";
		}

		return bean.getAtc().trim();
	}
	
	/**
	 * Resolves the Duration of the given product code;
	 * Returns NaN if the value is missing or null.
	 * 
	 * TODO: Handle the case of not existing values (write in Log?)
	 * @param code
	 * @return
	 */
	public static Float getDuration(String product_code)
	{
		if (map == null)
			loadFromCSV();
		
		RecordBean bean = map.get(fixCode(product_code));
		
		if ((bean == null) || (bean.getDuration_of_box() == null)) {
			LogST.logP(1, "getDuration unrecognized value \""+product_code+"\"");
			return null;
		}
		
		return bean.getDuration_of_box().floatValue();	
	}
	
	
	private static String fixCode(String product_code)
	{
		String result = product_code.trim();
		
		if (result.length() == 9)
			return result;
		else if (result.length() == 10)
			return result.substring(1); // remove the first char
		
		LogST.logP(1, "WARNING: PRODUCT_CODE lenght is not supported: "+product_code+" length: "+product_code.length());
		
		return result;
	}
	
	/**
	 * Performs the actual load of the file from the disk
	 * @return
	 */
	private static void loadFromCSV()
	{		
		String CSVpath = Dynamic.getLookupsPath()+"PRODUCT_CODE_AIFA.csv";
	
		ICsvBeanReader beanReader = null;
		String[] header = null;
		map = new HashMap<String,RecordBean>();

		  
        try 
        {
            beanReader = new CsvBeanReader(new FileReader(CSVpath), CsvPreference.STANDARD_PREFERENCE);
			header = beanReader.getHeader(true);
		        
            RecordBean record;
          
            
			while((record = beanReader.read(RecordBean.class, header, RecordBean.processors)) != null )
				    map.put(record.getPRODUCT_CODE(), record);
			
			
        }
        catch (IOException e) {
        	LogST.logP(3, "Error when reading/opening product code mapping file - "+e.getMessage());
        	throw new Error ("Cant open "+CSVpath);
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
        	LogST.logP(2, "WARNING: Product file seems empty, check your configuration");
	}
	

}
