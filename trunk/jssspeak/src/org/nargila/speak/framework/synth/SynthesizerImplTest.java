package org.nargila.speak.framework.synth;

import java.io.File;
import java.io.StringReader;

import junit.framework.TestCase;

import org.nargila.speak.event.synth.SynthesizerSeekEndEvent;
import org.nargila.speak.event.synth.SynthesizerSeekStartEvent;
import org.nargila.speak.framework.Conf;
import org.nargila.speak.synth.Synthesizer;
import org.nargila.util.EventHandler;
import org.nargila.util.WavPlayer;
import org.nargila.util.Xslt;
import org.w3c.dom.Node;

public class SynthesizerImplTest extends TestCase implements EventHandler {

	WavPlayer m_skipWait;
	public static void main(String[] args) {
		junit.swingui.TestRunner.run(SynthesizerImplTest.class);
	}

	private Synthesizer createSynth(String id) throws Exception {
		Synthesizer synth = new SynthesizerImpl(id);
		synth.addSynthListener(this);

		return synth;
	}
	/*
	 * Test method for 'jssspeak.ssmlsynth.Synth.Synth(Node, Listener, RawPlayer)'
	 */
	public void testSynthReuse() throws Exception {
		Synthesizer synth = createSynth("testSynth");
		Node ssml = Xslt.loadXML(new StringReader(
				"<speak>hello world, <mark name='m1'/> hello <mark name='m2'/> world. hello world.</speak>"
				));		
		synth.speakSsml(ssml);
		synth.finish();
		synth.speakPlainText("hello again");
		synth.finish();
		synth.speakPlainText("hello again - for the last time");
		synth.finish();
	}
	
	public void testSynth() throws Exception {
		Synthesizer synth = createSynth("testSynth");
		Node ssml = Xslt.loadXML(new StringReader(
				"<speak>hello world, <mark name='m1'/> hello <mark name='m2'/> world. hello world.</speak>"
				));
		

		synth.speakSsml(ssml);
		synth.finish();
	}
	
	public void testPause() throws Exception {
		Synthesizer synth = createSynth("testPause");
		String ssml = Conf.getProperty("testdir") + "/data/ssml/Mossad.ssml";
		synth.speakSsml(Xslt.loadXML(ssml));
		
		Thread.sleep(3000);
		synth.pause();
		Thread.sleep(3000);
		synth.resume();
		Thread.sleep(3000);
		synth.pause();
		Thread.sleep(3000);
		synth.resume();
		synth.finish();
	}
	public void testSkipMark() throws Exception {
		Synthesizer synth = createSynth("testSkipMark");
		String ssml = Conf.getProperty("testdir") + "/data/ssml/Mossad.ssml";
		synth.speakSsml(Xslt.loadXML(ssml));
		synth.skip("/wiki/Arab");
		synth.finish();
	}
	public void testSkipMarkCancel() throws Exception {
		Synthesizer synth = createSynth("testSkipMarkCancel");
		String ssml = Conf.getProperty("testdir") + "/data/ssml/Mossad.ssml";
		synth.speakSsml(Xslt.loadXML(ssml));

		System.out.println("starting pending skip");

		System.out.println("will cancel skip in one second");

		synth.skip("/wiki/Arab");
		
		Thread.sleep(1000);
		
		System.out.println("canceling skip - should continue where it was");

		synth.cancelSkip();
		
		synth.finish();
	}
	public void testPauseWhileSkipping() throws Exception {
		Synthesizer synth = createSynth("testPauseWhileSkipping");
		String ssml = Conf.getProperty("testdir") + "/data/ssml/Mossad.ssml";
		synth.speakSsml(Xslt.loadXML(ssml));
		synth.skip("/wiki/Arab");		
		
		Thread.sleep(1000);
		
		synth.pause();
		
		Thread.sleep(1000);
		
		synth.resume();
		
		Thread.sleep(5000);

		synth.finish();
	}

	public void handleEvent(Object event) {
		System.out.println("type:" + event);

		if (event instanceof SynthesizerSeekStartEvent) {
			if (m_skipWait.isStopped()) {
				m_skipWait.play(800);
			}
		} else if (event instanceof SynthesizerSeekEndEvent) {			
			m_skipWait.stop();
		} else if (event instanceof SynthesizerSeekStartEvent) {
			if (m_skipWait.isStopped()) {
				m_skipWait.play(800);
			}
		} else if (event instanceof SynthesizerSeekEndEvent) {			
			m_skipWait.stop();
		}
	}

//	@Override
	protected void setUp() throws Exception {
		m_skipWait = new WavPlayer(new File(Conf.getProperty("ssspeakDir") + "/share/sounds/ssspeak/skipping.wav"));
		super.setUp();
	}
}
