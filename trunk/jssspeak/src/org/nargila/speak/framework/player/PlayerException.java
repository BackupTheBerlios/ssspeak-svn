package org.nargila.speak.framework.player;

import org.nargila.speak.synth.SynthesisException;

/**
 * Base exception type thrown by the player component.
 */
public class PlayerException extends SynthesisException {

	private static final long serialVersionUID = 1L;

	/**
	 * tor with exception message
	 * @param message the exception message
	 */
	public PlayerException(String message) {
		super(message);
	}

	/**
	 * tor with exception message and cause
	 * @param message the exception message
	 * @param cause the parent cause
	 */
	public PlayerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * tor with exception parent cause
	 * @param cause the parent cause
	 */
	public PlayerException(Throwable cause) {
		super(cause);
	}
}
