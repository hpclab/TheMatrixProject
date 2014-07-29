package org.erasmusmc.jerboa.dataClasses;

public class Exposure {
  public long start;
  public String caseset;
  public String type;
  public String patientID;  

  public Exposure() {
  }

  public Exposure(Exposure exposure) {
    this.start = exposure.start;
    this.caseset = exposure.caseset;
    this.type = exposure.type;
    this.patientID = exposure.patientID;
  }

}