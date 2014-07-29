package it.cnr.isti.thematrix.configuration.mapping;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ICheckable;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the ROOT element of the XML configuration file for the mapping
 * between the DBMS (LIAD) records and the IAD schema
 */
@XmlRootElement(name = "iadMapping")
public class IadMapping implements ICheckable
{
	@XmlElement
	private Collection<Dataset> dataset;
	
	/**
	 * Retrieves a dataset mapping given its name. This class interfaces with XML parsing via annotations.
	 * 
	 * @param datasetName the symbolic name of the dataset (in the mapping file ?)
	 * @return a Dataset object (copy of the one requested).
	 */
	public Dataset getDatasetByName(String datasetName)
	{
		Dataset result = null;
		
		for (Dataset d : dataset)
		{
			if (d.getName().equals(datasetName))
			{
				result = d;
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * Retrieves all dataset names known from the mapping configuration; 
	 * the order is taken from the <code>private Collection<Dataset> dataset</code>
	 * 
	 * @return a newly created String Collection with the names
	 */
	public Collection<String> getDatasetNames()
	{
		Collection<String> result = new ArrayList<String>();
	
		for (Dataset data: dataset)
		{
			result.add(data.getName());
		}
		
		return result;
	}

	@Override
	public void doCheck(ConfigurationMappingContext context) 
	{
		String parent = "<iadMapping>";
		
		if ((dataset == null) || dataset.isEmpty())
		{
			LogST.errorMappingElement(context.file, "dataset", parent);
			context.errors ++;
		}
		else
		{
			for (Dataset d: dataset)
				d.doCheck(context);
		}
	}
}
