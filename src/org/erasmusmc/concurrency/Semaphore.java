package org.erasmusmc.concurrency;

public class Semaphore {
  private boolean signal = false;

  public synchronized void take() {
    this.signal = true;
    this.notify();
  }

  public synchronized void release() {
    while(!this.signal)
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    this.signal = false;
  }

}
