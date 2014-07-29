package org.erasmusmc.utilities;

import java.util.Iterator;
import java.util.List;

/**
 * Helper class for processing sequences of objects. 
 * @author schuemie
 *
 * @param <I>
 * @param <O>
 */
public abstract class SequenceProcessing<I,O> implements Iterator<O> {
	
	private Iterator<I> inputIterator;
	private Iterator<O> outputIterator;
	private O buffer;	
	
	public SequenceProcessing(Iterator<I> inputIterator){
		this.inputIterator = inputIterator;
		outputIterator = getNextOutputIterator();
		if (outputIterator != null && outputIterator.hasNext())
			buffer = outputIterator.next();
		else 
			buffer = null;
	}
	
	
	private Iterator<O> getNextOutputIterator() {
		while (inputIterator.hasNext()){
			List<O> output = processInput(inputIterator.next());
			if (output != null && output.size() != 0)
				return output.iterator();
		}
		return null;
	}
	
	@Override
	public boolean hasNext() {
		return (buffer != null);
	}

	@Override
	public O next() {
		O next = buffer;
		if (outputIterator.hasNext())
			buffer = outputIterator.next();
		else {
			outputIterator = getNextOutputIterator();
			if (outputIterator == null)
				buffer = null;
			else 
				buffer = outputIterator.next();
		}

		return next;
	}

	@Override
	public void remove() {		
	}
	
	public abstract List<O> processInput(I input);
}

