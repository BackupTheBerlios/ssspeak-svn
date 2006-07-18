package org.nargila.util;


import org.nargila.util.AsyncJob.JobControl;

import junit.framework.TestCase;

public class AsyncJobTest extends TestCase {
    class Value {
        int i;
    }
    
    AsyncJob makeJob(final Value val) {
        AsyncJobRunnable job = new AsyncJobRunnable() {
        	public String getName() {
        		return "increment task";
        	}
        	public void run(JobControl controler)  throws InterruptedException  {
        		val.i = 0;
        		while (controler.check() && ++val.i < 10) {
        			
        			
        			System.out.println(val.i);
        			
        			try {
        				Thread.sleep(1000);
        			} catch (InterruptedException e){
        				break;
        			} // end of try-catch
        		}
        	}
       };
        
       AsyncJob runner = new AsyncJob("AsyncJob", job) {
    	   protected void onAbort() {}

		protected void onStart() {
			
		}

		protected void onDone() {
			
		}
    	   
       };
       
       runner.start();
       
       return runner;
    }
    
    public void testAbort() {
        Value val = new Value();

        Job job = makeJob(val);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e){
        } // end of try-catch

        job.abort();
        
        System.out.println("i=" + val.i);
        
        assertTrue(val.i < 10);
    }
    
    public void testFinish() {
        Value val = new Value();

        Job job = makeJob(val);

        job.finish();
        
        System.out.println("i=" + val.i);
        
        assertTrue(val.i == 10);
    }
    
    public void testPause() {
        Value val = new Value();

        final Job job = makeJob(val);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e){
        } // end of try-catch


        job.pause();

        System.out.println("i=" + val.i);
        
        final int pauseMs = 5000;
        
        System.out.println("paused - will resume in " + pauseMs / 1000 + " seconds");

        assertTrue(val.i < 10);
        
        new Thread() {
        	public void run() {

                    try {
						Thread.sleep(pauseMs);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
 
                System.out.println("resumed");
                job.resume();
        	}
        }.start();

        job.finish();

        System.out.println("i=" + val.i);
        
        assertTrue(val.i == 10);
    }
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.run(AsyncJobTest.class);
    }
}

