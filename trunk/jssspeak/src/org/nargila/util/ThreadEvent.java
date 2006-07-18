package org.nargila.util;

/**
 * Thread event class inpired by libcommonc++ - now available in java 1.5 
 * @author tshalif
 *
 */
public class ThreadEvent {
	private boolean flagged;
		
	public ThreadEvent(boolean flagged) {
		this.flagged = flagged;
	}
	
	public synchronized void waitFlagged(boolean condition) throws InterruptedException {
		while (flagged != condition) {
			wait();
		}
	}
	
	public synchronized void setFlagged(boolean flagged) {
		this.flagged = flagged;
		notifyAll();
	}

	public boolean isFlagged() {
		return flagged;
	}
}
