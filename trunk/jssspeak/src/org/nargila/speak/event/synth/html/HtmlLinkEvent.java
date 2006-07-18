/**
 * events generated by the HTML synthesis subsystem
 */
package org.nargila.speak.event.synth.html;

import java.net.URI;

import org.nargila.speak.event.synth.SynthesizerEvent;

/**
 * Event generated by the HtmlSynthesizer uppon href link.
 * @author tshalif
 * 
 * @see org.nargila.speak.synth.HtmlSynthesizer
 */
public class HtmlLinkEvent extends SynthesizerEvent {
	
	private static final long serialVersionUID = 1L;
	public final URI href;
	
	/**
	 * ctor
	 * @param source event generator (e.g. synthesizer) id
	 * @param href the href at current audio position
	 */
	public HtmlLinkEvent(Object source, URI href) {
		super(source);
		this.href = href;
	}

	/**
	 * get href value
	 * @return the href
	 */
	public URI getHref() {
		return href;
	}

	public String toString() {
		return "html link: " + getHref();
	}
}
