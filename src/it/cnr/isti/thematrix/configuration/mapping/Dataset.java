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
package it.cnr.isti.thematrix.configuration.mapping;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ConfigurationMappingContext;
import it.cnr.isti.thematrix.configuration.mapping.reporting.ICheckable;
import it.cnr.isti.thematrix.exception.SyntaxErrorInMappingException;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * Class for the mapping schema of a IAD dataset, handles the dataset XML tag. This class interfaces with XML parsing
 * via annotations.
 * 
 * FIXME the whole class tries to give the impression of a Hash table for mappings, while the Collection is actually
 * more useful (VERY bad choice in the original translation algorithm in ValueRemapper!)
 */
public class Dataset implements ICheckable
{
	@XmlAttribute
	private String name;

	@XmlElement
	private String joinName;

	@XmlElement
	private String joinClause;

	@XmlElements({@XmlElement(name = "voidMapping", type = VoidMapping.class),
			@XmlElement(name = "simpleMapping", type = SimpleMapping.class),
			@XmlElement(name = "complexMapping", type = ComplexMapping.class),
			@XmlElement(name = "multiMapping", type = MultiMapping.class),
			@XmlElement(name = "lookupMapping", type = LookupMapping.class)})
	private Collection<AbstractMapping> abstractMapping;

	/**
	 * Gets the name of the IAD dataset.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the "join" clause used to express the mapping. It can be <code>null</code>.
	 * 
	 * TODO: NO IT CANNOT BE NULL! WREAKS HAVOC IN THE MAPPER ADD CHECK!! -- Max
	 */
	public String getJoinClause() {
		return joinClause;
	}

	/**
	 * Gets the name of the file used to store temporary data extracted from the DBMS. It can be <code>null</code>.
	 * 
	 * TODO: NO IT CANNOT BE NULL! WREAKS HAVOC IN THE MAPPER ADD CHECK!! -- Max
	 */
	public String getJoinName() {
		return joinName;
	}

	/**
	 * Gets the actual mapping descriptors for each attribute of the IAD dataset.
	 */
	public Collection<AbstractMapping> getMappings() {
		return abstractMapping;
	}

	/**
	 * Gets the mapping associated with a specific LIAD attribute.
	 * 
	 * @param attributeName
	 *            the LIAD attribute whose mapping descriptor has to be searched.
	 * 
	 * @return An <code>AbstractMapping</code>.
	 */
	public AbstractMapping getMappingByLIADAttribute(String attributeName) {
		AbstractMapping result = null;

		for (AbstractMapping mapping : abstractMapping) {
			if (mapping.getLIADAttribute().equals(attributeName)) {
				result = mapping;
				break;
			}
		}
		return result;
	}

	/**
	 * Gets the mapping associated with a specific IAD attribute.
	 * 
	 * @param attributeName
	 *            the IAD attribute whose mapping descriptor has to be searched.
	 * 
	 * @return An <code>AbstractMapping</code>.
	 */
	public AbstractMapping getMappingByIADAttribute(String attributeName) {
		AbstractMapping result = null;

		for (AbstractMapping mapping : abstractMapping) {
			if (mapping.getName().equals(attributeName)) {
				result = mapping;
				break;
			}
		}
		return result;
	}

	/**
	 * Gets the LIAD column names belonging to a LIAD table used to map the specified set of IAD attributes. Will only
	 * return the LIAD columns which are actually needed to generate the SQL queries, <b>not</b> taking into account any
	 * VoidMapping.
	 * 
	 * @param LIADTable
	 *            the LIAD table
	 * @param iadAttributes
	 *            The set of IAD attributes
	 * 
	 * @return A <code>Collection<String></code>.
	 */
	public Collection<String> getLIADAttributes(String LIADTable, Collection<String> iadAttributes) {
		Collection<String> result = new ArrayList<String>();

		for (AbstractMapping mapping : getMappings()) {
			if (!(mapping instanceof VoidMapping)) { // only for real mappings
				if (iadAttributes.contains(mapping.getName()) && mapping.getLIADTable().equals(LIADTable)) {
					result.add(mapping.getLIADAttribute());
				}
			}
		}
		return result;
	}

	/**
	 * Gets within this IAD dataset the IAD attribute names that are completely missing in mapped LIAD tables, and are
	 * substituted with voidMappings.
	 * 
	 * FIXME note the inconsistency with previous method, here IAD attributes are returned.
	 * 
	 * @param iadAttributes
	 *            The set of IAD attributes
	 * 
	 * @return A <code>Collection<String></code>.
	 */
	public Collection<String> getVoidAttributes() {
		Collection<String> result = new ArrayList<String>();

		for (AbstractMapping mapping : getMappings()) {
			if (mapping instanceof VoidMapping) {
				result.add(mapping.getName());
			}
		}
		return result;
	}

	/**
	 * Gets the set of LIAD tables used to map this dataset.
	 * 
	 * @return A <code>Collection<String></code>.
	 */
	public Collection<String> getAllLIADTables() {
		Collection<String> result = new ArrayList<String>();

		for (AbstractMapping mapping : getMappings()) {
			String LIADTableName = mapping.getLIADTable();

			if (!result.contains(LIADTableName)) result.add(LIADTableName);
		}

		return result;
	}

	/**
	 * Gets the subset of LIAD tables used to map a subset of this dataset.
	 * 
	 * @param iadAttributeNames
	 *            UNUSED.
	 * 
	 * @return A <code>Collection<String></code>.
	 */
	// FIXME: check why the param 'iadAttributeNames' is not used!
	public Collection<String> getLIADTables(Collection<String> iadAttributeNames) {
		Collection<String> result = getAllLIADTables();

		return result;
	}

	/**
	 * Translate each LIAD attribute name into the equivalent IAD name, in the order of the LIAD list argument.
	 * 
	 * 
	 * Gets the IAD attributes corresponding to a set of LIAD attributes.<br>
	 * TODO refactor search code, refactor and change method name<br>
	 * TODO this will ONLY map IAD fields which HAVE an exactly corresponding LIAD field
	 * 
	 * FIXME this is bad code and it is already causing problems with the mappings
	 * 
	 * @param liadAttributeNames
	 *            A <code>Collection<String></code> of LIAD attribute names.
	 * 
	 * @return A <code>Collection<String></code> of IAD attribute names.
	 * 
	 * @throws SyntaxErrorInMappingException
	 */
	public Collection<String> getAllIADAttributesOrdered(Collection<String> liadAttributeNames)
			throws SyntaxErrorInMappingException {
		Collection<String> result = new ArrayList<String>();

		for (String liadAttributeName : liadAttributeNames) {
			/**
			 * THIS. IS. OBFUSCATED. CODE. Who wrote this?
			 * 
			 * TODO rewrite this search over the mappings of this dataset. It should be a separate method.
			 */
			for (AbstractMapping mapping : getMappings()) { // for all mappings
				String iadName = mapping.getName();

				if (mapping.getLIADAttribute().equals(liadAttributeName)) { // we got the right one?
					if (!result.contains(iadName)) {
						result.add(iadName); // translate the name
					}
					else {
						/* Sanity check that should be done just once on the configuration maybe? */
						throw new SyntaxErrorInMappingException(
								"Error: duplicate IAD mapping for field "+iadName+". Check mapping file correctness and consistency.");
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the whole set of IAD attributes expressed by this dataset.
	 * 
	 * TODO: sanity checks here on IAD field names should be moved to program start.
	 * 
	 * @return A <code>Collection<String></code> of IAD attribute names.
	 * 
	 * @throws SyntaxErrorInMappingException
	 */
	public Collection<String> getAllIADAttributes() throws SyntaxErrorInMappingException {
		Collection<String> result = new ArrayList<String>();

		for (AbstractMapping mapping : getMappings()) {
			String iadName = mapping.getName();

			if (result.contains(iadName)) {
				throw new SyntaxErrorInMappingException("Error: duplicate IAD mapping for field "+iadName+". Check mapping file correctness/consistency.");
			}
			else {
				result.add(iadName);
			}
		}

		return result;
	}

	/**
	 * Gets IAD attribute corresponding to a LIAD attribute.
	 * 
	 * @param attributeName
	 *            The LIAD attribute
	 * 
	 * @return The corresponding IAD attribute as a <code>String</code>.
	 */
	public String getIADAttributeByLIADAttribute(String attributeName) {
		String result = null;

		for (AbstractMapping mapping : abstractMapping) {
			if (mapping.getLIADAttribute().equals(attributeName)) {
				result = mapping.getName();
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if the specified IAD attribute needs recoding to be performed.
	 * 
	 * @param attributeName
	 *            The IAD attribute to be checked.
	 * 
	 * @return <code>True</code> if recoding is needed, <code>false</code> otherwise.
	 */
	public boolean isRecodingNeeded(String attributeName) {
		return this.getMappingByLIADAttribute(attributeName).isRecodingNeeded();
	}

	@Override
	public void doCheck(ConfigurationMappingContext context) 
	{
		String parent = "<dataset name=\""+name+"\">";
		
		if ((name == null) || name.trim().isEmpty())
		{
			LogST.errorMappingAttribute(context.file, "name", "<dataset>");
			context.errors ++;
		}

		if ((joinName == null) || joinName.trim().isEmpty())
		{
			LogST.errorMappingElement(context.file, "joinName", parent);
			context.errors ++;
		}

		if ((joinClause == null) || joinClause.trim().isEmpty())
		{
			LogST.errorMappingElement(context.file, "joinClause", parent);
			context.errors ++;
		}
		
		if ((abstractMapping == null) || abstractMapping.isEmpty())
		{
			LogST.errorCustom("ERROR: No mapping defined for "+parent+". File: "+context.file);
			context.errors ++;
		}
		else
		{
			for (AbstractMapping element: abstractMapping)
				element.doCheck(context);
		}	
	}
}