package it.cnr.isti.thematrix.configuration.mapping;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ICheckable;

import javax.xml.bind.annotation.XmlElement;

/**
 * Class for a mapping descriptor, handles the mapping XML tag.
 * TODO: document better this class
 * This class interfaces with XML parsing via annotations.
 */
public class Mapping implements ICheckable
{
	@XmlElement
	private String sourceTable;

	@XmlElement
	private String sourceAttribute;
	
	/**
	 */
	public String getSourceTable()
	{
		return sourceTable;
	}
	
	/**
	 */
	public String getSourceAttribute()
	{
		return sourceAttribute;
	}

	@Override
	public void doCheck(ConfigurationMappingContext context) 
	{
		String parent = "<mapping>";
		
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
	}
}
