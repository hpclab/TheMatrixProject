package it.cnr.isti.thematrix.mapping.utils;

import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.mapping.utils.WardEnum.TypeWard;

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
 * Class to load ward.csv into HashMap.
 * @author marco d
 */
public class WardStatics {
		
	private static Map<String,WardEnum.TypeWard> map = null;
		
	/**
	 * Resolves the TYPE_WARD of the given WARD;
	 * Returns empty string if the value is missing or null.
	 * Handles the case of not existing values by writing in Logs
	 * 
	 * @param ward
	 * @return
	 * @throws Exception 
	 */
	public static String getTYPE_WARD(String ward)
	{
		if (map == null)
			loadFromCSV();
		
		TypeWard tWard = map.get(ward);
		
		if (tWard == null) {
			LogST.logP(1, "getTYPE_WARD unrecognized value \""+ward+"\"");
			return "";
		}
			
		return tWard.name();
	}
			
	/**
	 * Performs the actual load of the file from the disk
	 * 
	 * FIXME this code is the same in several classes. There should be a single function doing the csv load, with
	 * parameters such as the file name, the Bean to use and the class to create.
	 * 
	 * @return
	 * @throws Exception
	 */
	private static void loadFromCSV()
	{		
		String CSVpath = Dynamic.getLookupsPath()+"ward.csv";
		ICsvBeanReader beanReader = null;
		String[] header = null;
		map = new HashMap<String,WardEnum.TypeWard>();
			  
        try 
        {
            beanReader = new CsvBeanReader(new FileReader(CSVpath), CsvPreference.STANDARD_PREFERENCE);
			header = beanReader.getHeader(true);
			        
			WardBean record;
	          
			while((record = beanReader.read(WardBean.class, header, WardBean.processors)) != null ){
				if (WardEnum.isInEnum(record.getTYPE_WARD()))
						map.put(record.getWARD(), WardEnum.getTypeWard(record.getTYPE_WARD()));
				else{
					LogST.logP(0, "WardStatics.loadFromCSV() : record : "+record+" has an invalid TYPE_WARD");
					//TODO: define a proper Exception for this case
					throw new TypeConstraintException("WardStatics.loadFromCSV(): unknown value of TYPE_WARD in "+CSVpath);
				}
			}	
        }
        catch (IOException e) {
        	LogST.logP(3, "WardStatics.loadFromCSV(): Error when reading/opening product code mapping file - "+e.getMessage());
        	throw new Error ("Cant open "+CSVpath);
        }
        catch (SuperCsvConstraintViolationException e1){
        	LogST.logP(0, "WardStatics.loadFromCSV() Error null value encountered in "+CSVpath);
        	throw new Error (e1.getMessage());
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
        	LogST.logP(2, "WardStatics.loadFromCSV() WARNING: Product file seems empty, check your configuration");
	}
}
