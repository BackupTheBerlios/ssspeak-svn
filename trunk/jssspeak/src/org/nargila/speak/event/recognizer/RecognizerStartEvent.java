package org.nargila.speak.event.recognizer;


public class RecognizerStartEvent extends RecognizerEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RecognizerStartEvent(Object source) {
		super(source, null);
	}

	public String toString() {
		return "recognizer started";
	}
}
