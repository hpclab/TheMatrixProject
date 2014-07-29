package it.cnr.isti.thematrix.mapping.utils;

/**
 * This class holds query-related information.
 * 
 */
public class QueryDescriptor 
{
	/**
	 * The SQL query expressed as a <code>String</code>.
	 */
	public String query;

	/**
	 * A <code>String</code> holding the name(s) of the tables involved in the query.
	 */
	public String tableName;

	/**
	 * Creates a new instance of <code>QueryDescriptor</code>.
	 * 
	 * @param query The SQL query expressed as a <code>String</code>.
	 * @param tableName The name of the table.
	 */
	public QueryDescriptor(String query, String tableName) 
	{
		this.query = query;
		this.tableName = tableName;
	}
}
