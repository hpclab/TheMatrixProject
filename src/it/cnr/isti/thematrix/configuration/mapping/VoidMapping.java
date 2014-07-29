package it.cnr.isti.thematrix.configuration.mapping;

import it.cnr.isti.thematrix.configuration.ConfigSingleton;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;

import javax.xml.bind.annotation.XmlElement;

/**
 * Class for the a void mapping descriptor (cannot map any LIAD information to IAD, leave theIAD field empty) , handles
 * the voidMapping XML tag. Extends AbstractMapping.
 * 
 * This class interfaces with XML parsing via annotations.
 */
public class VoidMapping extends AbstractMapping
{
	// we have no source attributes

	/**
	 * what type is the missing value to map FIXME currently unused, we use String. Is this needed?
	 */
	@XmlElement
	private String destType;

	/**
	 * the default value to use <br>
	 * TODO if not defined, the default missing value for the type should be used, atm we just use the empty string.
	 */
	@XmlElement
	private String defaultValue = "";

	/**
	 * Gets whether a recoding operation is needed for the mapping.
	 */
	@Override
	public boolean isRecodingNeeded() {
		return true;
	}

	/**
	 * Name of the LIAD table must be empty. TODO maybe return null?
	 */
	@Override
	public String getLIADTable() {
		return "";
	}

	/**
	 * Cannot return any name for the LIAD attribute. TODO maybe return null?
	 */
	@Override
	public String getLIADAttribute() {
		return "";
	}

	/**
	 * used to fill up the new column
	 * 
	 * @return
	 */
	public String getDefaultValue() {
		return this.defaultValue;
	}

	@Override
	public void doCheck(ConfigurationMappingContext context) 
	{		
		if ((name == null) || name.trim().isEmpty())
		{
			LogST.errorMappingAttribute(context.file, "name", "<voidMapping>");
			context.errors ++;
		}
		
		// Emanuele, TODO: what to do for the others field?
	}
}
