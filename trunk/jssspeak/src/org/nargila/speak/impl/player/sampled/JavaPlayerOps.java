/**
 * javax.sampled based low-level player 
 */
package org.nargila.speak.impl.player.sampled;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.nargila.speak.framework.player.PlayerException;
import org.nargila.speak.framework.player.PlayerOps;
import org.nargila.util.DefaultJob;

/**
 * javax.sampled based implementation of PlayerOps
 * @author tshalif
 *
 */
public class JavaPlayerOps extends DefaultJob implements PlayerOps {

	/**
	 * the output player line
	 */
	private SourceDataLine m_line;

	public void open(AudioFormat format) throws PlayerException {
        DataLine.Info  info = new DataLine.Info(SourceDataLine.class, format);
        try {
			m_line = (SourceDataLine) AudioSystem.getLine(info);
			m_line.open();
		} catch (LineUnavailableException e) {
			throw new PlayerException(e);
		}
		
		m_line.start();
	}
	
	public int write(byte[] data, int length) throws PlayerException {
		return m_line.write(data, 0, length);
	}

	public AudioFormat getAudioFormat() {
		return m_line.getFormat();
	}

	public int getLatency() {
		if (null != m_line && m_line.isRunning()) {
			return m_line.getBufferSize() - m_line.available();
		}
		
		return 0;
	}

	public void finish() {
		m_line.drain();
		m_line.close();
	}

	public void abort() {
		if (null != m_line) {
			m_line.flush();
			m_line.close();
		}
	}

	public void pause() {
		if (null != m_line) {
			m_line.stop();
		}
	}

	public void resume() {
		if (null != m_line) m_line.start();
	}

	public void start() {
		if (null != m_line) m_line.start();
	}

	public void flush() {
		if (null != m_line) m_line.flush();
	}

	public void drain() {
		if (null != m_line) m_line.drain();
	}

	public boolean isPlaying() {
		if (null != m_line) {
			return m_line.isRunning();
		}
		
		return false;
	}
}
