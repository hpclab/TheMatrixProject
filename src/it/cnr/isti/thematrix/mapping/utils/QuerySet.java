/*
 * Copyright (c) 2010-2014 "HPCLab at ISTI-CNR"
 *
 * This file is part of TheMatrix.
 *
 * TheMatrix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
