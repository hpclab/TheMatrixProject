package it.cnr.isti.thematrix.configuration.mapping;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Class for a mapping descriptor of a lookup mapping, handles the lookupMapping XML tag.
 * Extends AbstractMapping.  
 * TODO: document better this class
 * This class interfaces with XML parsing via annotations.
 */
public class LookupMapping extends AbstractMapping
{
	@XmlAttribute
	private Boolean inverseLookup;

	@XmlElement
	private String lookupTable;

	@XmlElement
	private String lookupAttribute;

	@XmlElement
	private Mapping mapping;

	/**
	 * Gets whether the inverse lookup operation is permitted.
	 */
	public boolean getInverseLookup()
	{
		return inverseLookup;
	}

	/**
	 */
	public String getLookupTable()
	{
		return lookupTable;
	}
	
	/**
	 */
	public String getLookupAttribute()
	{
		return lookupAttribute;
	}

	/**
	 * Gets the name of the LIAD attribute.
	 */
	@Override
	public String getLIADAttribute()
	{
		return mapping.getSourceAttribute();
	}

	/**
	 * Gets the name of the LIAD table.
	 */
	@Override
	public String getLIADTable()
	{
		return mapping.getSourceTable();
	}

	@Override
	public void doCheck(ConfigurationMappingContext context) 
	{
		String parent = "<lookupMapping name=\""+name+"\">";
		
		if ((name == null) || name.trim().isEmpty())
		{
			LogST.errorMappingAttribute(context.file, "name", "<lookupMapping>");
			context.errors ++;
		}
		
		if (inverseLookup == null)
		{
			LogST.errorMappingElement(context.file, "inverseLookup", parent);
			context.errors ++;
		}
		
		if ((lookupTable == null) || lookupTable.trim().isEmpty())
		{
			LogST.errorMappingElement(context.file, "lookupTable", parent);
			context.errors ++;
		}
		
		if ((lookupAttribute == null) || lookupAttribute.trim().isEmpty())
		{
			LogST.errorMappingElement(context.file, "lookupAttribute", parent);
			context.errors ++;
		}

		if (mapping == null)
		{
			LogST.errorMappingElement(context.file, "mapping", parent);
			context.errors ++;
		}
		else
			mapping.doCheck(context);	
	}
}
