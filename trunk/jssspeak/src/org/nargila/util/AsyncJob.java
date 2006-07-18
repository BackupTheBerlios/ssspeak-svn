package org.nargila.util;

import java.util.logging.Logger;

public abstract class AsyncJob extends DefaultJob {

	public static class Counter {
		int m_count;
		final int min;
		final int max;
		
		Counter(int min, int max) {
			this.min = min;
			this.max = max;
		}
		
		synchronized int decr() {
			m_count = Math.max(min, m_count - 1);
			
			if (min == m_count) {
				notifyAll();
			}
			return m_count;
		}
		
		synchronized int incr() {
			m_count = Math.min(max, m_count + 1);

			if (max == m_count) {
				notifyAll();
			}
			return m_count;
		}

		public int getCount() {
			return m_count;
		}
		
	}
	
	public final class JobControl {
	   ThreadEvent running = new ThreadEvent(false);
	   ThreadEvent paused = new ThreadEvent(false);
	   
	   boolean aborted;    
	
	   boolean m_finishRequested;
	 
	   private JobControl() {}
	   
	   /**
	    * check job is not aborted and block until job is resume if paused. 
	    * @return true if not aborted - so check() can be placed in control loops
	    * @throws InterruptedException if calling thread is interrupted (aborted) 
	    */
	   public boolean check() throws InterruptedException {
	   		if (!aborted) {
	   			waitResumed();
	   			
	   			return true;
	   		}
	   		
	   		return false;
	    }
	
	    synchronized void reset() {
	    	running.setFlagged(false);
	    	paused.setFlagged(false);
	    	aborted = false;
	    	m_finishRequested = false;
	    	
	    	notifyAll();
	    }
	    
	    public final void abort() {
	    	
	    	synchronized (AsyncJob.this) {
	    		if (!aborted) {
	    			aborted = true;
	    			
	    			killJobs();
	    			onAbort();
	    		}
	    	}
	    	
	    	try {
				waitJobs();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    
	 	public boolean isDone() {
			return !running.isFlagged();
		}
		
		public void waitDone() throws InterruptedException {
			running.waitFlagged(false);
		}
		
		public boolean isPaused() {
			return !aborted && paused.isFlagged();
		}
		
		public void waitResumed() throws InterruptedException {
			if (!aborted) paused.waitFlagged(false);
		}
		
		public boolean isResumed() {
			return !aborted && !paused.isFlagged();
		}
	
		public void waitRunning() throws InterruptedException {
			if (!aborted) running.waitFlagged(true);
		}
	
		public boolean isRunning() {
			return running.isFlagged();
		}
	
		public boolean isAborted() {
			return aborted;
		}
		
		public synchronized void requestFinish() {
			m_finishRequested = true;
			
			notifyAll();
		}
		
		public boolean finishRequested() {
			return m_finishRequested;
		}
	}

	static Logger logger = Logger.getLogger(AsyncJob.class.getName()); 

	private JobControl m_jobControl = new JobControl();
	
    private Thread[] m_jobThreads = new Thread[0];
    private AsyncJobRunnable[] m_jobs = new AsyncJobRunnable[0];
    
    private Counter m_runningCount;
    
    public AsyncJob(String name) {
    	this(name, (AsyncJobRunnable[])null);
    }
    
    private void waitJobs() throws InterruptedException {
       	for (int i = 0; i < m_jobThreads.length; ++i) {
    		Thread jobThread = m_jobThreads[i];
    		
    		if (jobThread != null && jobThread.isAlive()) {
    			jobThread.join();
    		}
    	}
		
	}

	public AsyncJob(String name, AsyncJobRunnable job) {
    	this(name, new AsyncJobRunnable[]{job});
    }
    
    public AsyncJob(String name, AsyncJobRunnable[] jobs) {
    	setId(name);
    	
    	if (null != jobs) {
    		setJobs(jobs);
    	}
    }
    
    public synchronized void start() {
    	start(false);
    }
    
    public synchronized void start(boolean paused) {
    	   	
    	if (0 == m_jobs.length) {
    		throw new IllegalStateException("AsyncJob has no jobs to run");
    	}
    	
    	if (!m_jobControl.isDone()) {
    		
    		throw new IllegalStateException("AsyncJob is already running - call abort() first");    		     		
    		
    	}
    	
    	m_runningCount = new Counter(0, m_jobs.length);
    	
    	m_jobControl.reset();
    	
    	m_jobControl.paused.setFlagged(paused);
    	
    	final Object startBarrier = "";
    	
    	
    	synchronized (m_runningCount) {
    		for (int i = 0; i < m_jobs.length; ++i) {
    			
    			final AsyncJobRunnable job = m_jobs[i];
    			
    			final String name = m_jobs[i].getName();
    			
    			m_jobThreads[i] = new Thread(new Runnable() {
    				public void run() {
    					System.out.println("at barrier: " + name);
    					
    					synchronized (startBarrier) {

    						m_runningCount.incr();
    						
    						try {
    							startBarrier.wait();
    						} catch (InterruptedException e) {
    							// e.printStackTrace();
    						}
    					}
    					
    					System.out.println("after barrier: " + name);
    					
    					try {
    						job.run(m_jobControl);
    					} catch (InterruptedException e) {
    					}
    					
    					System.out.println("finished: " + name);
    					
    					m_runningCount.decr();
    					
    					synchronized (this) {
    						notifyAll();
    					}
    					
    					synchronized (AsyncJob.this) {
    						if (m_runningCount.getCount() == 0) {
    							System.out.println("shutting down: " + name);
    							    							
    							onDone();

    							m_jobControl.running.setFlagged(false);
    						}
    					}    							
    				}
    			}, job.getName() + ": " + getId());
    			
    			m_jobThreads[i].start();    				
    		}
    		
    	    try {
				m_runningCount.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	
    	assert m_runningCount.getCount() == m_jobs.length: "HDIGH: running count does no match job count";
    	
    	
    	onStart();
    	
    	m_jobControl.running.setFlagged(true);

    	synchronized (startBarrier) {
    		startBarrier.notifyAll();
    	}
    }
    
    
    

    public final void finish() {
    	m_jobControl.requestFinish();
    	
    		try {
				m_jobControl.waitDone();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    }

    /* (non-Javadoc)
	 * @see org.nargila.util.JobControl#abort()
	 */
    public final void abort() {
    	m_jobControl.abort();
    }

    protected void onAbort() {}
    protected void onStart() {}
    protected void onDone() {}
    
    private void killJobs() {
    		
    	for (int i = 0; i < m_jobThreads.length; ++i) {
    		Thread jobThread = m_jobThreads[i];
    		
    		if (jobThread != null && jobThread.isAlive()) {
    			jobThread.interrupt();
    		}
    	}
    }

   
    public void pause() {
    	m_jobControl.paused.setFlagged(true);
    }

    public void resume() {
       	m_jobControl.paused.setFlagged(false);
    }

    public boolean isDone() {
    	return m_jobControl.isDone();
    }

     public synchronized void setId(Object id) {
    	super.setId(id);
    	
    	for (int i = 0; i < m_jobThreads.length; ++i) {
    		Thread jobThread = m_jobThreads[i];
    		
    		AsyncJobRunnable job = m_jobs[i];
    		
    		if (null != jobThread) {
    			jobThread.setName(job.getName() + ": " + id.toString());
    		}
    	}
    }

	public JobControl getJobControl() {
		return m_jobControl;
	}

	public AsyncJobRunnable[] getJobs() {
		return m_jobs;
	}

	public void setJobs(AsyncJobRunnable[] jobs) {
		m_jobs = jobs;
		
    	m_jobThreads = new Thread[jobs.length];    	
	}

	public Counter getRunningCount() {
		return m_runningCount;
	}
}
