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
package it.cnr.isti.thematrix.runtime;
import it.cnr.isti.thematrix.configuration.LogST;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Class to regularly check the amount of free memory. We should probably use JMX instead.
 * 
 * @author massimo
 *
 */
public class MemoryChecker extends Thread 
{

	private long freeMem, totalMem, maxMem;
	private Runtime runtime;
	private boolean running=false;
	private SimpleDateFormat dt = new SimpleDateFormat("[yy-MM-dd HH:mm:ss]");
	
	/**
	 * check interval in seconds
	 */
	private int interval;
	
	final long mB = 1<<20; // display unit is megabytes 

	/**
	 */
	/**
	 * Constructor, initializes variables and emits first log. Preferably create this thread after the logging
	 * facilities have been already initialized.
	 * 
	 * @param interval number of seconds between check, in the range (0;600]; if not 30 seconds is used.
	 */
	public MemoryChecker(int interval) {
		runtime = Runtime.getRuntime();
		if (interval>0 & interval<601)
			this.interval=interval;
		else
			this.interval= 30;
		LogST.logP(-1, "MEMSTATS, \ttime,\t\tMax,\tTot,\tFree,\tTotDe,\tFreeDe");
		updateStats();
		printStats();
		running=true;
	}

	/**
	 * Access Java runtime and collect memory stats.
	 */
	private void updateStats() {
		freeMem = runtime.freeMemory()/mB;
		totalMem = runtime.totalMemory()/mB;
		maxMem = runtime.maxMemory()/mB;
	}

	/**
	 * Prepare and print log informations
	 */
	private void printStats(){
		Date date = new Date();
		long freeDelta = freeMem;
		long totalDelta = totalMem;
		updateStats();
		freeDelta = freeMem-freeDelta;
		totalDelta = totalMem - totalDelta;
//		LogST.logP(0, "MEMSTATS \ttime,\tMax,\tTot,\tFree,\tTotDe,\tFreeDe");
		LogST.logMemory("MEMSTATS, "+dt.format(date)+",\t" + maxMem + ",\t" + totalMem + ",\t" + freeMem
				+ ",\t" + totalDelta + ",\t" + freeDelta);
		
	}

	public void run()
	{
		while (running)
		{
			try {
				Thread.sleep(interval *1000);
				printStats();
			}catch (InterruptedException e) {
				// do nothing, while condition will take care
			}
		}
		LogST.logP(-1,"MEMSTAT MemoryChecker shutting down -- last log");
		printStats();
	}

	public void stopChecking(){
		running = false;
		this.interrupt();
	}
	
	
}
