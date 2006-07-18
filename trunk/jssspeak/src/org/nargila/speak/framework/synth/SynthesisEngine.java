package org.nargila.speak.framework.synth;

import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;

import org.nargila.speak.synth.SynthesisException;
import org.nargila.util.EventHandler;
import org.nargila.util.Job;
import org.w3c.dom.Node;

/**
 * Interface for a serial synthesis producer.
 * @author tshalif
 *
 */
public interface SynthesisEngine extends Job {

	/**
	 * Get speech output audio format.
	 * @return synthesis output audio format
	 */
	public AudioFormat getAudioFormat();
	
	/**
	 * Start producing speech output for given SSML input.
	 * During synthesis, mark offset events are sent to the 
	 * engine listener to allow for just-in-time resolution of SSML
	 * to offset mappings.
	 *  
	 * @param ssml input SSML node
	 * @param wavOutput output where audio data will be written
	 * @param engineListener listener for mark offset events
	 * @throws SynthesisException
	 */
	public void synth(Node ssml, OutputStream wavOutput, EventHandler engineListener) throws SynthesisException;
	
	/**
	 * Get list of voices supported by this engine.
	 * @return list of voices
	 * @throws SynthesisException
	 */
	public String[] getVoices() throws SynthesisException;
}
