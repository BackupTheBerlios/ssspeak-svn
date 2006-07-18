package org.nargila.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.nargila.speak.framework.Conf;


/**
 * Simple wav player
 * @author tshalif
 *
 */
public class WavPlayer {
	
	/**
	 * source wav input path
	 */
	private final File m_wav;
	
	/**
	 * low-level player output
	 */
	private final SourceDataLine m_line;
	
	/**
	 * Delete after play flag
	 */
	boolean m_deleteAfterPlay;

	/**
	 * Play job runner thread
	 */
    private Thread m_runner = new Thread();

	private boolean m_noheader = true;
    
    /**
     * ctro with data to play from File
     * @param src wav input file
     * @throws UnsupportedAudioFileException
     * @throws IOException
     * @throws LineUnavailableException
     */
    public WavPlayer(File src) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    	this(src, AudioSystem.getAudioFileFormat(src).getFormat());
    	
    	m_noheader  = false;
    }
    
    /**
     * ctro with data to play from File and audio format
     * @param src wav input file
     * @param format format of given data
     * @throws UnsupportedAudioFileException
     * @throws IOException
     * @throws LineUnavailableException
     */
    public WavPlayer(File src, AudioFormat format) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    	m_wav = src;

    	DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
    	
    	m_line = (SourceDataLine) AudioSystem.getLine(info);    	
	}

    /**
     * mark file as deletable after play
     *
     */
    public void deleteAfterPlay() {
    	m_deleteAfterPlay = true;
    }

    /**
     * play file once
     * @return this
     */
	public WavPlayer play() {
		return play(-1);				
	}
	
	/**
	 * stop/abort playing
	 * @return this
	 */
	public WavPlayer stop() {
		m_runner.interrupt();

		synchronized (this) {
			m_line.close();
		}
		
		return this;
	}
	
	/**
	 * drain player
	 * @return this
	 */
	public synchronized WavPlayer drain() {
		m_line.drain();
		
		return this;
	}
	
	/**
	 * Play once
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	private synchronized void playOnce() throws IOException, UnsupportedAudioFileException {
			InputStream input = null;
				
			assert m_line.isOpen();
			
			notifyAll();

			if (m_noheader) {
				input = new FileInputStream(m_wav);
			} else {
				input = AudioSystem.getAudioInputStream(m_wav);
			}

			byte[] buff = new byte[4096];
			
			for (int len = input.read(buff);
				!m_runner.isInterrupted() && len > 0;
				len = input.read(buff)) {
				m_line.write(buff, 0, len);
			}
	}

	/**
	 * check either player is stopped
	 * @return true if stopped
	 */
	public boolean isStopped() {
		return !m_runner.isAlive();
	}
	
	/**
	 * start play loop every ms miliseconds.
	 * @param ms pause between each play loop - if bigger than zero
	 * @return this
	 */
	public synchronized WavPlayer play(final long ms) {
		stop();

		try {
			m_line.open();
			m_line.start();
			
			m_runner = new Thread("WavPlayer: " + m_wav) {
				
				public void run() {
					try {
						do {
							playOnce();
							
							if (ms > 0) {
								Thread.sleep(ms);
							} else {
								break;
							}
						} while (!m_runner.isInterrupted());
					} catch (IOException e) {
						throw new RuntimeException(e);
					} catch (UnsupportedAudioFileException e) {
						throw new RuntimeException(e);
					} catch (InterruptedException e) {
					} finally {				
						if (m_deleteAfterPlay && !Boolean.valueOf(Conf.getProperty("debug.keepFiles")).booleanValue()) {
							m_wav.delete();
						}
					}
				}
			};
			
			m_runner.start();
				
			wait();
		} catch (InterruptedException e) {
		} catch (LineUnavailableException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

//	@Override
	protected void finalize() throws Throwable {
		m_line.close();
		super.finalize();
	}
}
	
