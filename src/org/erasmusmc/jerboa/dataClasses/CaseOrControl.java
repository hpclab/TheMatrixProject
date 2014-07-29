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
