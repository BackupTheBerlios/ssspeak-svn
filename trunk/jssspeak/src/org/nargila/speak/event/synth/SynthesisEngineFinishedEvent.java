package org.nargila.speak.event.synth;

/**
 * Synthesis engine/producer has finished it's job.
 * This event is generated when all ssml has been synthesized and audio data produced.
 * @author tshalif
 *
 */
public class SynthesisEngineFinishedEvent extends SynthesisEngineEvent {

	private static final long serialVersionUID = 1L;

	/**
	 * ctor
	 * @param source event generator (e.g. synthesizer) id 
	 */
	public SynthesisEngineFinishedEvent(Object source) {
		super(source, null);
	}

	public String toString() {
		return "synthesis engine finished";
	}
}
