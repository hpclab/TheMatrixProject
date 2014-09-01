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
package it.cnr.isti.thematrix.mapping.creator;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.configuration.MappingSingleton;
import it.cnr.isti.thematrix.configuration.mapping.AbstractMapping;
import it.cnr.isti.thematrix.configuration.mapping.Dataset;
import it.cnr.isti.thematrix.configuration.mapping.SimpleMapping;
import it.cnr.isti.thematrix.configuration.mapping.VoidMapping;
import it.cnr.isti.thematrix.exception.SyntaxErrorInMappingException;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

/**
 * THis class <i>should</i> translate the header names and values retrieved from DB (LIAD) into values suitable for the
 * CSV file cache (IAD). It is expected to use all the extensions of <code>AbstractMapping</code>
 * 
 * @author iacopo, giacomo, massimo
 * 
 */
public class ValueRemapper {

	/**
	 * Performs the mapping of the values in the body section. <br>
	 * Access the given iadCsv as if it was a given IAD dataset; select the mapping definitions for that IAD dataset,
	 * extract the attributes that need value mapping, for each one call the proper recoding method on the column
	 * buffer. <br>
	 * Currently supports VoidMapping and SimpleMapping (with and without recoding table). <br>
	 * Note that the method needs access to both the unmapped header and the mapped one, even after the creation of the
	 * header (see applyMappingToCsvHeader() ). In order to save the CSV with the correctly mapped header, it is
	 * necessary to hack the CSVFile class.
	 * 
	 * TODO implement all mappings (beside Simple) and return warnings and exceptions for mapping failures.<br>
	 * FIXME this needs reverse engineering and refactoring: the remapping cannot be controlled from the LIAD side!
	 * 
	 * @param datasetName
	 *            The name of the dataset to be mapped.
	 * @param iadCsv
	 *            The <code>CSVFile</code> used to store the data. It assumes the CSV file is already loaded.
	 * 
	 * @throws JAXBException
	 */
	public static void applyMappingToDataValues(CSVFile iadCsv, String datasetName) throws JAXBException {
		LogST.logP(1,"Matrix ValueRemapper - start mapping on body of the temp csv file");

		Dataset dataset = MappingSingleton.getInstance().mapping.getDatasetByName(datasetName);

		// this gets the attributes form LIAD --> will skip the VoidMapping
		Collection<String> requiredAttributeNames = dataset.getLIADAttributes(iadCsv.getFileName(), iadCsv.getHeader());

		//this collects the voidMapping attributes
		Collection<String> missingAttributeNames= dataset.getVoidAttributes();

		LogST.logP(
				0,
				"Matrix ValueRemapper.applyMappingToDataValues() remapping with requiredAttributeNames: "
						+ requiredAttributeNames.toString() + "\n and missingAttributeNames "
						+ missingAttributeNames.toString());
		
		for (String attribute : requiredAttributeNames) { // for all attributes to remap
			Collection<String> columnValues = null; 
			// FIXME 2: we reuse the same variable for the result. If so, make it a one-argument call.
			// iadCsv.getColumnValues(attribute, true);
			//this is moved down to allow controlling IF we want to extend the column set
			if (dataset.isRecodingNeeded(attribute)) {
				AbstractMapping m =dataset.getMappingByLIADAttribute(attribute);
				/***
				 * voidmapping is separate
				if ( m instanceof VoidMapping) {
					columnValues = applyRecoding((VoidMapping) m, iadCsv.getColumnValues(attribute, ((VoidMapping)m).getDefaultValue()));
				} else     
				 *
				 ***/
				if ( m instanceof SimpleMapping) { 
					// FIXME: this only works for SimpleMapping; extend and decompose into functions, check errors
					// in simpleMapping we do not create new columns
					columnValues = applyRecoding((SimpleMapping) m, iadCsv.getColumnValues(attribute, null));
					// LogST.logP(1, "Matrix ValueRemapper - recoding done");
					iadCsv.setColumnValues(dataset.getIADAttributeByLIADAttribute(attribute), columnValues);
				} else {
					LogST.logP(0, "Matrix ValueRemapper - WARNING unsupported mapping subclass for file " 
							+ iadCsv.getFileName() + " column " + attribute);
				}
				
			}
			else {
				LogST.logP(0, "Matrix ValueRemapper - WARNING value recoding was skipped for file " + iadCsv.getFileName()
						+ " column " + attribute);
			}
			// FIXME: qua si puo ottimizzare il codice poiche nell'header ci sono gia i valori IAD necessari
			// basta mettere un contatore esterno così da levare la chiamata sotto
			//iadCsv.setColumnValues(dataset.getIADAttributeByLIADAttribute(attribute), columnValues);
		}

		for (String attribute : missingAttributeNames) { // for all void attributes 
			Collection<String> columnValues = null; 
			AbstractMapping m =dataset.getMappingByIADAttribute(attribute);
			if ( m instanceof VoidMapping) {
				columnValues = applyRecoding((VoidMapping) m, iadCsv.getColumnValues(attribute, ((VoidMapping)m).getDefaultValue()));
				// we shall set the new colum into place
				iadCsv.setColumnValues(attribute, columnValues); // this is a IAD attribute name

			} else { //this should not be possible
				LogST.logP(0, "Matrix ValueRemapper - WARNING bogus VoidMapping attribute for file "
						+ iadCsv.getFileName() + " column " + attribute);
				throw new Error ("Matrix ValueRemapper - WARNING bogus VoidMapping attribute");
			}
		}
		
		LogST.logP(0, "Matrix ValueRemapper - body correctly mapped");
	}

	/**
	 * Performs the mapping of the header section, i.e. it rewrites the CSV header with the definition of all the
	 * IAD data fields provided by the specific mapping and corresponding to the (LIAD) names found in the csv header. 
	 * Uses and modifies information from the CSVFile object. The dataset name is looked up for in the mapping singleton.
	 * 
	 * @param datasetName
	 *            The name of the dataset to be mapped.
	 * @param csv
	 * 
	 * @throws JAXBException
	 *             , BadFormattedMappingException
	 * @throws SyntaxErrorInMappingException 
	 */
	public static void applyMappingToCsvHeader(CSVFile csv, String datasetName) throws JAXBException,
			SyntaxErrorInMappingException {
		LogST.logP(1,"Matrix ValueRemapper - start mapping procedure on header of the temp csv file");

		
		Dataset dataset = MappingSingleton.getInstance().mapping.getDatasetByName(datasetName);
 
		ArrayList<String> header = (ArrayList<String>) csv.getHeader();
		// FIXME wrong solution, doesn't work for generic remapping!!
		ArrayList<String> newHeader = 
				//new ArrayList<String>();
//		
				(ArrayList<String>) dataset.getAllIADAttributesOrdered(header); //misses VoidMapping
		for (String attribute : dataset.getAllIADAttributes())
		{
			if (dataset.getMappingByIADAttribute(attribute) instanceof VoidMapping) 
				newHeader.add(attribute);
		}
		
		csv.setHeader(dataset.getAllIADAttributesOrdered(header)); // old method, doesn't include voids
		csv.setMappedHeader(newHeader); 							// real new header with voids

		/******* OLD CODE
		Dataset dataset = MappingSingleton.getInstance().mapping.getDatasetByName(datasetName);

		ArrayList<String> header = (ArrayList<String>) csv.getHeader();
		csv.setHeader(dataset.getAllIADAttributesOrdered(header));

		
		********/
		
		LogST.logP(1,"Matrix ValueRemapper - header correctly mapped");
	}

	/**
	 * Performs the mapping of a set of values to IAD values by using a recoding table. The recoding table has been
	 * implemented, but better error checking should be done for values not in the table. Current semantics is that
	 * unmapped values silently become null (e.g. missing). No recoding is performed if no HashMap is available (identity).
	 * 
	 * FIXME_DESIGN if the only use is to translate the in-memory buffer, make it void and modify the parameter.
	 * 
	 * @param mapping
	 *            the mapping object with the recoding table to use
	 * @param columnValues
	 *            the values
	 * 
	 * @return a <code>Collection<String></code>
	 */
	private static Collection<String> applyRecoding(SimpleMapping mapping, Collection<String> columnValues) {
		// for each item in columnValues
		// look it up in the mapping table
		// transcode it or set it to null (and issue warning)
		// question is if we should work in place or return a different data structure

		HashMap<String,String> recodingTable = mapping.getRecodingHashMap();
		if (recodingTable != null){
			ArrayList<String> newColumnValues = new ArrayList<String>(columnValues.size());
			for (String s : columnValues) {
				newColumnValues.add(recodingTable.get(s));
			}
			return newColumnValues;
		}
		
		return columnValues;
	}


	/**
	 * Performs the mapping of a column to IAD values when the column does not have a corresponding LIAD one
	 * 
	 * FIXME_DESIGN if the only use is to translate the in-memory buffer, make it void and modify the parameter.<br> 
	 * @param mapping
	 *            the mapping object of VoidMapping type
	 * @param columnValues
	 *            the values
	 * 
	 * @return a <code>Collection<String></code>
	 */
	private static Collection<String> applyRecoding(VoidMapping mapping, Collection<String> columnValues) {
		// TODO shall we introduce some changes/casts here in the data?
		return columnValues;
	}
}
