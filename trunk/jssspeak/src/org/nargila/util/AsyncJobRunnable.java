package org.nargila.util;

import org.nargila.util.AsyncJob.JobControl;

public interface AsyncJobRunnable {
	
	public void run(JobControl controler) throws InterruptedException;

	public String getName();
}
