package org.nargila.speak.framework.synth.html;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.nargila.speak.event.synth.SynthesizerSeekEndEvent;
import org.nargila.speak.event.synth.SynthesizerSeekStartEvent;
import org.nargila.speak.event.synth.html.HtmlLinkEvent;
import org.nargila.speak.framework.Conf;
import org.nargila.util.EventHandler;
import org.nargila.util.WavPlayer;

public class HtmlSynthesizerTest extends TestCase implements EventHandler {
	final String url = "file:" + Conf.getProperty("testdir") + "/data/html/wikipedia.org.Mossad.html";
	WavPlayer m_skipWait;
	HtmlSynthesizerImpl synth;
	/*
	 * Test method for 'jssspeak.browser.HtmlSynthesizer.skip(String)'
	 */
	public void testSkipString() throws Exception {
		synth.speakHtml(new URL(url));
		Thread.sleep(6000);
		synth.targetSkip("Departments");
		System.out.println("should be beeping now");
		
		Runnable asyncSkipCanceler = new Runnable() {		
			public void run() {
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
				}
				synth.cancelSkip();
				System.out.println("should be resuming at previous pre-skip location");
			}		
		};

		System.out.println("main thread is alive!");
		
		new Thread(asyncSkipCanceler, "Asynchroneous Skip Cancel") {}.start();
		
		Thread.sleep(10000);
		synth.targetSkip("Departments");
		System.out.println("should be beeping now");
		synth.finish();
	}

	public void handleEvent(Object event) {

		System.out.println("event: " + event);

		if (event instanceof SynthesizerSeekStartEvent) {
			if (m_skipWait.isStopped()) {
				m_skipWait.play(800);
			}
		} else if (event instanceof SynthesizerSeekEndEvent) {			
			m_skipWait.stop();
		} else if (event instanceof HtmlLinkEvent) {
			System.out.println("Html link: " + ((HtmlLinkEvent)event).getHref());
		}
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(HtmlSynthesizerTest.class);
	}

//	@Override
	protected void setUp() throws Exception {
		m_skipWait = new WavPlayer(new File(Conf.getProperty("ssspeakDir") + "/share/sounds/ssspeak/skipping.wav"));
		synth = new HtmlSynthesizerImpl("test");

		synth.addSynthListener(this);
		
		Conf.setProperty("debug.keepFiles", "true");
		
		super.setUp();
	}

}
