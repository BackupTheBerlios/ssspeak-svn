package org.nargila.speak.event.synth;

import org.nargila.speak.event.SpeakEvent;

/**
 * Base synthesis engine event 
 * @author tshalif
 */
public abstract class SynthesisEngineEvent extends SpeakEvent {

	private static final long serialVersionUID = 1L;

	/**
	 * ctor
	 * @param source event generator (e.g. synthesizer) id 
	 */
	protected SynthesisEngineEvent(Object source, Object data) {
		super(source, data);
	}
}
