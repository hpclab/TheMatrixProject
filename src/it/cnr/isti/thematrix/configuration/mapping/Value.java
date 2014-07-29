package it.cnr.isti.thematrix.configuration.mapping;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ICheckable;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Elementary class for an XML ground pair, named after the entries in a recoding table. Protected fields as to allow
 * other classes in the package to access them.
 * 
 * @author massimo
 */
public class Value implements ICheckable
{
	@XmlAttribute
	protected String src;

	@XmlAttribute
	protected String dest;

	@Override
	public void doCheck(ConfigurationMappingContext context) 
	{
		String parent = "<value>";
		
		if ((src == null) || src.trim().isEmpty())
		{
			LogST.errorMappingAttribute(context.file, "src", parent);
			context.errors ++;
		}
			
		if ((dest == null) || dest.trim().isEmpty())
		{
			LogST.errorMappingAttribute(context.file, "dest", parent);
			context.errors ++;
		}
	}

}
