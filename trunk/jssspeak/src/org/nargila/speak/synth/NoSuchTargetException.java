package org.nargila.speak.synth;


/**
 * Exception thrown when target skip is requested but target does not exist.
 * @author tshalif
 *
 */
public class NoSuchTargetException extends SynthesisException {

	private static final long serialVersionUID = 1L;

	public NoSuchTargetException(String message) {
		super(message);
	}

	public NoSuchTargetException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchTargetException(Throwable cause) {
		super(cause);
	}

}
