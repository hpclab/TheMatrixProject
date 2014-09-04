/*
 * Copyright (c) Erasmus MC
 *
 * This file is part of TheMatrix.
 *
 * TheMatrix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

