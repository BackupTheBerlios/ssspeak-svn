package org.nargila.speak.event.synth;


/**
 * Event sent by synthesis engine/producer notifying of 
 * SSML mark's audio byte offset position.
 * 
 * The sysnthesizer resolves the mark/offset mapping of
 * the next SSML mark uppon interception of this event
 * @author tshalif
 *
 */
public class SynthesisEngineOffsetEvent extends SynthesisEngineEvent {

	private static final long serialVersionUID = 1L;

	/**
	 * ctor
	 * @param source generating synthesizer/engine id 
	 * @param offset SSML mark's audio byte offset 
	 */
	public SynthesisEngineOffsetEvent(Object source, long offset) {
		super(source, new Long(offset));
	}
	
	/**
	 * get offset
	 * @return audio byte offset
	 */
	public long getOffset() {
		return ((Long)getData()).longValue();
	}

	public String toString() {
		return "synthesis engine offset: " + getOffset();
	}
}
