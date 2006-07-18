package org.nargila.speak.event.recognizer;


public class RecognizerEndEvent extends RecognizerEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RecognizerEndEvent(Object source, String result) {
		super(source, result);
	}
	public String getResult() {
		return (String)getData();
	}
	public String toString() {
		return "recognizer finished: " + getResult();
	}
}
