package org.nargila.speak.impl.synth.festival;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;

import org.nargila.speak.framework.Conf;
import org.nargila.util.EventHandler;
import org.nargila.util.WavPlayer;
import org.nargila.util.Xslt;
import org.w3c.dom.Node;

public class FestivalSynthesisEngineTest extends TestCase {
	Node ssml;
	FestivalSynthesisEngine s2w;
	File fileName;
	
	protected void setUp() throws Exception {
		Conf.setProperty("debug.keepFiles", "true");
		
		s2w = new FestivalSynthesisEngine();

		super.setUp();
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(FestivalSynthesisEngineTest.class);
	}
	public void testGetVoices() throws Exception {

		Iterator i = Arrays.asList(s2w.getVoices()).iterator();
		
		while (i.hasNext()) {
			String s = (String) i.next();
			System.out.println("voice:" + s);
		}				
	}
	/*
	 * Test method for 'jssspeak.ssmlsynth.FestivalSynthesisEngine.doJob()'
	 */
	public void testDoJob() throws Exception {
		ssml = 
			Xslt.loadXML(new StringReader("<speak>hello <mark name='bula'/> world</speak>"));
		
		fileName = File.createTempFile("FestivalSynthesisEngineTest", ".wav");
		
		FileOutputStream out = new FileOutputStream(fileName);

		s2w.synth(ssml, out, new EventHandler() {
			public void handleEvent(Object event) {
				String msg = "SynthesisEngineEvent: " + event;
				System.out.println(msg);
			}
		});
		
		
		Thread.sleep(500);
		
		System.out.println("pausing for 5 seconds");
		
		s2w.pause();
		
		Thread.sleep(5000);
		
		System.out.println("resuming");

		s2w.resume();
		
		s2w.finish();

		WavPlayer player = new WavPlayer(fileName, new RawAudioFormat());
		player.play().drain();
	}

}
