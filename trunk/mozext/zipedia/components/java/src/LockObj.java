import java.util.Set;
import java.util.HashSet;

public class LockObj {
    private int m_readers = 0;
    private boolean m_monitorEntered = false;
    private boolean m_writeLock = false;


    private void trace(String msg) {
	System.out.println(msg);
    }

    synchronized public void readUnlock() {
	trace("readUnlock() <<");
	if (m_writeLock) {
	    throw new RuntimeException("HDIGH: how come both read and write locks are active?");
	}
	
	if (m_readers <= 0) {
	    throw new IllegalStateException("read lock is not locked");
	}
	
	m_readers--;

	notifyAll();

	trace("readUnlock() >>");
    }

    synchronized public void readLock() {
	trace("readLock() <<");
	while (m_writeLock) {
	    try {
		wait();
	    } catch (InterruptedException e) {}
	}
	
	m_readers++;

	trace("readLock() >>");
    }

    synchronized public void writeLock() {
	trace("writeLock() <<");
	while (m_readers > 0 || m_writeLock) {
	    try {
		wait();
	    } catch (InterruptedException e) {}
	}

	m_writeLock = true;
	trace("writeLock() >>");
    }
    
    synchronized public void writeUnlock() {
	trace("writeUnlock() <<");
	if (!m_writeLock) {
	    throw new IllegalStateException("write lock is not set");
	}

	m_writeLock = false;

	notifyAll();
	trace("writeUnlock() >>");
    }

    synchronized public void exitMonitor() {
	trace("exitMonitor() <<");

	if (!m_monitorEntered) {
	    throw new IllegalStateException("exitMonitor() called without first entering");
	}

	m_monitorEntered = false;

	notifyAll();

	trace("exitMonitor() >>");
    }

    synchronized public void enterMonitor() {
	trace("enterMonitor() <<");
	while (m_monitorEntered) {
	    try {
		wait();
	    } catch (InterruptedException e) {}
	}
	
	m_monitorEntered = true;

	trace("enterMonitor() >>");
    }
}
	