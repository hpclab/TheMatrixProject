package org.erasmusmc.collections;

import java.io.Serializable;

public class SortedPair<T extends Comparable<T>> implements Comparable<SortedPair<T>>, Serializable{
	private static final long serialVersionUID = -5463993741387589072L;
	private T object1;
	private T object2;
	public SortedPair(T object1, T object2){
		if (object1.compareTo(object2) > 0){
			this.object2 = object1;
			this.object1 = object2;
		}else {
			this.object1 = object1;
			this.object2 = object2;
		}
	}

	public String toString(){
		return "[[" + object1.toString() + "],[" + object2.toString() + "]]";
	}
	
	public T getObject1() {
		return object1;
	}

	public T getObject2() {
		return object2;
	}

	
	public int compareTo(SortedPair<T> arg0) {
		int match = object1.compareTo(arg0.object1);
		if (match != 0)
			return match;
		return object2.compareTo(arg0.object2);
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj){
		if (obj instanceof SortedPair){
			SortedPair<T> other = ((SortedPair<T>)obj);
			return (other.object1.equals(object1) && other.object2.equals(object2));
		}
		return false;	
	}
	
	public int hashCode(){
		return object1.hashCode() + object2.hashCode();
	}
}