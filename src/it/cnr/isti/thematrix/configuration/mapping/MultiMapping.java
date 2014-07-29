package it.cnr.isti.thematrix.configuration.mapping;

import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;

/**
 * Class for a mapping descriptor of a multiple mapping, handles the multiMapping XML tag.
 * Extends AbstractMapping.  
 * TODO: document better this class
 * This class interfaces with XML parsing via annotations.
 */
public class MultiMapping extends AbstractMapping
{
	@XmlElement
	private String function;

	@XmlElement
	private Collection<Mapping> mapping;

	@Override
	public String getLIADAttribute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLIADTable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doCheck(ConfigurationMappingContext context)
	{
		// TODO Auto-generated method stub	
	}
}
