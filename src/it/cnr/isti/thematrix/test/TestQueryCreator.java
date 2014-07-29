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
