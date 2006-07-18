package org.nargila.speak.event.synth;

/**
 * Synthesizer reached end of synthesis
 */
public class SynthesizerEndEvent extends SynthesizerEvent {	

	private static final long serialVersionUID = 1L;

	/**
	 * ctor
	 * @param source event generator (e.g. synthesizer) id 
	 */
	public SynthesizerEndEvent(Object source) {
		super(source, null);
	}

	public String toString() {
		return "synthesizer end of synthesis";
	}
}
