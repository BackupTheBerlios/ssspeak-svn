/**
 * 
 */
package org.nargila.speak.framework.browser;

import java.net.URI;

public final class SynthSession {
	public final URI uri;
	public SynthRefItem synthRef;
	public boolean paused = false;
	public long savedOffset;
	public SynthSession(URI uri, SynthRefItem synthRef) {
		this.uri = uri;
		this.synthRef = synthRef;			
	}
}