package org.erasmusmc.jerboa;

import it.cnr.isti.thematrix.configuration.LogST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.erasmusmc.collections.Pair;

/**
 * Parses a settings file into an internal representation.
 * @author schuemie
 *
 */
public class SettingsParser {
  
	private static int NONE = 0;
	private static int MODULE = 1;
	private static int INPUTS = 2;
	private static int PARAMETERS = 3;
	private static int METADATA = 4;
	private static int LONGCOMMENT =5;
	
	public static ParsedSettings parse(List<String> settings){
		ParsedSettings parsedSettings = new ParsedSettings();
		int mode = NONE; 
		int savedmode = NONE;
		ModuleSettings moduleSettings = null;
		LogST.logP(0, "Jerboa Parser start, logLevel is "+Integer.toString(LogST.getLogLevel()));
		for (String line : settings){
			String trimLine = line.trim();
			String lcTrimLine = trimLine.toLowerCase();
			LogST.logP(3, "Parsing ||"+trimLine);
			//added part to deal with long comments
			if (mode==LONGCOMMENT) { if (trimLine.endsWith("*/")) mode=savedmode; }
			else if (trimLine.startsWith("/*"))  {
				savedmode=mode;
				mode=LONGCOMMENT;
				}
			//end of added part
			else if (!trimLine.equals("") && ! trimLine.startsWith("//") && ! trimLine.startsWith("#")){
				if (mode == NONE){
					if (trimLine.equals("MetaData"))
						mode = METADATA;
					else {
						moduleSettings = new ModuleSettings();
						moduleSettings.name = trimLine.substring(0,trimLine.indexOf('(')).trim();
						moduleSettings.type = trimLine.substring(trimLine.indexOf('(')+1, trimLine.indexOf(')')).trim();
						moduleSettings.virtual = trimLine.substring(trimLine.indexOf(')')).contains("virtual");
						mode = MODULE;
					}
				} else if (mode == MODULE || mode == INPUTS || mode == PARAMETERS){
					if (lcTrimLine.equals("end")){
						parsedSettings.moduleSettings.add(moduleSettings);
						moduleSettings = null;
						mode = NONE;
					} else if (lcTrimLine.equals("inputs"))
						mode = INPUTS;
					else if (lcTrimLine.equals("parameters"))
						mode = PARAMETERS;
					else if (mode == INPUTS){
						if (!trimLine.contains("="))
							throw new RuntimeException("Error in settings file: " + line);
						String[] parts = trimLine.split("=");
						moduleSettings.inputs.put(parts[0].trim(), parts[1].trim());
					} else if (mode == PARAMETERS){
						if (!trimLine.contains("="))
							throw new RuntimeException("Error in settings file: " + line);
						//System.out.println("Jerboa Parser, PARAMETERS mode, trimline is: |"+trimLine+"|");
						String[] parts = trimLine.split("=");
						if (parts.length == 1)
							moduleSettings.parameters.add(new Pair<String,String>(parts[0].trim(), ""));						
						else
							moduleSettings.parameters.add(new Pair<String,String>(parts[0].trim(), parts[1].trim()));
					}
				} else if (mode == METADATA){
					if (lcTrimLine.equals("end"))
						mode = NONE;
					else {
						if (!trimLine.contains("="))
							throw new RuntimeException("Error in settings file: " + line);
						String[] parts = trimLine.split("=");
						if (parts[0].trim().equals("databaseName"))
							parsedSettings.metaData.databaseNames.add(parts[1].trim());
						if (parts[0].trim().equals("runLabelTemplate"))
							parsedSettings.metaData.runLabelTemplate = parts[1].trim();
					}
				} else {throw new RuntimeException("Unrecoverable error in Jerboa Parser");}
			} // if not comment
		}
		LogST.logP(3,"Jerboa Parser, end, moduleSettings.parameters contains\n"+
				(parsedSettings.moduleSettings==null?"crap":parsedSettings.moduleSettings.toString())+"\n");

		return parsedSettings;
	}
	public static class ParsedSettings {
		public List<ModuleSettings> moduleSettings = new ArrayList<SettingsParser.ModuleSettings>();
		public MetaData metaData = new MetaData();
	}
	
	public static class MetaData {
		public List<String> databaseNames = new ArrayList<String>();
		public String runLabelTemplate;
	}
	
	public static class ModuleSettings {
		public boolean virtual;
		public Map<String, String> inputs = new HashMap<String, String>();
		public List<Pair<String,String>> parameters = new ArrayList<Pair<String,String>>();
		public String name;
		public String type;

		/**
		 *  Custom method to convert to a String, avoids Jerboa Pair.toString. Skips some internal fields.
		 */
		public String toString()
		{
			String result = "ModuleSetting:{"+name+","+type+","+(virtual?"true,":"false,");			
			for (Pair<String,String> s: parameters)
				result += "("+s.object1+"|"+s.object2+")";				
			result += "}";
//			System.out.println(result);
//			LogST.logP(3, result);
			return result;
		}
	}
}
