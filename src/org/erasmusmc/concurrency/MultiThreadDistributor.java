package org.erasmusmc.concurrency;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class MultiThreadDistributor<T> {
	private MultiThreadDistributorListener<T> listener;
	private Iterator<T> iterator;
	private List<ExecutionThread> threads;
	private int numberOfThreads;
	private ReentrantLock lock = new ReentrantLock();
	
  public MultiThreadDistributor(MultiThreadDistributorListener<T> listener, int numberOfThreads){
  	this.listener = listener;
  	threads =  new ArrayList<ExecutionThread>(numberOfThreads);
  	this.numberOfThreads = numberOfThreads;
  }
  
  public void process(Iterator<T> iterator){
  	this.iterator = iterator;
  	threads.clear();
  	for (int i = 0; i < numberOfThreads; i++){
  		ExecutionThread thread = new ExecutionThread();
  		thread.start();
  		threads.add(thread);
  	}
  	
  	for (ExecutionThread thread : threads)
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
  }
  
  private class ExecutionThread extends Thread {
  	
  	public void run(){
  		T object = getNext();
  		while (object != null){
  			listener.process(object);
  			object = getNext();
  		}
  	}
  	
  	private T getNext(){
  		T result;
  		lock.lock();
  		if (iterator.hasNext())
  		  result = iterator.next();
  		else
  			result = null;
  		lock.unlock();
  		return result;
  	}
  }
}
