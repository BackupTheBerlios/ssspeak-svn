package jssspeak.util;

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

import jssspeak.Conf;
import jssspeak.player.impl.rawplayer.RawAudioFormat;


public class WavPlayer {
	final File m_wav;
	final SourceDataLine m_line;
	boolean m_loop = false;
	AudioFormat m_format;
	boolean m_stopped = true;
    boolean m_deleteAfterPlay;
	
    Object m_onePlayBlock = new Object();
    
    public WavPlayer(File src) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    	this(src, AudioSystem.getAudioFileFormat(src).getFormat());
    }
    
    public WavPlayer(File src, AudioFormat format) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    	m_wav = src;
    	m_format = format;
    	
    	DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
    	
    	m_line = (SourceDataLine) AudioSystem.getLine(info);
    	
    	m_line.open();
    	
    	
	}

    public void deleteAfterPlay() {
	m_deleteAfterPlay = true;
    }

	public WavPlayer play() {
		return play(-1);				
	}
	
	public synchronized WavPlayer stop() {
		
			m_loop = false;
			m_stopped = true;
			m_line.stop();
		
		return this;
	}
	
	public WavPlayer drain() {
		synchronized (m_onePlayBlock) {		
			m_line.drain();
		}	
		
		
		return this;
	}
	
	public synchronized boolean isStopped() {
			return m_stopped;
	}
	
	public synchronized WavPlayer play(final long ms) {
						
		final Object syncObj = "";

		synchronized (syncObj) {
			stop();

			m_loop = -1 == ms ? false : true;
			m_stopped = false;
			m_line.start();
			
			
			new Thread("WavPlayer") {
				
				public void run() {
					try {
						do {
														
							InputStream input = null;
							
							if (m_format instanceof RawAudioFormat) {
								input = new FileInputStream(m_wav);
							} else {
								input = AudioSystem.getAudioInputStream(m_wav);
							}
							
							synchronized (m_onePlayBlock) {
								synchronized (syncObj) {
									syncObj.notifyAll();
								}

								byte[] buff = new byte[4096];
								for (int len = input.read(buff);
								     len > 0;
								     len = input.read(buff)) {
								    if (m_stopped) return;
								    m_line.write(buff, 0, len);
								} 
							}
							
							if (ms > 0) {
								try {
									Thread.sleep(ms);
								} catch (InterruptedException e) {
									return;
								}
							}
						} while (m_loop);
						
					} catch (UnsupportedAudioFileException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {				
						if (m_deleteAfterPlay && !Boolean.valueOf(Conf.getProperty("debug.keepFiles")).booleanValue()) {
							m_wav.delete();
						}
					}
				}
			}.start();
						
			try {
				syncObj.wait();
			} catch (InterruptedException e1) {
			}
		}
		
		return this;
	}

//	@Override
	protected void finalize() throws Throwable {
		m_line.close();
		super.finalize();
	}
}
	
