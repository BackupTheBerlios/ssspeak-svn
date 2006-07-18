package org.nargila.speak.framework.player;

import javax.sound.sampled.AudioFormat;

import org.nargila.util.Job;

/**
 * Interface to describe low-level player operations.
 * @author tshalif
 *
 */
public interface PlayerOps extends Job {
	/**
	 * write audio data to player
	 * @param data audio data
	 * @param length number of bytes to write 
	 * @return number of bytes actually written
	 * @throws IllegalArgumentException if length is not a multiplication of audio frame size
	 * @throws PlayerException if data can not be played
	 */
	public int write(byte[] data, int length) throws PlayerException;
	/*
	 * Get audio format
	 */
	public AudioFormat getAudioFormat();
	/**
	 * Get player buffer latency (in bytes) 
	 * @return number of yet-unplayed bytes in buffer
	 */
	public int getLatency();
	
	/**
	 * Open underlying player implementation for given audio data format.
	 * @param format audio data format to be played by player
	 * @throws PlayerException if player can not be opened
	 */
	public void open(AudioFormat format) throws PlayerException;
	
	/**
	 * Discards queued data
	 * @see javax.sound.sampled.DataLine#flush()
	 */
	public void flush();
	
	/**
	 * Drains queued data
	 * @see javax.sound.sampled.DataLine#drain()
	 */
	public void drain();
	
	/**
	 * Check either player is actually playing (running)
	 * @return true if low-level player is running/playing
	 */
	public boolean isPlaying();
}
