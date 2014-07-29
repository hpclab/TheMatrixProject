package org.erasmusmc.concurrency;

import java.util.LinkedList;
import java.util.Queue;


public class QueueSemaphore<T> {
	private Queue<T> queue = new LinkedList<T>();
  

  public synchronized void take(T value) {
    queue.offer(value);
    this.notify();
  }

  public synchronized T release() {
    while(queue.isEmpty())
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    return queue.poll();
  }

}
