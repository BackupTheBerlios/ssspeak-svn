package org.nargila.speak.apps.htmldump;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.nargila.speak.framework.synth.SynthesisEngineFactory;
import org.nargila.util.WavPlayer;

public class HtmlDumpTest extends TestCase {

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(HtmlDumpTest.class);
	}

	/*
	 * Test method for 'jssspeak.progs.HtmlDump.main(String[])'
	 */
	public void testDump() throws Exception {
		File outfile = File.createTempFile("dog", ".wav");
		outfile.deleteOnExit();
		
		HtmlDump.dump(new URL("file:///home/tshalif/src/ws/sspeak/mozext/tmp/dog.html"), new FileOutputStream(outfile));
		
		WavPlayer player = new WavPlayer(outfile, SynthesisEngineFactory.getInstance().getAudioFormat());
		
		player.play().drain();
	}
}
