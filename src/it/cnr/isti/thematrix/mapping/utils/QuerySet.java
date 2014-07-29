package it.cnr.isti.thematrix.mapping.utils;

import java.util.Collection;

/**
 * Container class for information about a batch of queries, used for the creation of a IAD file. 
 * No actions/changes are performed on the queries by this class. 
 * Note that: some details (e.g. database connection info) have to be explicitly retrieved 
 * from the various configuration singletons 
 * (this will be an issue in supporting multiple-DB interoperation). 
 */
public class QuerySet
{
	private String iadDataset;
	private Collection<QueryDescriptor> queries;

	/**
	 * Returns the IAD dataset name for this set of queries.
	 * 
	 * A <code>String</code>.
	 */
	public String getIADDataset()
	{
		return iadDataset;
	}

	/**
	 * Gets the queries associated with the query set.
	 * 
	 * A <code>Collection<QueryDescriptor></code>.
	 */
	public Collection<QueryDescriptor> getQuery()
	{
		return queries;
	}
	
	/**
	 * Creates a new instance of a <code>QuerySet</code>.
	 * 
	 * @param iadDataset The name of the IAD dataset. 
	 * @param queryDescriptors A <code>Collection<QueryDescriptor></code>
	 */
	public QuerySet(String iadDataset, Collection<QueryDescriptor> queryDescriptors)
	{
		this.iadDataset = iadDataset;
		this.queries = queryDescriptors;
	}

	/**
	 * Adds a query to the query set.
	 * 
	 * @param query The <code>QueryDescriptor</code> to be added. 
	 */
	public void addQuery(QueryDescriptor query)
	{
		this.queries.add(query);
	}
}
