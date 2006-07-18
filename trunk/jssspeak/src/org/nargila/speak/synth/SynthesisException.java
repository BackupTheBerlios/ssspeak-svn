package org.nargila.speak.synth;

/**
 * Base exception type thrown by synthesizer implementations
 * @author tshalif
 */
public class SynthesisException extends Exception {


	private static final long serialVersionUID = 1L;


	public SynthesisException(String message) {
		super(message);
	}


	public SynthesisException(String message, Throwable cause) {
		super(message, cause);
	}


	public SynthesisException(Throwable cause) {
		super(cause);
	}

}
