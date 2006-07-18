package org.nargila.speak.synth;

import java.net.URI;
import java.net.URL;

import org.w3c.dom.Document;

/**
 * Operations required by an Html synthesizer
 * @author tshalif
 *
 */
public interface HtmlSynthesizer extends Synthesizer {

	/**
	 * Synthesize html
	 * @param url location/id of HTML synth job
	 * @throws SynthesisException
	 */
	public void speakHtml(URL url) throws SynthesisException;

	/**
	 * Synthesize html
	 * @param html input html data
	 * @param uri uri id to attach to identify synthesis job with
	 * @throws SynthesisException
	 */
	public void speakHtml(Document html, URI uri) throws SynthesisException;
	
	/**
	 * Synthesize html
	 * @param source location of HTML data
	 * @param uri the id/location to identify synthesis job with
	 * @throws SynthesisException
	 */
	public void speakHtml(URL source, URI uri) throws SynthesisException;

	/**
	 * skip to given HTML target (e.g. #acme)
	 * @param target target/anchor name to skip to
	 * @throws SynthesisException
	 */
	public void targetSkip(String target) throws SynthesisException;
	
	/**
	 * Get the id/location associated with this synth job
	 * @return
	 */
	public URI getUri();
}