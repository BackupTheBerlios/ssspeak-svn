package org.nargila.speak.event.synth;


/**
 * Synthesizer stoped syntheis.
 */
public class SynthesizerStopEvent extends SynthesizerEvent {	
	private static final long serialVersionUID = 1L;

	/**
	 * ctor
	 * @param source event generator (e.g. synthesizer) id 
	 */
	public SynthesizerStopEvent(Object source) {
		super(source);
	}
	public String toString() {
		return "synthesizer stopped";
	}
}
