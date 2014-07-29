package org.erasmusmc.jerboa.calculations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.erasmusmc.math.ChiSquaredProbabilityFunction;

public class MantelHaenszel {
  
  public static void main(String[] args){
    List<CaseControlData> ccData = new ArrayList<CaseControlData>();
    
    CaseControlData data = new CaseControlData();
    data.caseAndExposed = 3;
    data.controlAndExposed = 104;
    data.caseNotExposed = 9;
    data.controlNotExposed = 1059;
    ccData.add(data);

    data = new CaseControlData();
    data.caseAndExposed = 1;
    data.controlAndExposed = 5;
    data.caseNotExposed = 3;
    data.controlNotExposed = 86;
    ccData.add(data);

    
    Stats stats = calculateCaseControlStats(ccData);
    System.out.println(stats.rrmh + "\t" + stats.ci95up + "\t" + stats.ci95down + "\t" + stats.p);
    
  }
  public static Stats calculateRateStats(Map<String, PersonTimeData> key2dataCases, Map<String, PersonTimeData> key2dataBackground) {
    double g = 0;
    double h = 0;
    double v = 0;
    int events = 0;
    double e = 0;
    double expected = 0;
    for (Map.Entry<String, PersonTimeData> entry : key2dataCases.entrySet()){
      PersonTimeData background = getBackground(entry.getKey(),key2dataBackground);//.get(entry.getKey());
      PersonTimeData data = entry.getValue();
      int nonExposedEvents = background.events - data.events;
      long nonExposedDays = background.days - data.days;
      g += data.events * nonExposedDays / (double)background.days;
      h += (long)nonExposedEvents * data.days / (double)background.days;
      v += background.events*data.days*(nonExposedDays/Math.pow(background.days, 2));
      events += data.events;
      e += (long)background.events*data.days/(double)background.days;
      if (nonExposedDays != 0)
        expected += data.days * (long)nonExposedEvents / (double)nonExposedDays;
    }
    double var = v/g/h;
    double mh = (events-e)/Math.sqrt(v);
    mh = Math.abs(mh);
    Stats result = new Stats();
    result.rrmh = g/h;
    result.ci95up = Math.exp(Math.log(result.rrmh)+1.96*Math.sqrt(var));
    result.ci95down = Math.exp(Math.log(result.rrmh)-1.96*Math.sqrt(var));
    result.p = ChiSquaredProbabilityFunction.chiSquaredProbabilityFunction(mh*mh, 1);
    result.se = Math.sqrt(var);
    result.expected = expected;
    return result;
  }
  private static PersonTimeData getBackground(String key, Map<String, PersonTimeData> key2dataBackground) {
  	String[] keyParts = key.split(":");
  	if (keyParts[0].length() != 0 && keyParts[1].length() != 0)
  		return key2dataBackground.get(key);
  	else {
  		if (keyParts[1].length() != 0){
  			PersonTimeData data = new PersonTimeData();
  			String searchKey = ":"+keyParts[1];
  			for (String otherKey : key2dataBackground.keySet()){
  				if (otherKey.contains(searchKey))
  					data.add(key2dataBackground.get(otherKey));
  			}
  			return data;
  		} else if (keyParts[0].length() != 0){
  			PersonTimeData data = new PersonTimeData();
  			String searchKey = keyParts[1]+":";
  			for (String otherKey : key2dataBackground.keySet()){
  				if (otherKey.contains(searchKey))
  					data.add(key2dataBackground.get(otherKey));
  			}
  			return data;
  		} else { //Both age and gender are unknown
  			PersonTimeData data = new PersonTimeData();
  			for (PersonTimeData otherData : key2dataBackground.values())
  				data.add(otherData);
  			return data;			
  		}
  	}
  }
  
	public static Stats calculateCaseControlStats(List<CaseControlData> ccData){
    double g = 0;
    double h = 0;
    double v = 0;
    double gp = 0;
    double gqhp = 0;
    double hq = 0;
    int events = 0;
    double e = 0;
    for (CaseControlData data : ccData){
      StringBuilder sb = new StringBuilder();
      sb.append("a: " + data.caseAndExposed);
      sb.append("\tb: " + data.caseNotExposed);
      sb.append("\tc: " + data.controlAndExposed);
      sb.append("\td: " + data.controlNotExposed);
      System.out.println(sb.toString());
      double localG = data.caseAndExposed * data.controlNotExposed / data.total();
      double localH = data.caseNotExposed * data.controlAndExposed / data.total();
      g += localG;
      h += localH;
      double p = (data.caseAndExposed + data.controlNotExposed)/data.total();
      double q = (data.caseNotExposed + data.controlAndExposed)/data.total();
      gp += localG*p;
      hq += localH*q;
      gqhp += localG*q + localH*p;
      v += (data.cases()*data.controls()*data.exposed()*data.notExposed())/data.total()/data.total()/(data.total()-1);
      events += data.caseAndExposed;
      e += data.cases()*data.exposed()/data.total();
    }
    double var = gp/(2*g*g) + gqhp/(2*g*h) + hq/(2*h*h);
    double mh = (events-e)/Math.sqrt(v);
    mh = Math.abs(mh);
    Stats result = new Stats();
    result.rrmh = g/h;
    result.ci95up = Math.exp(Math.log(result.rrmh)+1.96*Math.sqrt(var));
    result.ci95down = Math.exp(Math.log(result.rrmh)-1.96*Math.sqrt(var));
    result.se = Math.sqrt(var);
    //result.p = AssociationMeasures.chiSquareToP(mh*mh);
    //result.p = 2*(0.3989423 * Math.exp(-mh*mh/2) * (1 / (1 + 0.231649 * mh))  * ((((1.330274 * (1 / (1 + 0.231649 * mh)) - 1.821256) * (1 / (1 + 0.231649 * mh)) + 1.781478) *( 1 / (1 + 0.231649 *mh)) - 0.3565638) * (1 / (1 + 0.231649 *mh) ) + 0.3193815));
    result.p = ChiSquaredProbabilityFunction.chiSquaredProbabilityFunction(mh*mh, 1);
    System.out.println("*** rrmn = " + result.rrmh + ", p = " + result.p + " ***");
    return result;
  }
  
  public static class Stats {
    public double rrmh = 0;
    public double ci95up = 0;
    public double ci95down = 0;
    public double p = 0;
    public double se = 0;
    public double expected = 0;
  }
}
