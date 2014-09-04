/*
 * Copyright (c) Erasmus MC
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.erasmusmc.jerboa.postProcessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

public class CaseControlSampler {

	public int sampleSize = 1;
	
	public boolean ignoreSetIfNotEnoughSamples = true;
	
	public long randomSeed = 0;
	
	private Random random = new Random(0);

	public static void main(String[] args) {
		CaseControlSampler module = new CaseControlSampler();
		module.process("/home/data/OSIM/CaseControlForJan/CaseSets.txt", "/home/data/OSIM/CaseControlForJan/CaseSetsSampled.txt");
		//module.process("/home/data/OSIM/CaseControlForJan/test.txt", "/home/data/OSIM/CaseControlForJan/CaseSetsSampled.txt");
	}
	
	public void process(String sourceCaseSets, String targetCaseSets){
		random = new Random(randomSeed);
		Map<Integer, List<Integer>> caseSetID2Indexes = sampleIndexes(sourceCaseSets);		
		System.out.println("Retrieving samples");
		
		CountingSet<Integer> caseSetCounts = new CountingSet<Integer>(caseSetID2Indexes.size());
		WriteCSVFile out = new WriteCSVFile(targetCaseSets);
		Iterator<List<String>> iterator = new ReadCSVFile(sourceCaseSets).iterator();
		List<String> header = iterator.next();
		out.write(header);
		int caseSetIDCol = header.indexOf("CaseSetID");
		int isCaseCol = header.indexOf("IsCase");
		int beforeCount = 0;
		int afterCount = 0;
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			beforeCount++;
			int caseSetID = Integer.parseInt(cells.get(caseSetIDCol));
			List<Integer> indexes = caseSetID2Indexes.get(caseSetID);
			if (indexes != null) {
			  if (cells.get(isCaseCol).equals("1")){ //Allways include case
			  	out.write(cells);
			  	afterCount++;
					if (afterCount % 10000 == 0){
						System.out.println("Processed " +  afterCount + " cases.");
						out.flush();
					}
			  } else {
			  	if (indexes.size() != 0){
			  		int count = caseSetCounts.getCount(caseSetID);
			  		if (indexes.get(0).equals(count)){
			  			out.write(cells);
			  			indexes.remove(0);
			  			afterCount++;
			  		}
			  		caseSetCounts.add(caseSetID);
			  	}
			  }
			}
		}
		out.close();
		System.out.println("Original cases and controls: " + beforeCount + ", after sampling: " + afterCount);
	}

	private Map<Integer, List<Integer>> sampleIndexes(String sourceCaseSets) {
		CountingSet<Integer> sets = countControlsPerSet(sourceCaseSets);
		if (ignoreSetIfNotEnoughSamples)
			removeSmallSets(sets);
		System.out.println("Creating sample selection.");
		Map<Integer, List<Integer>> sampledIndexes = new HashMap<Integer, List<Integer>>(sets.size());
		Iterator<Map.Entry<Integer, CountingSet.Count>> iterator = sets.key2count.entrySet().iterator();
		int sampleCount = 0;
		while (iterator.hasNext()){
			Map.Entry<Integer, CountingSet.Count> entry = iterator.next();
			int count = entry.getValue().count;
			List<Integer> samples;
			if (count <= sampleSize){
				samples = new ArrayList<Integer>(count);
				for (int i = 0; i < count; i++)
					samples.add(count);
			} else {
			  samples = new ArrayList<Integer>(sampleSize);
			  for (int i = 0; i < sampleSize; i++)
				  sampleWithoutReturning(entry.getValue().count, samples);
			}
			sampledIndexes.put(entry.getKey(), samples);
			sampleCount += samples.size();
		}
		System.out.println("Selected " + sampleCount + " samples.");
		return sampledIndexes;
	}

	public void sampleWithoutReturning(int size, List<Integer> samples) {
		int sampleRange = size - samples.size();
		int index = random.nextInt(sampleRange);
		int i = 0;
		while (i < samples.size()){
			if (samples.get(i) <= index)
				index++;
			else 
				break;
			i++;
		}
	  samples.add(i, index);
	}

	private void removeSmallSets(CountingSet<Integer> sets) {
		System.out.println("Removing small case control sets.");
		Iterator<Map.Entry<Integer, CountingSet.Count>> iterator = sets.key2count.entrySet().iterator();
		int count = 0;
		while (iterator.hasNext()){
			if (iterator.next().getValue().count < sampleSize){
				iterator.remove();
				count++;
			}
		}
		System.out.println("Removed " + count + " case sets because not enough controls.");
	}

	private CountingSet<Integer> countControlsPerSet(String sourceCaseSets) {
		System.out.println("Counting available controls per case.");
		CountingSet<Integer> result = new CountingSet<Integer>();
		Iterator<List<String>> iterator = new ReadCSVFile(sourceCaseSets).iterator();
		List<String> header = iterator.next();
		int caseSetIDCol = header.indexOf("CaseSetID");
		int isCaseCol = header.indexOf("IsCase");
		int caseCount = 0;
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			if (cells.get(isCaseCol).equals("0"))
				result.add(Integer.parseInt(cells.get(caseSetIDCol)));
			else {
				caseCount++;
				if (caseCount % 10000 == 0)
					System.out.println("Processed " +  caseCount + " cases.");
			}
		}
		return result;
	}
}

