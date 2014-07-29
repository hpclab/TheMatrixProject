package it.cnr.isti.thematrix.configuration.mapping;

import it.cnr.isti.thematrix.configuration.mapping.reporting.ICheckable;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Class for a mapping descriptor of a IAD attribute, handles the AbstractMapping XML tag contents. 
 * This class interfaces with XML parsing via annotations.
 */
public abstract class AbstractMapping implements ICheckable
{
	@XmlAttribute
	protected String name;

	@XmlAttribute
	protected boolean isSensible;
	
	/**
	 * Gets the name of the IAD attribute.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Gets the privacy value of the IAD attribute.
	 */
	public boolean isSensible()
	{
		return isSensible;
	}

	/**
	 * Check if recoding is needed to map this attribute.
	 */
	public boolean isRecodingNeeded() 
	{
		return false;
	}
	
	/**
	 * Gets the name of the LIAD table.
	 */
	public abstract String getLIADTable();
	
	/**
	 * Gets the name of the LIAD attribute.
	 */
	public abstract String getLIADAttribute();
}
