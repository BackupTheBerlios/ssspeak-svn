package org.nargila.speak.framework.browser;

import java.io.File;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;

import org.nargila.speak.event.synth.SynthesizerSeekEndEvent;
import org.nargila.speak.event.synth.SynthesizerSeekStartEvent;
import org.nargila.speak.event.synth.html.HtmlLinkEvent;
import org.nargila.speak.framework.Conf;
import org.nargila.util.EventHandler;
import org.nargila.util.WavPlayer;

public class SynthBrowserTest extends TestCase implements EventHandler {
	SynthBrowser m_test;
	URL url;
	URI id;
	WavPlayer m_skipWait;

	protected void setUp() throws Exception {
		m_skipWait = new WavPlayer(new File(Conf.getProperty("ssspeakDir") + "/share/sounds/ssspeak/skipping.wav"));
		
		url = new URL("file:" + Conf.getProperty("testdir") + "/data/html/wikipedia.org.Mossad.html");

		// url = new URL("file:///home/tshalif/src/ws/sspeak/mozext/tmp/dog.html");
		id = new URI(url.toString());
		
		m_test = new SynthBrowser();
		m_test.addSynthListener(this);
		
		super.setUp();
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(SynthBrowserTest.class);
	}

	/*
	 * Test method for 'jssspeak.ssmlsynth.SynthSessionManager.setSession(String)'
	 */
	public void testSetSession() throws Exception {
		m_test.speakHtml(url);
		m_test.setSession(id);
		
		assertTrue(m_test.getCurrent().uri.equals(id));
		
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		m_test.abort();
	}

	/*
	 * Test method for 'jssspeak.ssmlsynth.SynthSessionManager.add(String, String)'
	 */
	public void testAddHtml() throws Exception {
		URL u = new URL("file:///home/tshalif/src/ws/sspeak/mozext/tmp/cat.html");
		id = new URI(u.toString());
		
		m_test.speakHtml(u);
		assertTrue(m_test.hasSession(id));

		m_test.setSession(id);
		
		assertTrue(m_test.getCurrent().uri.equals(id));
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

	/*
	 * Test method for 'jssspeak.ssmlsynth.SynthSessionManager.pause(boolean)'
	 */
	public void testPause() throws Exception {
		m_test.speakHtml(url, id);

		m_test.setSession(id);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		m_test.pause();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		m_test.resume();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/*
	 * Test method for 'jssspeak.ssmlsynth.SynthSessionManager.remove(String)'
	 */
	public void testRemove() throws Exception {
		URL u = new URL("file:///home/tshalif/src/ws/sspeak/mozext/tmp/cat.html");

		id = new URI(u.toString());
		
		m_test.speakHtml(u);
		
		assertTrue(m_test.hasSession(id));

		m_test.setSession(id);
		
		assertTrue(m_test.getCurrent().uri.equals(id));

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		

		m_test.remove(id);
		
		assertFalse(m_test.hasSession(id));
		
		
		assertTrue(m_test.getCurrent() == null);			

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

	protected void tearDown() throws Exception {
		m_test.clearAll();
		super.tearDown();
	}

	public void handleEvent(Object event) {
		System.out.println("event:" + event);

		if (event instanceof SynthesizerSeekStartEvent) {
			if (m_skipWait.isStopped()) {
				m_skipWait.play(800);
			}
		} else if (event instanceof SynthesizerSeekEndEvent) {			
			m_skipWait.stop();
		} else if (event instanceof HtmlLinkEvent) {
			System.out.println("html link:" + event);
		} 
	}
}
