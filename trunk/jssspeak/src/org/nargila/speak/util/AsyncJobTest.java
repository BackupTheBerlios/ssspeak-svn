package jssspeak.util;


import junit.framework.TestCase;

public class AsyncJobTest extends TestCase {
    class Value {
        int i;
    }
    
    Job makeJob(final Value val) {

        AsyncJob job = new AsyncJob("test") {
                public void doJob() {
                    val.i = 0;
                    while (++val.i < 10) {
                        if (!checkState()) {
                            break;
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e){
                            break;
                        } // end of try-catch
                    }
                }
                protected void completed() {
                    synchronized (val) {
                        val.notify();
                    }
                }
            };

        job.start();

        return job;
    }

    public void testRun() {
        Value val = new Value();

        synchronized (val) {
            makeJob(val);

            try {
                val.wait();
            } catch (InterruptedException e){
            } // end of try-catch
        }

        System.out.println("i=" + val.i);
        
        assertTrue(val.i == 10);
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

        Job job = makeJob(val);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e){
        } // end of try-catch


        job.pause();

        System.out.println("i=" + val.i);
        
        assertTrue(val.i < 10);
        
        job.resume();

        job.finish();

        System.out.println("i=" + val.i);
        
        assertTrue(val.i == 10);
    }
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.run(AsyncJobTest.class);
    }
}

