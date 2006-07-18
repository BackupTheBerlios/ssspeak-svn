package org.nargila.speak.event.synth;

/**
 * Synthesizer started syntheis event
 */
public class SynthesizerStartEvent extends SynthesizerEvent {	

	private static final long serialVersionUID = 1L;

	/**
	 * ctor
	 * @param source event generator (e.g. synthesizer) id 
	 */
	public SynthesizerStartEvent(Object source) {
		super(source);
	}
	public String toString() {
		return "synthesizer started";
	}
}
