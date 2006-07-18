package org.nargila.speak.event.player;


/**
 * Event generated by player when it's resume() method is called.
 * 
 * @author tshalif
 * @see org.nargila.speak.framework.player.Player#resume()
 */
public class PlayerResumedEvent extends PlayerEvent {	
	private static final long serialVersionUID = 1L;

	/**
	 * ctor
	 * @param source event generator (e.g. player) id 
	 */
	public PlayerResumedEvent(Object source) {
		super(source);
	}

	public String toString() {
		return "player resumed";
	}
}
