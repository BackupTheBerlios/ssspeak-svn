package org.nargila.speak.event.player;


/**
 * Event generated when player has reached end of audio stream.
 * @author tshalif
 *
 */
public class PlayerEndEvent extends PlayerEvent {	

	private static final long serialVersionUID = 1L;

	/**
	 * ctor
	 * @param source event generator (e.g. player) id 
	 */
	public PlayerEndEvent(Object source) {
		super(source);
	}

	public String toString() {
		return "player end";
	}
}

