package it.cnr.isti.thematrix.configuration.setting;

import javax.xml.bind.annotation.XmlElement;

public class Path
{
	@XmlElement
	private String results;

	/**
	 * Path to disk location where iad .csv files are stored.
	 */
	@XmlElement
	private String iad;

	@XmlElement
	private String mapping;

	@XmlElement
	private String scripts;

	/**
	 * Path to disk location where SQL queries are stored.
	 */
	@XmlElement
	private String queries;

	@XmlElement
	private String lookupTable;

	public String getQueries()
	{
		return queries;
	}

	public String getLookup()
	{
		return lookupTable;
	}

	public String getIad()
	{
		return iad;
	}

	public String getResult()
	{
		return queries;
	}

	public String getMapping()
	{
		return mapping;
	}

	public String getScripts()
	{
		return scripts;
	}

}
