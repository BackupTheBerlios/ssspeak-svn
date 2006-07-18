package org.nargila.util;

public class DefaultJob implements Job {

	private Object m_id;
	
	public void finish() {

	}

	public void abort() {

	}

	public void pause() {

	}

	public void resume() {

	}

	public void start() {

	}

	public Object getId() {
		return m_id;
	}

	public void setId(Object id) {
		this.m_id = id;
	}
}
