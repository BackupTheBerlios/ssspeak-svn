package org.nargila.speak.framework.synth;

import java.io.IOException;

import org.nargila.speak.event.synth.SynthesizerSeekEndEvent;
import org.nargila.speak.event.synth.SynthesizerSeekStartEvent;
import org.nargila.speak.framework.player.Player;
import org.nargila.speak.synth.SynthesisException;

/**
 * SynthesizerImpl helper to manage skipping and seeking. The two types of
 * skipping supported are skipping in seconds (positive for or negative) in the
 * audio stream or to the position of an SSML mark. Request for skipping
 * to yet unresolved mark or yet unsynthesized audio offset, will triger an
 * asynchroneous pending skip job.
 *   
 * @author tshalif
 *
 */
class SkipManager {
	
	/**
	 * Specialized pending skip thread job class.
	 * @author tshalif
	 *
	 */
	private class PendingSkip extends Thread {
		final MarkPos pendingMark;
		final Object syncObj = "";
		boolean aborted;
		
		PendingSkip() {
			this(new MarkPos(null, -1));
		}
		
		PendingSkip(MarkPos pendingMark) {
			super("PendingSkip: " + pendingMark.name);
			this.pendingMark = pendingMark;	
		}
		
		public synchronized void abort() {
			aborted = true;
			interrupt();
		}
		
		public void start() {
			m_synthesizer.disableSound(this);
			
			Object seekPos = (pendingMark.pos == -1) ? (Object)pendingMark.name : (Object)new Long(pendingMark.pos);
			
			m_synthesizer.generateEvent(new SynthesizerSeekStartEvent(m_synthesizer.getId(), seekPos));

			synchronized (syncObj) {
				if (pendingMark.pos != -1 && m_synthesizer.getPlayer().getLength() >= pendingMark.pos) {
					/*
					 * Skip thread cost and latency if offset position already available
					 */
					run();
				} else {
					super.start();
				
					try {
						syncObj.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		public void run() {
			try {
				synchronized (syncObj) {
					syncObj.notifyAll();
				}
				
				Player player = m_synthesizer.getPlayer();
				
				do {
					if (-1 != pendingMark.pos) { // skipping beyond player data stream
						while (player.getLength() < pendingMark.pos && player.isGrowing()) {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								return; // interrupted
							}
						}
					} else { // skipping to yet-unresolved mark (Sable2Wav is still running, wav file growing
						synchronized (pendingMark) {
							if (pendingMark.pos == -1 && player.isGrowing()) {						
								
								try {
									pendingMark.wait();
								} catch (InterruptedException e) {
									return; // pending skip cancelled
								}
							}
						}
					}
					
					if (-1 == pendingMark.pos) { // skip mark not found
						break;
					} else if (pendingMark.pos > player.getLength() && player.isGrowing()) { // send back to wait loop
						continue; 
					} else { // resolved: seek player
						player.seek(pendingMark.pos);
						break;
					}
				} while (true);
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				abort(); // mark this pending job as invalid
				m_synthesizer.enableSound(this);
				m_synthesizer.generateEvent(new SynthesizerSeekEndEvent(m_synthesizer.getId(), pendingMark.pos));
			}
		}
	}
	
	/**
	 * A pending skipping job
	 */
	private PendingSkip m_pendingSkip = new PendingSkip();
	
	/**
	 * owner synthesizer object
	 */
	private SynthesizerImpl m_synthesizer;
	
	/**
	 * ctro with owner object
	 * @param synthesizer our owner
	 */
	SkipManager(SynthesizerImpl synthesizer) {
		m_synthesizer = synthesizer;
	}
	
	/**
	 * cancell pending skip job
	 *
	 */
	synchronized void cancelSkip() {
		synchronized (m_pendingSkip) {
			m_pendingSkip.abort();
		}		
	}
	
	/**
	 * Start a skip job. If pedingMark.pos is resolved (i.e. not -1)
	 * and within range of audio stream, skip action is done synchronously. Else,
	 * the pending skip runs in own thread asynchronously.
	 * 
	 * @param pendingMark mark or pos to skip to
	 * @throws IOException
	 */
	private void skip(MarkPos pendingMark) throws IOException {
		synchronized (m_pendingSkip) {
			synchronized (pendingMark) {
				m_pendingSkip = new PendingSkip(pendingMark);
				m_pendingSkip.start();
				
				return;
			}
		}
	}
	
	/**
	 * seek to given position in audio stream. If offset is 
	 * not yet synthesized (i.e. SynthesizerEngine is still working)
	 * a pending skip is started.
	 * 
	 * @param offset speech audio offset to seek to
	 * @throws SynthesisException
	 */
	void seek(long offset) throws SynthesisException {
		cancelSkip();
		
		if (-1 != offset) {
				try {
					skip(new MarkPos(null, offset));
				} catch (IOException e) {
					throw new SynthesisException(e);
				}
		}
	}
	
	/**
	 * Skip by so many given seconds in the speech audio stream. 
	 * @param sec positive for skipping forward, negative for skipping back
	 * @throws SynthesisException
	 */
	void skip(int sec)  throws SynthesisException {
		cancelSkip();
		
		
		long pos = m_synthesizer.getPlayer().getPlayPos() + 
			m_synthesizer.getPlayer().secondsToBytes(sec);
		
		seek(pos);
	}
	
	/**
	 * Skip to the offset of a given SSML mark. If mark pos is not
	 * yet resolved, a pending skip job is started.
	 * 
	 * @param mark SSML mark to skip to
	 * @throws SynthesisException
	 */
	void skip(String mark)  throws SynthesisException {
		cancelSkip();
		
		MarkPos markPos = m_synthesizer.getMarkManager().getMarkPos(mark);
		
		if (markPos != null) {
			try {
				skip(markPos);
			} catch (IOException e) {
				throw new SynthesisException(e);
			}
		}
	}
	
}
