package it.cnr.isti.thematrix.mapping.utils;

/**
 * WardEnum Class for TypeWard enum
 * TYPE_WARD check in no case sensitive way.
 * 
 * @author marco
 *
 */
public class WardEnum {

	public static enum TypeWard{
		AC, NOTAC;
	}

	/**
	 * Method getTypeWard for get a TypeWard type given a string value (no case sensitive)
	 * 
	 * @param tWard
	 * @return TypeWard.AC or NOTAC
	 */
	public static TypeWard getTypeWard(String tWard){
		return TypeWard.valueOf(tWard.toUpperCase());
	}
	
	/**
	 * Method isInEnum to check if the String value is a correct Type_Outpat (no case sensitive)
	 * 
	 * @param value
	 * @return boolean
	 */
	public static boolean isInEnum(String value) {
		  for (TypeWard t : TypeWard.values()) {
		    if(t.name().equals(value.toUpperCase())) { return true; }
		  }
		  return false;
		}
}
