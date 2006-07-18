package org.nargila.speak.event.synth;

/**
 * Pending seek started event. This event will be generated
 * when a future seek (i.e. synthesis producer/engine has not yet
 * synthesized the requested seek position).
 *
 * @see SynthesizerSeekEndEvent
 */
public class SynthesizerSeekStartEvent extends SynthesizerEvent {	
	private static final long serialVersionUID = 1L;

	/**
	 * ctor 
	 * @param source event generator (e.g. synthesizer) id 
	 * @param seekPos the new position (expressed as either ssml mark name or byte offset) to which player is to be eventually set.
	 */
	public SynthesizerSeekStartEvent(Object source, Object seekPos) {
		super(source, seekPos);
	}
	public String toString() {
		return "synthesizer seek start: " + getData();
	}
}
