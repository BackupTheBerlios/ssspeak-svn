package jssspeak.util;


public abstract class AsyncJob implements Job, Runnable {
    int m_state = STATE_INITIAL;

    Thread m_runner = null;

    protected String m_name;
    
    private boolean m_startPaused;
    
    /**
     * run in-thread this runnable uppon successful (non abortive)
     * completion of doJob()
     */
    Runnable m_runAtEnd;
    
    public AsyncJob(String name) {
    	this(null, name);
    }
    
    public AsyncJob(Runnable runAtEnd, String name) {
    	m_runAtEnd = runAtEnd;
    	m_name = name;
    }
    
    public synchronized void start() {
    	start(false);
    }
    
    public synchronized void start(boolean paused) {
    	abort();
    	m_startPaused = paused;
    	m_state = STATE_INITIAL;
    	m_runner = new Thread(this, m_name);
    	m_runner.start();
       	checkState(); // this forces a wait until state becomes STATE_RUN
    }

    public int getState() {
        return m_state;
    }

    protected synchronized int changeState(int state) {
        int old_state = m_state;

        switch (m_state) {
        case STATE_INITIAL: // only allow changing from initial to running
        	if (state == STATE_RUN) {
        		if (m_startPaused) {
        			m_state = STATE_PAUSE;
        		} else {
        			m_state = state;
        		}
        	}
        	break;
        case STATE_FINISH:
        	if (state == STATE_DONE) {
        		m_state = state;
        	}
        case STATE_DONE:
        	break;
        default:
        	m_state = state;
        	break;
        }
        
        notifyAll();
        return old_state;
    }
    
    public synchronized void finish() {
    	//checkState();
        
       changeState(STATE_FINISH);

        while (m_state != STATE_DONE) {
            try {
                wait();
            } catch (InterruptedException ex) {
                return;
            }
        }
    }

    public synchronized void abort() {
        changeState(STATE_STOP);

        if (m_runner != null) {
            m_runner.interrupt();
        }
        
        if (STATE_INITIAL != getState()) {
        	while (STATE_DONE != getState()) {
        		try {
        			wait();
        		} catch (InterruptedException e) {
        		}
        	}
        }
    }
        
    public void pause() {
        checkNotShuttingDown();
        changeState(STATE_PAUSE);
    }

    public void resume() {
        checkNotShuttingDown();
        changeState(STATE_RUN);
    }

    public synchronized boolean isDone() {
        return m_state == STATE_DONE;
    }

    protected synchronized boolean checkState() {

        while (true) {
            switch (m_state) {
            case STATE_RUN:
            case STATE_FINISH:
                return true;
            case STATE_PAUSE:
            case STATE_INITIAL:
                try {
                    wait();
                } catch (InterruptedException ex) {
                    return false;
                }

                continue;
            case STATE_STOP:
            case STATE_DONE:
                return false;
            default:
                assert false: "HDIGH!";
                throw new RuntimeException("HDIGH!");
            }
        }
    }
        
    protected void checkNotShuttingDown() throws IllegalStateException {
        switch (m_state) {
        case STATE_STOP:
            throw new IllegalStateException("shutting down");
        case STATE_DONE:
            //throw new IllegalStateException("already finished");
        }
    }
        
    protected abstract void doJob();

    protected void completed() {}
    protected void b4completed() {}

    public void run() {
    	changeState(STATE_RUN);
    	
        try {
            doJob();
            
            if (checkState() && null != m_runAtEnd) {
           		m_runAtEnd.run();
            }
        } finally {
            b4completed();
            changeState(STATE_DONE);
            completed();
        }
    }
}
