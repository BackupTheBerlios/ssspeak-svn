package org.nargila.speak.framework.browser;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.nargila.speak.framework.synth.SynthesizerImpl;
import org.nargila.speak.synth.HtmlSynthesizer;
import org.nargila.speak.synth.NoSuchTargetException;
import org.nargila.speak.synth.SynthesisException;
import org.nargila.util.DefaultJob;
import org.nargila.util.EventHandler;
import org.nargila.util.EventHandlerManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class SynthBrowser extends DefaultJob implements HtmlSynthesizer {
	private EventHandlerManager m_synthesizerListenerManager = new EventHandlerManager();
	private SynthSessionManager m_synthSessionManager = new SynthSessionManager(m_synthesizerListenerManager);

	SynthSession m_currentSession = null;
	
	public SynthBrowser() {
	}
	
	public void setSession() throws SynthesisException  {
		setSession((URI)null);
	}
	
	public void setSession(URI uri) throws SynthesisException  {
		SynthSession session = m_synthSessionManager.getSession(uri);
		
		if (m_currentSession == session) {
			return;
		}
		
		if (m_currentSession != null) {
			m_currentSession.synthRef.synth.cancelSkip();
			m_currentSession.synthRef.synth.pause();
			m_currentSession.savedOffset = m_currentSession.synthRef.synth.getPlayPos();
		}

		m_currentSession = session;
			
		if (session != null) {

			session.synthRef.synth.setId(uri);

			/*
			 * If url contains a target (e.g. '#Chapter_1)
			 * scroll to that position
			 */
			if (uri.getFragment() != null) {
				try {
					session.synthRef.synth.targetSkip(uri.getFragment());
				} catch (NoSuchTargetException e) {
					//e.printStackTrace();
				}
			} else {	
				session.synthRef.synth.seek(session.savedOffset); // skip to offset (would be 0 if session is new)
			}
			
			session.synthRef.synth.enableSound(m_synthSessionManager);
				
			session.synthRef.synth.resume();
		}
	}	
	
	public synchronized void speakHtml(URL htmlSrc) throws SynthesisException {
		try {
			speakHtml(htmlSrc, new URI(htmlSrc.toString()));
		} catch (URISyntaxException e) {
			throw new SynthesisException(e);
		}
	}
	
	public synchronized void speakHtml(URL source, URI uri) throws SynthesisException {
		if (null == m_synthSessionManager.getSession(uri)) {
			m_synthSessionManager.addSession(source, uri);
		}
	}
	
	public void skip(String mark) throws SynthesisException {
		if (m_currentSession != null) {
			m_currentSession.synthRef.synth.skip(mark);
		}
	}
	public synchronized void seek(long offset) throws SynthesisException {
		if (m_currentSession != null) {
			m_currentSession.synthRef.synth.seek(offset);
		}
	}
	public void skip(int sec) throws SynthesisException {
		if (m_currentSession != null) {
			m_currentSession.synthRef.synth.skip(sec);
		}
	}

	public void resume() {
		if (m_currentSession != null) {
			m_currentSession.synthRef.synth.resume();
			m_currentSession.paused = false;
		}
	}	

	public void pause() {
		if (m_currentSession != null) {
			m_currentSession.paused = true;
			m_currentSession.synthRef.synth.pause();
		}
	}

	public synchronized void remove(URI uri) throws SynthesisException {
	    if (null != m_currentSession && m_currentSession.uri.equals(uri)) {
		setSession();
	    }
		
	    m_synthSessionManager.removeSession(uri);
		
	}

	public SynthSession getCurrent() {
		return m_currentSession;
	}

	public long getPlayPos() {
		if (null != m_currentSession) {
			return m_currentSession.synthRef.synth.getPlayPos();
		}
		
		return 0;
	}
	
	public int getProgress() {
		if (null != m_currentSession) {
			return m_currentSession.synthRef.synth.getProgress();
		}
		
		return 0;
	}

	public void removeHandler(EventHandler synthListener) {
		m_synthesizerListenerManager.removeHandler(synthListener);
	}
	public void cancelSkip() {
		SynthSession session = getCurrent();
		
		if (null != session) {
			session.synthRef.synth.cancelSkip();
		}
	}

	public void disableSound(Object id) {
		SynthSession session = getCurrent();
		
		if (null != session) {
			session.synthRef.synth.disableSound(id);
		}
	}

	public boolean isEnabledSound(Object id) {
		SynthSession session = getCurrent();
		
		if (null != session) {
			return session.synthRef.synth.isEnabledSound(id);
		}
		
		return false;
	}

	public void enableSound(Object id) {
		SynthSession session = getCurrent();
		
		if (null != session) {
			session.synthRef.synth.enableSound(id);
		}
	}

	public long getLength() {
		SynthSession session = getCurrent();
		
		if (null != session) {
			return session.synthRef.synth.getLength();
		}
		return 0;
	}

	public boolean isGrowing() {
		SynthSession session = getCurrent();
		
		if (null != session) {
			return session.synthRef.synth.isGrowing();
		}

		return false;
	}


	public void finish() {
		SynthSession session = getCurrent();
		
		if (null != session) {
			session.synthRef.synth.finish();
		}
	}

	public void abort() {
		SynthSession session = getCurrent();
		
		if (null != session) {
			try {
				remove(session.uri);
			} catch (SynthesisException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean hasSession(URI uri) {
		return m_synthSessionManager.hasSession(uri);
	}

	public void clearAll() {
		abort();		
		m_synthSessionManager.clearAll();
	}


	public void targetSkip(String target) throws SynthesisException {
		SynthSession session = getCurrent();
		
		if (null != session) {
			session.synthRef.synth.targetSkip(target);
		}
	}

	public URI getUri() {
		SynthSession session = getCurrent();

		if (null != session) {
			return session.synthRef.synth.getUri();
		}
		return null;
	}

	public void speakSsml(Node ssml) throws SynthesisException {
		throw new SynthesisException("not implemented");
	}

	public void speakSsml(String ssml) throws SynthesisException {
		throw new SynthesisException("not implemented");
	}

	public void speakPlainText(String text) throws SynthesisException {
		throw new SynthesisException("not implemented");
	}

	public void addSynthListener(EventHandler synthesizerListener) {
		m_synthesizerListenerManager.addHandler(synthesizerListener);
	}

	public void removeSynthListener(EventHandler synthesizerListener) {
		m_synthesizerListenerManager.removeHandler(synthesizerListener);
	}

	public String[] getVoices() throws SynthesisException {
		return new SynthesizerImpl().getVoices();
	}

	public void speakHtml(Document html, URI uri) throws SynthesisException {
		throw new SynthesisException("not implemented");
	}
}
