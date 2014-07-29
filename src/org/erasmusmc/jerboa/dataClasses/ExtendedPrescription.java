package org.erasmusmc.jerboa.dataClasses;

public class ExtendedPrescription extends Prescription {
	public double dddTotal;
	public double totalUnits;
	public double unitsPerDay;
  public ExtendedPrescription(Prescription prescription){
  	super(prescription);
  }
}
