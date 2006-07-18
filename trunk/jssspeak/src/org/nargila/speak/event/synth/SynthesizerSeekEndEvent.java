package org.nargila.speak.event.synth;

/**
 * Pending seek ended event. This event will be generated
 * when the synthesis producer/engine has reached a previously
 * requested future seek position.
 *
 * @see SynthesizerSeekStartEvent
 */
public class SynthesizerSeekEndEvent extends SynthesizerEvent {	
	private static final long serialVersionUID = 1L;

	/**
	 * ctor 
	 * @param source event generator (e.g. synthesizer) id 
	 * @param seekPos the new position to which player was probably set to by the synthesizer
	 */

	public SynthesizerSeekEndEvent(Object source, long seekPos) {
		super(source, new Long(seekPos));
	}

	public String toString() {
		return "synthesizer seek end: " + getData();
	}
}
