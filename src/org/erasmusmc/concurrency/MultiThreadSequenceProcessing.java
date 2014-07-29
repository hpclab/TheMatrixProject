package org.erasmusmc.concurrency;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class can be used when objects coming from an iterator have to processed, and the results of the processing is a list of objects over which
 * you need to iterate. So the input is an iterator over objects of class I, and the output is an iterator over objects of class O. The processInputs method 
 * must be implemented to consume input and produce a list of outputs (can be size 0). The processing is performed in parallel threads. The sequence of input to
 * output is preserved.
 * @author schuemie
 *
 * @param <I>	Class of the input objects
 * @param <O>	Class of the output objects
 */
public abstract class MultiThreadSequenceProcessing<I,O> implements Iterator<O> {
	
	private Iterator<I> inputIterator;
	private List<ProcessingThread> threads;
	private int threadCursor;
	private Iterator<O> outputIterator;
	private O buffer;	
	private boolean noMoreInputs;
	
	public MultiThreadSequenceProcessing(Iterator<I> inputIterator, int nrOfThreads){
		this.inputIterator = inputIterator;
		threads = new ArrayList<ProcessingThread>(nrOfThreads);
		for (int i = 0; i < nrOfThreads; i++){
			if (inputIterator.hasNext()){
				ProcessingThread thread = new ProcessingThread();
				thread.input = inputIterator.next();
			  threads.add(thread);
			  thread.proceed();
			}
		}
		threadCursor = 0;
		noMoreInputs = false;
		outputIterator = getNextOutputIterator();
		if (outputIterator != null && outputIterator.hasNext())
			buffer = outputIterator.next();
	}
	
	
	private Iterator<O> getNextOutputIterator() {
		while (!noMoreInputs){
			ProcessingThread thread = threads.get(threadCursor);
			if (thread == null)
				noMoreInputs = true;
			else {
				thread.waitUntilFinished();
				List<O> output = thread.output;
				thread.output = null;
				if (inputIterator.hasNext()){
				  thread.input = inputIterator.next();
				  thread.proceed();
				} else {
					thread.terminate();
					threads.set(threadCursor, null);
				}

				threadCursor++;
				if (threadCursor == threads.size())
					threadCursor = 0;
				if (output == null)
					System.err.println("asdf");
				if (output != null && output.size() != 0)
					return output.iterator();
			}
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
	
	
	private class ProcessingThread extends BatchProcessingThread {

		public I input;
		public List<O> output;
		@Override
		protected void process() {
		  output = processInput(input);
		}
	}
	
	
	
}