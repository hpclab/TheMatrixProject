package org.erasmusmc.jerboa.modules;

import java.util.Iterator;
import java.util.Random;

import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.dataClasses.Patient;
import org.erasmusmc.jerboa.dataClasses.PatientFileReader;
import org.erasmusmc.jerboa.dataClasses.PatientFuzzyOffset;
import org.erasmusmc.jerboa.dataClasses.PatientFuzzyOffsetFileWriter;
import org.erasmusmc.utilities.FileSorter;

public class FuzzynessModule extends JerboaModule {
	
	private static final long serialVersionUID = 3516557934667539117L;

	public JerboaModule population;

	/**
	 * Specifies the level of noise to be added to dates.<BR>
	 * The offset is sampled from a normal distribution with a mean of 0, 
	 * and the standard deviation specified here.<BR>
	 * default = 0
	 */
	public double calenderTimeStandardDeviation = 0d;
	
	private Random random = new Random();

	public static void main(String[] args) {
		FuzzynessModule module = new FuzzynessModule();
		module.calenderTimeStandardDeviation = 2;
		String path = "D:\\Documents\\IPCI\\Vaccinaties\\Vaesco\\Jerboa Test\\Jerboa\\SCCS\\";
		FileSorter.sort(path + "Population.txt", new String[]{"PatientID"});
		module.process(path + "Population.txt", path + "Fuzzyness.txt");
	}

	@Override
	protected void runModule(String outputFilename) {
		FileSorter.sort(population.getResultFilename(), new String[]{"PatientID"});
		process(population.getResultFilename(), outputFilename);
	}
	
	public void process(String sourcePopulation, String targetFuzzyOffsets) {
		Patient currentPatient;		
		
		PatientFileReader population = new PatientFileReader(sourcePopulation);
		Iterator<Patient> populationIterator = population.iterator();

		PatientFuzzyOffsetFileWriter fuzzyData = new PatientFuzzyOffsetFileWriter(targetFuzzyOffsets);
		
		if (populationIterator.hasNext()) 
			currentPatient = populationIterator.next();
		else
			currentPatient = null;
		
		while (currentPatient != null) {
			PatientFuzzyOffset fuzzyPatient = new PatientFuzzyOffset();
			fuzzyPatient.patientID = currentPatient.patientID;
			fuzzyPatient.fuzzyOffset = getOffset(calenderTimeStandardDeviation);
			fuzzyData.write(fuzzyPatient);

			if (populationIterator.hasNext()) 
				currentPatient = populationIterator.next();
			else
				currentPatient = null;
			ProgressHandler.reportProgress();
		}
		//fuzzyData.flush();
		fuzzyData.close();
	}

	private long getOffset(double standardDeviation) {
		if (standardDeviation == 0)
		  return 0;
		else
			return Math.round(random.nextGaussian()*standardDeviation);
	}

}
