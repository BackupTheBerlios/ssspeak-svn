/**
 * Implementation of an HtmlSynthesizer
 */
package org.nargila.speak.framework.synth.html;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.nargila.speak.event.SpeakEvent;
import org.nargila.speak.event.synth.SynthesizerMarkEvent;
import org.nargila.speak.event.synth.html.HtmlLinkEvent;
import org.nargila.speak.framework.Conf;
import org.nargila.speak.framework.synth.SynthesizerImpl;
import org.nargila.speak.framework.xslt.XsltHelper;
import org.nargila.speak.synth.HtmlSynthesizer;
import org.nargila.speak.synth.NoSuchTargetException;
import org.nargila.speak.synth.SynthesisException;
import org.nargila.util.EventHandler;
import org.nargila.util.HtmlDocLoader;
import org.nargila.util.Xslt;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Implementation of HtmlSynthesizer
 * @author tshalif
 *
 */
public class HtmlSynthesizerImpl extends SynthesizerImpl implements HtmlSynthesizer, EventHandler {
	//private Map<String,String> m_target2markMap = new HashMap<String,String>();
	/**
	 * reverse mapping HTML target name to the corresponding SSML mark name 
	 */
	private Map m_target2markMap = new HashMap();
	//private Map<String, String> m_mark2hrefMap;
	
	/**
	 * mapping from SSML mark to the correspondig HTML href
	 */
	private Map m_mark2hrefMap;
	
	/**
	 * Base URI identifying this HTML synth job
	 */
	private URI m_baseUri;
	
	public HtmlSynthesizerImpl() {
		this("HtmlSynthesizerImpl");
	}
	
	public HtmlSynthesizerImpl(String name) {
		super(name);
		
		this.addSynthListener(this);
	}
	
	/**
	 * ctor with output to go to output stream. This constructor
	 * is usefull for batch processing.
	 * @param output the output to write speech data into
	 */
	public HtmlSynthesizerImpl(OutputStream output) {
		super(output);
	}

	public void speakHtml(URL source) throws SynthesisException {
		try {
			speakHtml(source, source.toURI());
		} catch (URISyntaxException e) {
			throw new SynthesisException(e);
		}
	}
	
	public void speakHtml(URL url, URI uri) throws SynthesisException {		
		try {
			speakHtml(HtmlDocLoader.load(url), uri);
		} catch (IOException e) {
			throw new SynthesisException(e);
		} catch (TransformerException e) {
			throw new SynthesisException(e);
		}
	}

	public void speakHtml(Document html, URI uri) {

		assert uri.getFragment() == null: "fragment not allowed in synthesizer uri";

		abort();		
		
		m_baseUri = uri;
		
		setId(uri);

		try {
			boolean markLinks = getPlayer() != null;
			
			if (markLinks) {
				m_mark2hrefMap = HtmlDocLoader.addLinkIds(html);
				m_target2markMap = HtmlDocLoader.generateTargetMarks(m_baseUri, html);
			}
			
			speakSsml(makeSsml(html, markLinks));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * create SSML for the given HTML document.
	 * @param html the HTML document
	 * @param markLinks if true, create an SSML mark for each HTML href 
	 * @return the SSML as Node
	 * @throws Exception
	 */
	private Node makeSsml(Document html, boolean markLinks) throws Exception {
		
	    List xslParams = new LinkedList();

	    xslParams.add("sounddir=" + Conf.getProperty("ssspeakDir") + "/share/sounds/ssspeak");
	    xslParams.add("marklinks=" + (markLinks ? "1" : "0")); 
	    	    
	    String speed = Conf.getProperty("synthesizer.voice.read.speed");
	    String volume = Conf.getProperty("synthesizer.voice.read.volume");
	    String voice = Conf.getProperty("synthesizer.voice.read");

	    if (null != speed) {
		    xslParams.add("speed=" + speed);
	    }
	    if (null != volume) {
		    xslParams.add("volume=" + volume);
	    }

	    if (null != voice && !voice.equals("default")) {
	    	xslParams.add("voice=" + voice);
	    }
		
	    Node ssml = XsltHelper.xslt("html2ssml", html, (String[])xslParams.toArray(new String[xslParams.size()]));
		
		if (!Boolean.valueOf(Conf.getProperty("debug.keepFiles")).booleanValue()) {
			File f = File.createTempFile("makeSsml", ".ssml");
			Xslt.serialize(ssml, f.getAbsolutePath());
		}
		return ssml;
	}

	public void handleEvent(Object event) {
			
		assert event instanceof SpeakEvent: "HDIGH: event shoudl be a subclass of SynthesizerEvent";
	
		if (event instanceof SynthesizerMarkEvent) {
			SynthesizerMarkEvent mark = (SynthesizerMarkEvent)event;
			String href = (String) m_mark2hrefMap.get(mark.getMark());
			
			if (href != null && !href.equals("")) {
				URI hrefuri;
				
				try {
					hrefuri = new URI(href);
				} catch (URISyntaxException e) {
					try {
						href = URLEncoder.encode(href, "utf8").replaceAll("\\+", "%20");
						hrefuri = new URI(href);
					} catch (Exception e2) {
						throw new RuntimeException(e2);
					}
				}
				
				URI link = m_baseUri.resolve(hrefuri);				
				m_listenerManager.generateEvent(new HtmlLinkEvent(getId(), link));				
			}
		}
	}

	// TODO check use of getBaseUrl() in JS code
	public URI getUri() {
		return m_baseUri;
	}

	public void targetSkip(String target) throws SynthesisException {
		String mark = (String) m_target2markMap.get(target);
		
		if (null == mark) {
			throw new NoSuchTargetException(target);
		}
		skip(mark);
	}
}
