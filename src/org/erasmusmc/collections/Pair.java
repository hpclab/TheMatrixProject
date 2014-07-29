package org.erasmusmc.collections;

public class Pair<A, B> {
	public A object1;
	public B object2;
	public Pair(A object1, B object2){
		this.object1 = object1;
		this.object2 = object2;
	}

	/* this is IMHO not readable but let's not change it for compatibility*/
	public String toString(){
		return "[[" + object1.toString() + "],[" + object2.toString() + "]]";
	}

	public int hashCode(){
		return object1.hashCode() + object2.hashCode();
	}

	@SuppressWarnings("rawtypes")
	public boolean equals(Object other){
		if (other instanceof Pair)
			if (((Pair)other).object1.equals(object1))
				if (((Pair)other).object2.equals(object2))
					return true;
		return false;  		 
	}
}
