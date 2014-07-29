/**
 * 
 */
package org.erasmusmc.jerboa.calculations;

public class CaseControlData {
  public int caseAndExposed = 0;
  public int caseNotExposed = 0;
  public int controlAndExposed = 0;
  public int controlNotExposed = 0;
  public double total(){
    return caseAndExposed + caseNotExposed + controlAndExposed + controlNotExposed;
  }
  public double cases(){
    return caseAndExposed + caseNotExposed;
  }
  
  public double controls(){
    return controlAndExposed + controlNotExposed;
  }
  
  public double exposed(){
    return caseAndExposed + controlAndExposed;
  }
  
  public double notExposed(){
    return caseNotExposed + controlNotExposed;
  }
  
  public void add(CaseControlData other){
    caseAndExposed += other.caseAndExposed;
    caseNotExposed += other.caseNotExposed;
    controlAndExposed += other.controlAndExposed;
    controlNotExposed += other.controlNotExposed;
  }
}