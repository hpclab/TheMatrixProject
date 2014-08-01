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
package it.cnr.isti.thematrix.test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import it.cnr.isti.thematrix.mapping.creator.QueryCreator;
import it.cnr.isti.thematrix.mapping.utils.QueryDescriptor;

/**
 * Tests the query creation feature.
 */
public class TestQueryCreator 
{	
	public static void main(String[] args) throws JAXBException, IOException, NoSuchAlgorithmException
	{
		Collection<String> attributeNames = new ArrayList<String>(3);
		attributeNames.add("BIRTH_LOCATION_CONCEPT_ID");
		attributeNames.add("DATE_OF_BIRTH");
		attributeNames.add("ENDDATE");
		
		Collection<QueryDescriptor> queries = QueryCreator.computeQuerySet("PERSON", attributeNames);
		
		for (QueryDescriptor queryDesc : queries)
		{
			System.out.println("table: " + queryDesc.tableName);
			System.out.println("\tquery: " + queryDesc.query);
			System.out.println();
		}
	}
}
