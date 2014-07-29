package org.erasmusmc.concurrency;


/**
 * Thread class for processing data in batches. After construction, the thread will stay alive
 * until terminate() is called. When proceed() is called, the function process() is executed in the 
 * thread. waitUntilFinished() will wait until the thread is done executing process().
 * 
 * @author schuemie
 *
 */
public abstract class BatchProcessingThread extends Thread {
	private final Semaphore newInputSemaphore = new Semaphore();
	private final Semaphore newOutputSemaphore = new Semaphore();
  private boolean terminated = false;
  
	public BatchProcessingThread(){
		super();
		this.start();
		
	}
  public void run(){
    while (true){
    	newInputSemaphore.release();
      if (terminated)
      	break;
      process();
      newOutputSemaphore.take();
    }
  }
  
	protected abstract void process();
	
  public void proceed(){ //This method will be run in the other thread!
  	newInputSemaphore.take();
  }
   
  public void waitUntilFinished(){ //Runs in other thread!
  	newOutputSemaphore.release();
  }
  
  public synchronized void terminate(){//Runs in other thread!
    terminated = true;
    newInputSemaphore.take();
  }
  

}
