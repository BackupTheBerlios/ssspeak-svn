package org.nargila.speak.event.recognizer;


public class RecognizerReadyEvent extends RecognizerEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RecognizerReadyEvent(Object source) {
		super(source, null);
	}

	public String toString() {
		return "recognizer ready";
	}
}
