package org.nargila.speak.framework.browser;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.nargila.speak.framework.Conf;
import org.nargila.speak.framework.synth.html.HtmlSynthesizerImpl;
import org.nargila.speak.synth.HtmlSynthesizer;
import org.nargila.speak.synth.SynthesisException;
import org.nargila.util.EventHandler;

public class SynthSessionManager {
	static final int DEFAULT_CACHE_SIZE = 10;
	
	//Map<URI,SynthRefItem> m_url2SynthMap = new HashMap<URI,SynthRefItem>();
	private Map m_url2SynthMap = new LinkedHashMap() {

		private static final long serialVersionUID = 1L;

		// This method is called just after a new entry has been added
		public boolean removeEldestEntry(Map.Entry eldest) {
			int maxSize = DEFAULT_CACHE_SIZE;
			
			try {
				maxSize = new Integer(Conf.getProperty("synthesizer.sessionCacheSize")).intValue();
				maxSize = Math.max(maxSize, 1);
			} catch (NumberFormatException e) {}
			
			if (size() > maxSize) {
				removeSession((URI) eldest.getKey());
			}
			
			return false;
		}
	};
	
	//Map<URI,SynthSession> m_sessionMap = new HashMap<URI,SynthSession>();
	private Map m_sessionMap = new HashMap();
	private EventHandler m_synthesizerListener;
	
	SynthSessionManager(EventHandler synthesizerListener) {
		m_synthesizerListener = synthesizerListener;
	}
	
	private synchronized void removeRef(SynthRefItem synthRef) {
		synthRef.synth.abort();		
		SynthRefItem removeCheck;		
		removeCheck = (SynthRefItem) m_url2SynthMap.remove(synthRef.synth.getUri());
		assert removeCheck == synthRef: synthRef.synth.getUri() + ": removed synth ref not found in m_url2SynthMap!";
	}	
	
	synchronized SynthSession removeSession(URI uri) {
		SynthSession session = getSession(uri);
		
		if (session != null) {
			SynthRefItem synthRef = session.synthRef;
			
			session.synthRef = null;
			
			m_sessionMap.remove(session.uri);
			
			synchronized (m_url2SynthMap) {
				synchronized (synthRef) {
					if (session.uri.getFragment() == null) {
						Iterator i = synthRef.refList.iterator();
						
						while (i.hasNext()) {
							URI u = (URI) i.next();
							m_sessionMap.remove(u);
						}
						synthRef.refList.clear();
					} else {
						synthRef.refList.remove(session.uri); // remove self from ref
					}
					/*
					 * if this is the only reference, or is the base url (i.e. no '#' part)
					 * remove any session refering to it
					 */
					if (synthRef.refList.isEmpty()) {
						removeRef(synthRef);
					}
				}
			}
		}
		return session;
	}
	
	public boolean hasSession(URI uri) {
		return m_sessionMap.containsKey(uri);
	}
	
	SynthSession getSession(URI uri) {
		SynthSession res = (SynthSession) m_sessionMap.get(uri);
		
//		if (null != res) {
//		res.synthRef.synth.enableSound(this);
//		}
		return res;
	}
	/**
	 * Search for existing Synth object for absolute part of url - or creates existing one.
	 * If the url contains an '#' and there is already a Synth object for that page, then no new Synth object
	 * is created and the current one is returned.
	 * @return an existing or new Synth object
	 * @throws SynthesisException 
	 */
	private SynthRefItem getCreateSynth(URL htmlSrc, URI uri) throws SynthesisException  {
		
		SynthRefItem synthRef = null;
		
		URI baseUri;
		try {
			baseUri = uri.resolve(new URI(null, null, uri.getPath(), uri.getQuery(), null));
		} catch (URISyntaxException e) {
			throw new SynthesisException(e);
		}
		
		assert baseUri.getFragment() == null: "baseUri should never have a fragment part!";
		
		synchronized (m_url2SynthMap) {
			
			synthRef = (SynthRefItem) m_url2SynthMap.get(baseUri);
			
			if (null == synthRef) {
				HtmlSynthesizer synth = createSynthesizer(baseUri);
				
				synthRef = new SynthRefItem(synth);
				synthRef.synth.disableSound(this);
				synthRef.synth.speakHtml(htmlSrc, baseUri);
				
				m_url2SynthMap.put(baseUri, synthRef);
			}
			synchronized (synthRef) {
				if (!synthRef.refList.contains(uri)) {
					synthRef.refList.add(uri);
				}
			}
		}
		
		return synthRef;
	}
	
	private HtmlSynthesizer createSynthesizer(URI baseUri) {
		
		HtmlSynthesizer synth = new HtmlSynthesizerImpl(baseUri.toString());

		synth.addSynthListener(m_synthesizerListener);

		return synth;
	}
	synchronized void clearAll() {
		synchronized (m_sessionMap) {
			URI[] sessionKeys = (URI[]) m_sessionMap.keySet().toArray(new URI[0]);
			
			for (int i = 0; i < sessionKeys.length; ++i) {
				removeSession(sessionKeys[i]);
			}
		}
	}
	
	/**
	 * 
	 * @param htmlSrc
	 * @param uri
	 * @throws SynthesisException 
	 */
	public synchronized void addSession(URL htmlSrc, URI uri) throws SynthesisException {				
		SynthSession session = null;
		
		assert null == getSession(uri): "session identified by url " + uri + " already exists";
		
		SynthRefItem synthRef = getCreateSynth(htmlSrc, uri);
		
		session = new SynthSession(uri, synthRef);
		
		m_sessionMap.put(uri, session);
	}
}
