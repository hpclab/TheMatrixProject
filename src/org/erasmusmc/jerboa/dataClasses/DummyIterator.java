package org.erasmusmc.jerboa.dataClasses;

import java.util.Iterator;

public class DummyIterator<T> implements Iterator<T>{

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public T next() {
		return null;
	}

	@Override
	public void remove() {	
	}
	
}