package org.nargila.speak.framework.player;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;

import org.nargila.speak.event.player.PlayerEndEvent;
import org.nargila.speak.event.player.PlayerOffsetEvent;
import org.nargila.speak.event.player.PlayerPausedEvent;
import org.nargila.speak.event.player.PlayerResumedEvent;
import org.nargila.speak.event.player.PlayerSeekEvent;
import org.nargila.speak.event.player.PlayerStartEvent;
import org.nargila.speak.event.player.PlayerStopEvent;
import org.nargila.speak.framework.Conf;
import org.nargila.util.AsyncJob;
import org.nargila.util.AsyncJobRunnable;
import org.nargila.util.EventHandler;
import org.nargila.util.EventHandlerManager;


/**
 * Generic implementation of the Player interface. 
 * Implementation is provided for most of the Player
 * methods, except for the most low-level operations such
 * as write, etc. The lower level operations are delegated
 * to a PlayerOps object which is more implementation
 * dependant.
 * 
 * @see org.nargila.speak.framework.player.PlayerOps
 * @see org.nargila.speak.framework.player.Player
 * @author tshalif
 *
 */
public class PlayerImpl extends AsyncJob implements Player {
	static Logger logger = Logger.getLogger(PlayerImpl.class.getName()); 

	/**
	 * On-disk file to hold the speech audio data.
	 */
	private RandomAccessFile m_wavDataFile;

    //private final SortedSet<Long> m_markOffsets = new TreeSet<Long>();
	/**
	 * Set of mark offsets to be fired when given play pos is reached.
	 */
    private final SortedSet m_markOffsets = new TreeSet();

    /**
     * Flag signifying no more speech audio data is expected from synthsis engine.
     */
    private boolean m_dataEnd;
    //private Set<Object> m_disableRequests = new HashSet<Object>();
    
    /**
     * bag of objects requesting player silense. Only
     * when it empty will sound output be enaled. 
     */
    private Set m_disableRequests = new HashSet();
    
    /**
     * pointer to current data play pos - may be different
     * to actual player play pos because of latency.
     */
    private long m_dataPos;
    
    /**
     * The temporary File object for the audio data.
     * @see #m_wavDataFile
     */
    private File m_tmpFile;
    
    /**
     * The low level player operation implementation
     */
    private final PlayerOps m_playerOps;
    
    /**
     * Specialized Thread job for firing player events. 
     * @author tshalif
     *
     */
    class EventLoopJob implements AsyncJobRunnable {
    	
		public void run(JobControl controler) throws InterruptedException {
			findAndFireMarkEvents(controler);
		}

		public String getName() {
			return "event loop";
		}

		/**
		 * Fire mark events correstponding to current play position.
		 * This method is meant to be ran in a loop, to send mark events
		 * back to the mark listener near as possible to the time their
		 * position is actually played. The play position and mark offsets are
		 * in bytes from the beginning of the play stream.
		 */
		private void findAndFireMarkEvents(JobControl controler)  throws InterruptedException  {
		
			if (m_listenerManager.isEmpty()) {
				return;
			}
			
			/*
			 * run as long as play loop is also alive and player not aborted 
			 */
			while (getRunningCount().getCount() > 1 && controler.check()) {
				
				Thread.yield();
				
				long old_pos = -1;
				long new_pos = -1;
				/*
				 * Get the play interval since this method was last called 
				 */
				synchronized (m_playPosSyncObj) {
					old_pos = m_lastEventPos;
					new_pos = getPlayPos();
				}
				
				if (old_pos < new_pos) {
					ArrayList markList = null;
					
					synchronized (m_markOffsets) {
						/*
						 * get a list of marks occuring between old_pos and current m_playPos
						 */
						//Iterator<Long> i = m_markOffsets.subSet(old_pos, m_playPos).iterator();
						
						Set markRange = m_markOffsets.subSet(new Long(old_pos), new Long(new_pos));
						
						markList = new ArrayList(markRange);
					}
					
					Iterator i = markList.iterator();
					
					while (i.hasNext() && !controler.isAborted() && !controler.isPaused()) {
						long markOffset = ((Long)i.next()).longValue();
						
						m_listenerManager.generateEvent(new PlayerOffsetEvent(getId(),markOffset));
					}
		        
					synchronized (m_playPosSyncObj) {
						if (old_pos != m_lastEventPos) { // skip/seek operation took place
							continue;
						}
						
						m_lastEventPos = new_pos;
						 
					}
					/*
					 * check if need to generate end of play event
					 */
					if (!controler.isAborted() && !controler.isPaused() && m_lastEventPos == getLength() && !isGrowing()) {
						m_playerOps.drain();
						
						m_listenerManager.generateEvent(new PlayerEndEvent(getId()));
					}
				}
			}
		}
			
    }
    
    
    /**
     * Specialized Thread job for feeding audio data to low level player.
     * @author tshalif
     *
     */
    class PlayLoopJob implements AsyncJobRunnable  {


		public String getName() {
			return "play loop";
		}		
		
		public void run(JobControl controler)  throws InterruptedException  {
			
			assert null != m_wavDataFile: "HDIGH: how come data file is not set up yet?";
			
			
			try {
				enableSound(PlayerImpl.this);
				
				int buffSize = Player.WRITE_GRANULARITY;
				
				byte[] buff = new byte[buffSize];
				
				int readCount;			
				boolean end = false;
				
				while (controler.check() && !(end && !isGrowing() && controler.finishRequested())) {
					Thread.yield();
				
		    	   	synchronized (m_playPosSyncObj) {   
		    	   		end = getPlayPos() >= getLength(); 
		    	   		
		    	   		if (!controler.isResumed()) {
		    	   			continue;
		    	   		}
		    	   		
						if (end && !controler.finishRequested())  {
							m_playPosSyncObj.wait();
							continue;
						}
		    	   	}				
		    	   	
					synchronized (m_wavDataFile) {
						
						m_wavDataFile.seek(m_dataPos);
						
						readCount = m_wavDataFile.read(buff);
					}    				
					
					if (readCount > 0) {
						
						if (checkSeek() || !controler.isResumed()) {
							continue;
						}
						
						int count = m_playerOps.write(buff, readCount);
						
						m_dataPos += count; 
					}		
				}
			} catch (IOException e) {
				if (!controler.isAborted())
					throw new RuntimeException(e);
			} catch (PlayerException e) {
				if (!controler.isAborted())
					throw new RuntimeException(e);
			}
		}};
    
	/**
	 * Sound silenser object during pause
	 */
    static final Object DISABLE_SOUND_PAUSED = new Object();
    
    /**
     * player event listener manager
     */
    private EventHandlerManager m_listenerManager = new EventHandlerManager();

    /**
     * Position mark for last event loop run.
     */
 	private long m_lastEventPos;

 	/**
 	 * Object used for any critical section synchronization related to play position.
 	 */
	private Object m_playPosSyncObj;

	/**
	 * Handy object to synchronized around seek requests.
	 * @author tshalif
	 *
	 */
	private static class SeekFlagged {
		boolean requested;
	}
	
	/**
	 * Handy object to synchronized around seek requests.
	 */ 
	private SeekFlagged m_seekFlaggedSyncObj = new SeekFlagged();

	public PlayerImpl() {
		this("PlayerImpl");
	}
	
	public PlayerImpl(String name) {
    	super(name);
    	
    	m_playerOps = PlayerOpsFactory.getInstance();
    	
    	m_playerOps.setId(name);
    	
    	setJobs(new AsyncJobRunnable[]{
    			new PlayLoopJob(),
    			new EventLoopJob()
    	});
    	
    	m_playPosSyncObj = getJobControl();
    }
    
    /* (non-Javadoc)
	 * @see jssspeak.player.Player#addPosMark(long)
	 */
    public void addPosMark(long off) {
    	synchronized (m_markOffsets) {
    		m_markOffsets.add(new Long(off));
    	}
    }

    /* (non-Javadoc)
	 * @see jssspeak.player.Player#getPlayPos()
	 */
    public long getPlayPos() {
    	return m_dataPos - m_playerOps.getLatency();
    }

    public long secondsToBytes(int sec) {
        return (long)(sec * m_playerOps.getAudioFormat().getSampleRate() * m_playerOps.getAudioFormat().getFrameSize());
    }

    int bytesToSeconds(long bytes) {
    	return (int)(bytes / m_playerOps.getAudioFormat().getSampleRate() / m_playerOps.getAudioFormat().getFrameSize());
    }
    

    /* (non-Javadoc)
	 * @see jssspeak.player.Player#seek(long)
	 */
    public synchronized void seek(long offset)  throws PlayerException {
    	
    	synchronized (m_playPosSyncObj) {
    		m_playerOps.flush();
    		
    		m_dataPos = m_lastEventPos = Math.min(getLength(), Math.max(offset, 0));
    		
    		m_seekFlaggedSyncObj.requested = true;

    		if (m_lastEventPos == getLength()) { // put m_lastEventPos a little behind end-of-file to triger end of play event
    			m_lastEventPos -= getAudioFormat().getFrameSize();
    		}
    		
    		m_playPosSyncObj.notifyAll();
    	}
    	
    	m_listenerManager.generateEvent(new PlayerSeekEvent(this, offset));
    }
    
	/* (non-Javadoc)
	 * @see jssspeak.player.Player#skip(int)
	 */
	public void skip(int seconds) throws PlayerException{
   	
		long skipBytes = secondsToBytes(seconds);
   		
   		seek(getPlayPos() + skipBytes);
    }
    
	/**
	 * Check either seek operation has been requested
	 * @return
	 */
	private boolean checkSeek() {
		synchronized (m_playPosSyncObj) {
			boolean retval = m_seekFlaggedSyncObj.requested;
			
			m_seekFlaggedSyncObj.requested = false;

			return retval;
		}		
	}

	/**
	 * Get output stream for feeding data into this player.
	 */
	public OutputStream outputStream() {
		synchronized (m_playPosSyncObj) {
			m_dataEnd = false;
		}
		
		return new OutputStream() {
//			@Override
			public void close() throws IOException {

				synchronized (m_playPosSyncObj) {
		    	   	m_dataEnd = true;
		    	   	m_playPosSyncObj.notifyAll();
				}
			}
			
//			@Override
			public void write(byte[] b, int off, int len) throws IOException {
			    RandomAccessFile wavDataFile = m_wavDataFile;

			    if (null != wavDataFile) {
			    	synchronized (wavDataFile) {
			    		m_wavDataFile.seek(m_wavDataFile.length());
			    		m_wavDataFile.write(b, off, len);
			    	}
			    	
			    	synchronized (m_playPosSyncObj) {
			    		m_playPosSyncObj.notifyAll();
			    	}
			    }
			}
			
//			@Override
			public void write(int b) throws IOException {
				write(new byte[]{(byte)b}, 0, 1);
			}
		};
	}
	
    /* (non-Javadoc)
	 * @see jssspeak.player.Player#getProgress()
	 */
    public int getProgress() {
    	int res = 0;
    	synchronized (m_playPosSyncObj) {
    		if (null != m_wavDataFile) {    			
    			
    			try {
    				res = (int)((float)getPlayPos() / m_wavDataFile.length() * 100);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    	return Math.min(res, 100);
    }

	public synchronized void pause() {
		super.pause();
		disableSound(DISABLE_SOUND_PAUSED);
    }
	
    public synchronized void resume() {
    	super.resume();
    	enableSound(DISABLE_SOUND_PAUSED);
    }

    protected void onDone() {
     	if (!getJobControl().isAborted()) {
   			m_playerOps.drain();
   		}

		if (null == m_wavDataFile) {
			return;
		}
		
		synchronized (m_wavDataFile) {
			try {
				m_wavDataFile.close();
			} catch (IOException e) {
			}
			
			if (!Boolean.valueOf(Conf.getProperty("debug.keepFiles")).booleanValue()) {
				m_tmpFile.delete();
			}
		}
		
   		m_dataPos = 0;
		m_dataEnd = false;
		
		m_markOffsets.clear();		
		m_wavDataFile = null;
		
		
		m_playerOps.abort();
		
		if (null != m_tmpFile && !Boolean.valueOf(Conf.getProperty("debug.keepFiles")).booleanValue()) {
			m_tmpFile.delete();
		}	
		
		m_listenerManager.generateEvent(new PlayerStopEvent(getId()));
    }
    
    protected void onAbort() {
    }
    
    protected void finalize() throws Throwable {
		onDone();
	}

	/* (non-Javadoc)
	 * @see jssspeak.player.Player#isGrowing()
	 */
	public boolean isGrowing() {
		return !m_dataEnd;
	}
	
	/* (non-Javadoc)
	 * @see jssspeak.player.Player#getLength()
	 */
	public long getLength() {
		RandomAccessFile wavDataFile = m_wavDataFile;
		
		if (null != wavDataFile) {
			synchronized (wavDataFile) {
				try {
					return wavDataFile.length();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see jssspeak.player.Player#disableSound()
	 */
	public void disableSound(Object id) {
		synchronized (m_disableRequests) {
			m_disableRequests.add(id);
			m_playerOps.pause();
			
			m_listenerManager.generateEvent(new PlayerPausedEvent(getId()));
		}
	}
	/* (non-Javadoc)
	 * @see jssspeak.player.Player#enableSound()
	 */
	public void enableSound(Object id) {
		synchronized (m_disableRequests) {
			m_disableRequests.remove(id);
			
			if (m_disableRequests.isEmpty() && !getJobControl().isPaused()) {				
				m_playerOps.resume();
				
				m_listenerManager.generateEvent(new PlayerResumedEvent(getId()));
				
			}
		}
	}
	
	protected void onStart() {
		m_listenerManager.generateEvent(new PlayerStartEvent(getId()));			
	}
	
	public void open(AudioFormat format) throws PlayerException {
		abort();
		
		synchronized (this) {
			
			logger.info(Thread.currentThread().getName());
			
			
			m_playerOps.open(format);
			
			try {
				m_tmpFile = File.createTempFile("MyRawPlayer", ".wav");
				
				if (!Boolean.valueOf(Conf.getProperty("debug.keepFiles")).booleanValue()) {
					m_tmpFile.deleteOnExit();
				}
				
				m_wavDataFile = new RandomAccessFile(m_tmpFile, "rws");
				
			} catch (Exception e) {
				throw new PlayerException(e);
			}
			
			start();
		}
	}

	public void addPlayListener(EventHandler playListener) {
		m_listenerManager.addHandler(playListener);
	}

	public void removePlayListener(EventHandler playListener) {
		m_listenerManager.removeHandler(playListener);
	}


	public AudioFormat getAudioFormat() {
		return m_playerOps.getAudioFormat();
	}

	public boolean isEnabledSound(Object id) {
		synchronized (m_disableRequests) {
			return !m_disableRequests.contains(id);
		}
	}
}