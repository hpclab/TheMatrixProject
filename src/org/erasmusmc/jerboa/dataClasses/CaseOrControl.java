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
package org.erasmusmc.jerboa.dataClasses;

import java.util.HashMap;
import java.util.Map;

public class CaseOrControl {
	public String patientID;
  public int caseSetID;
  public String eventType;
  public boolean isCase;
  public Map<String, String> variables = new HashMap<String, String>();
  public Map<String, WindowStats> window2stats = new HashMap<String, WindowStats>();
  public Prescription mainPrescription;

  public static class DrugStats {
  	public long daysUsed;
  	public long daysSinceUse = Long.MAX_VALUE;
  	public String ageRange;
  }
  
  public static class WindowStats extends HashMap<String, DrugStats>{
		private static final long serialVersionUID = -7760944728772314004L;
	}
  
}
