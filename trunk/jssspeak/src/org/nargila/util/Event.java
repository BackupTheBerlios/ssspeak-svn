package org.nargila.util;

import java.util.EventObject;

/**
 * Basic event object.
 */
public class Event extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final Object data;
	
	protected Event(Object source) {
		this(source, null);
	}
	protected Event(Object source, Object data) {
		super(source);
		this.data = data;
	}
	public Object getData() {
		return data;
	}
}
