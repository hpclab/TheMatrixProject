package org.erasmusmc.utilities;

import java.util.Random;

public class SampleUtilities {


	public static void main(String[] args){
		Random random = new Random();
		for (int j = 0; j < 20; j++){
			for (int i : sampleWithoutReplacement(3,10, random))
				System.out.print(i + " ");
			System.out.println("");
		}
	}

	public static int[] sampleWithoutReplacement(int sampleSize, int populationSize, Random random){
		if (sampleSize > populationSize)
			throw new RuntimeException(String.format("Samplesize (%s) larger than populationsize (%s)", Integer.toString(sampleSize), Integer.toString(populationSize)));
		int[] samples = new int[sampleSize];
		if (sampleSize == populationSize){
			for (int i = 0; i < sampleSize; i++)
				samples[i] = i;
		} else {
			for (int i = 0; i < sampleSize; i++){
				int tempID = random.nextInt(populationSize-i);
				int j = 0;
				for (; j < i; j++){
					if (tempID >= samples[j])
						tempID++;
					else
						break;
				}
				System.arraycopy(samples, j, samples, j+1, i-j);
				samples[j] = tempID;
			}
		}
		return samples;
	}
	
}
