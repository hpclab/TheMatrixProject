package org.erasmusmc.jerboa.postProcessing;

public class Signal implements Comparable<Signal>{
	public String atc;
	public String eventType;

	public Signal(String atc, String eventType) {
		this.atc = atc;
		this.eventType = eventType;
	}

	public int hashCode(){
		return (atc+eventType).hashCode();
	}
	
	public String toString(){
		return atc+"_"+eventType;
	}
	
	public boolean equals(Object object){
		if (object instanceof Signal){
			Signal other = (Signal)object;
			return (other.atc.equals(atc) && other.eventType.equals(eventType));
		} else
			return false;
	}

	@Override
	public int compareTo(Signal arg0) {
		int result = this.atc.compareTo(arg0.atc);
		if (result == 0)
			return this.eventType.compareTo(arg0.eventType);
		else
			return result;
	}
}
