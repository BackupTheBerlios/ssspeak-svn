package org.nargila.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Event handler helper utility. The EventHandlerManager
 * can be used by classes wishing to implement a 
 * addXXHandler() and removeXXHandler() functions.
 *
 * @see EventHandler
 */
public class EventHandlerManager implements EventHandler {

    //private Set<PlayerListener> m_playListeners = new HashSet<PlayerListener>();  
	private Set m_listeners = new HashSet();

	public void generateEvent(Object event) {
		Iterator i = m_listeners.iterator();
		
		while (i.hasNext()) {
			((EventHandler) i.next()).handleEvent(event);
		}		
	}

	public void addHandler(EventHandler listener) {
		m_listeners.add(listener);
	}

	public void removeHandler(EventHandler listener) {
		m_listeners.remove(listener);
	}

	public void clear() {
		m_listeners.clear();
	}

	public boolean isEmpty() {
		return m_listeners.isEmpty();
	}

	public int size() {
		return m_listeners.size();
	}

	public void handleEvent(Object event) {
		generateEvent(event);
	}
}
