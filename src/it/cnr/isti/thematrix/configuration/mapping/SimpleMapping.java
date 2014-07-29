package it.cnr.isti.thematrix.configuration.mapping;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;

/**
 * Class for the simple mapping descriptor (straight mapping of IAD and LIAD fields), 
 * handles the simpleMapping XML tag.
 * Extends AbstractMapping.  
 *
 * This class interfaces with XML parsing via annotations.
 */
/**
 * @author massimo
 *
 */
public class SimpleMapping extends AbstractMapping
{	
	@XmlElement
	private RecodingTable recodingTable;
	
	@XmlElement
	private String sourceTable;

	@XmlElement
	private String sourceAttribute;
	
	/**
	 * Gets whether a recoding operation is needed for the mapping. 
	 */
	@Override
	public boolean isRecodingNeeded()
	{
		return recodingTable != null;
	}
	
	/**
	 * Gets the name of the LIAD table.
	 */
	@Override
	public String getLIADTable()
	{
		return sourceTable;
	}
	
	/**
	 * Gets the name of the LIAD attribute.
	 */
	@Override
	public String getLIADAttribute()
	{
		return sourceAttribute;
	}
	
	/**
	 * Get the hashmap implementing the direct recoding of values, src to dest .
	 * @return
	 */
	public HashMap<String,String> getRecodingHashMap(){
		return recodingTable.getAsHashMap();
	}

	@Override
	public void doCheck(ConfigurationMappingContext context)
	{

		String parent = "<simpleMapping name=\""+name+"\">";
		
		if ((name == null) || name.trim().isEmpty())
		{
			LogST.errorMappingAttribute(context.file, "name", "<simpleMapping>");
			context.errors ++;
		}
		
		if ((sourceTable == null) || sourceTable.trim().isEmpty())
		{
			LogST.errorMappingElement(context.file, "sourceTable", parent);
			context.errors ++;
		}

		if ((sourceAttribute == null) || sourceAttribute.trim().isEmpty())
		{
			LogST.errorMappingElement(context.file, "sourceAttribute", parent);
			context.errors ++;
		}
		
		if (recodingTable != null) 
		{
			if ((recodingTable.getValues() == null) || (recodingTable.getValues().size() == 0))
			{
				LogST.errorCustom("WARNING: recodingTable in "+parent+" is declared but appears to be empty. File: "+context.file);
				context.warnings ++;
			}
			else
				recodingTable.doCheck(context);
		}
			
	}
}
