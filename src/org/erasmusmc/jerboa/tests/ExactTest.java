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
package org.erasmusmc.jerboa.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.erasmusmc.jerboa.calculations.CaseControlData;
import org.erasmusmc.jerboa.calculations.FastStratifiedExact;
import org.erasmusmc.jerboa.calculations.FastStratifiedExact.Result;
import org.erasmusmc.jerboa.calculations.PersonTimeData;
import org.erasmusmc.jerboa.calculations.StratifiedExact;
import org.erasmusmc.utilities.WriteTextFile;

public class ExactTest {
	public static String filename = "/home/schuemie/temp/ExactMA/testData.dat";
	public static int strata = 20;
	public static double rr = 0.9;
	public static double incidenceUnexposed = 0.0001;
	public static int meanUnexposedDays = 5000000;
	public static double sdUnexposedDays = 2000000;
	public static int meanExposedDays = 20000;
	public static double sdExposedDays = 10000;
	
	public static double oddsUnexposed = 0.5;
	public static int meanUnexposedPersons = 800;
	public static double sdUnexposedPersons = 100;
	public static int meanExposedPersons = 800;
	public static double sdExposedPersons = 100;
	
	public static void main(String[] args) {
		testStratifiedPersonTime();
		//testStratifiedCaseControl();
	}
	private static void testStratifiedPersonTime(){
		Random random = new Random(1);
		WriteTextFile out = new WriteTextFile(filename);
		out.writeln("[9         ]"+(char)(13)+"\nsfINCLUDE STUDYTYPE sfLABEL$ NUMER_TX DENOM_TX NUMER_CO DENOM_CO sfSUBGRP$ "+(char)(13));
		Map<String, PersonTimeData> key2dataCases = new HashMap<String, PersonTimeData>();
		Map<String, PersonTimeData> key2dataBackground = new HashMap<String, PersonTimeData>();
		PersonTimeData data;
		for (int i =0; i < strata; i++){
			String stratus = Integer.toString(i);
			int exposedDays = (int)Math.max(0,meanExposedDays + Math.round(random.nextGaussian()*sdExposedDays));
			int unExposedDays = (int)Math.max(0,meanUnexposedDays + Math.round(random.nextGaussian()*sdUnexposedDays));
			int unExposedEvents = (int)(incidenceUnexposed * unExposedDays);
			int exposedEvents = (int)(rr * incidenceUnexposed * exposedDays * (1+random.nextGaussian()*.2));
				
			System.out.println("Exposed: " + exposedDays + " days, " + exposedEvents + " events, Unexposed: " + unExposedDays + " days, " + unExposedEvents + " events");
			data = new PersonTimeData();
			data.events = exposedEvents;
			data.days = exposedDays;
			key2dataCases.put(stratus, data);

			data = new PersonTimeData();
			data.events = exposedEvents + unExposedEvents;
			data.days = exposedDays + unExposedDays;
			key2dataBackground.put(stratus, data);
			out.writeln("1 1 \""+stratus+"\" "+exposedEvents+" "+exposedDays+" "+unExposedEvents+" "+unExposedDays+" \""+stratus+"\" "+(char)(13));
		}
		
		out.close();
		long start = System.currentTimeMillis();
		Result result = new FastStratifiedExact().calculateExactStats(key2dataCases, key2dataBackground);
		System.out.println("Time: " + (System.currentTimeMillis()-start) + " ms");
		outputResults(result);
	}
	
	private static void testStratifiedCaseControl(){
		Random random = new Random(1);
		WriteTextFile out = new WriteTextFile(filename);
		out.writeln("[9         ]"+(char)(13)+"\nsfINCLUDE STUDYTYPE sfLABEL$ NUMER_TX DENOM_TX NUMER_CO DENOM_CO sfSUBGRP$ "+(char)(13));
		Map<String, CaseControlData> key2data = new HashMap<String, CaseControlData>();
		CaseControlData data;
		for (int i =0; i < strata; i++){
			String stratus = Integer.toString(i);
			int exposedPersons = (int)Math.max(0,meanExposedPersons + Math.round(random.nextGaussian()*sdExposedPersons));
			int unExposedPersons = (int)Math.max(0,meanUnexposedPersons + Math.round(random.nextGaussian()*sdUnexposedPersons));
			int unExposedEvents = (int)(oddsUnexposed * unExposedPersons);
			int exposedEvents = (int)(rr * oddsUnexposed * exposedPersons * (1+random.nextGaussian()*.2));
			int cases = unExposedEvents + exposedEvents;
			int controls = exposedPersons + unExposedPersons - cases;
			System.out.println("Exposed: " + exposedPersons + " persons, " + exposedEvents + " events, Unexposed: " + unExposedPersons + " persons, " + unExposedEvents + " events");
			data = new CaseControlData();
			data.caseAndExposed = exposedEvents;
			data.controlAndExposed = exposedPersons - exposedEvents;
			data.caseNotExposed = unExposedEvents;
			data.controlNotExposed = unExposedPersons - unExposedEvents;
			key2data.put(stratus, data);
			out.writeln("1 0 \""+stratus+"\" "+data.caseAndExposed+" "+cases+" "+data.controlAndExposed+" "+controls+" \""+stratus+"\" "+(char)(13));
		}
		
		out.close();
		long start = System.currentTimeMillis();
		Result result = new FastStratifiedExact().calculateExactStats(key2data);
		//org.erasmusmc.jerboa.calculations.StratifiedExact.Result result = new StratifiedExact().calculateExactStats(key2data);
		System.out.println("Time: " + (System.currentTimeMillis()-start) + " ms");
		outputResults(result);
	}
	
	private static void outputResults(Result result){
		System.out.println("Fisher upper P: " + result.upFishPVal);
		System.out.println("Fisher lower P: " + result.loFishPVal);
		System.out.println("MidP upper P: " + result.upMidPPVal);
		System.out.println("MidP lower P: " + result.loMidPPVal);
		System.out.println("MidP 2-tail P: " + Math.min(result.upMidPPVal,result.loMidPPVal)*2);
		System.out.println("CMLE: " + result.cmle);
		System.out.println("Fisher upper limit: " + result.upFishLimit);
		System.out.println("Fisher lower limit: " + result.loFishLimit);
		System.out.println("MidP upper limit: " + result.upMidPLimit);
		System.out.println("MidP lower limit: " + result.loMidPLimit);
	}
	
	private static void outputResults(org.erasmusmc.jerboa.calculations.StratifiedExact.Result result){
		System.out.println("Fisher upper P: " + result.upFishPVal);
		System.out.println("Fisher lower P: " + result.loFishPVal);
		System.out.println("MidP upper P: " + result.upMidPPVal);
		System.out.println("MidP lower P: " + result.loMidPPVal);
		System.out.println("MidP 2-tail P: " + Math.min(result.upMidPPVal,result.loMidPPVal)*2);
		System.out.println("CMLE: " + result.cmle);
		System.out.println("Fisher upper limit: " + result.upFishLimit);
		System.out.println("Fisher lower limit: " + result.loFishLimit);
		System.out.println("MidP upper limit: " + result.upMidPLimit);
		System.out.println("MidP lower limit: " + result.loMidPLimit);
	}
}
