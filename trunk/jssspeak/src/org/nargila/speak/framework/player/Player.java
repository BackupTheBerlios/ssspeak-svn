/**
 * Player framework usable by a serial synthesizer implementation
 */
package org.nargila.speak.framework.player;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;

import org.nargila.util.EventHandler;
import org.nargila.util.Job;

/**
 * A Player usable by a serial synthesizer.
 * A serial synthesizer produces raw audio output
 * of a given AudioFormat and feeds it to the player so
 * player can start playing while the synthesis engine is
 * still producing more audio output.
 * 
 * @author tshalif
 *
 */
public interface Player extends Job {

	/**
	 * How big read/write chunks should be - this
	 * has impact on player performance and good
	 * thread cooperation.
	 */
	public static final int WRITE_GRANULARITY = 6144;

	/**
	 * Add a listener to receive Player events such as PlayerEndEvent()
	 * @param playListener the listener to add
	 */
	public abstract void addPlayListener(EventHandler playListener);

	/**
	 * Add offset mark. Player will send an PlayerOffsetEvent() when
	 * that play stream offset is reached.
	 * @param off byte offset to mark 
	 */
	public abstract void addPosMark(long off);

	/**
	 * Temporary disable sound. The sound output is stopped and the object
	 * idendified by id is placed in the disable request queue. Only when all
	 *  but the player
	 * Job state does not change to PAUSED. 
	 * @see #enableSound(Object)
	 */
	public abstract void disableSound(Object id);

	/**
	 * Reanable sound if temporary disable by #disableSound(). The player will only
	 * Resume playing if it is neither: a) paused, b) still have other objects in it's
	 * disable sound queue, 
	 * @param id
	 * @see #disableSound(Object)
	 */
	public abstract void enableSound(Object id);

	/**
	 * Check either sound is enabled for given object id
	 * @see #disableSound(Object)
	 * @see #enableSound(Object)
	 */
	public abstract boolean isEnabledSound(Object id);
	/**
	 * Return current length of audio stream
	 * @return length in bytes
	 */
	public abstract long getLength();

	/**
	 * Return player position.
	 * @return play stream position in bytes
	 */
	public abstract long getPlayPos();

	/**
	 * Get current playing progress as percent
	 * @return a value between 0 - 100
	 */
	public abstract int getProgress();

	/**
	 * check either audio stream is still growing or not
	 * @return true if still growing
	 * @see #isGrowing()
	 */
	public abstract boolean isGrowing();

	/**
	 * Remove a listener to receive Player events such as PlayerEndEvent()
	 * @param playListener the listener to remove
	 */
	public abstract void removePlayListener(EventHandler playListener);

	/**
	 * calculates skip seconds in data stream byte length
	 */
	public long secondsToBytes(int sec);

	/**
	 * Seek to given audio position
	 * @param offset play stream byte offset to seek to
	 * @throws IOException
	 */
	public abstract void seek(long offset) throws PlayerException;

	/**
	 * Skip given amount of seconds forward or back in play stream
	 * @param seconds skip back if negative (< 0), forward if positive (> 0)
	 * @throws IOException
	 */
	public abstract void skip(int seconds) throws PlayerException;

	/**
	 * Create an OutputStream object allowing appending to end of play data.
	 * returned output stream should be closed when no more data is intended for appending
	 * @return an OutputStream object wrapping this Player's internal play data
	 */
	public OutputStream outputStream();

	/**
	 * Set audio format
	 */
	public void open(AudioFormat format) throws PlayerException;

	/**
	 * Get audio format
	 */
	public AudioFormat getAudioFormat();
}
