package org.nargila.speak.event.recognizer;

import org.nargila.speak.event.SpeakEvent;

public abstract class RecognizerEvent extends SpeakEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected RecognizerEvent(Object source, Object data) {
		super(source, data);
	}
}
