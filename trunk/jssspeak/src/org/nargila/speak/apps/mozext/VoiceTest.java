package org.nargila.speak.apps.mozext;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import junit.framework.TestCase;

public class VoiceTest extends TestCase {

	Voice voice;
	boolean m_stop;

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(VoiceTest.class);
	}

	public VoiceTest() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		voice  = new Voice();
	}
	public void testSay() throws Exception {
		int sleepTime;
		
		voice.say("loading").play().drain();
		System.out.println("you should have just heard 'loading' now");
		voice.say("speaking").play().drain();
		System.out.println("you should have just heard 'speaking' now");
		
		sleepTime = 7;
		
		System.out.println("you should hear 'loading' repeated for " + sleepTime + " seconds");
		voice.say("loading", true).play(1000);
				
		Thread.sleep(sleepTime * 1000);
		
		voice.say("loading", true).stop();
		System.out.println("'loading' should have stopped now");

		System.out.println("program will exit in " + sleepTime + " seconds");
		Thread.sleep(sleepTime * 1000);
		System.out.println("program has exited");
	}

	public void testQueue() throws Exception {
		voice.getDumpQueue().dump(new URL("file:///home/tshalif/src/ws/sspeak/mozext/tmp/dog.html"), "/tmp/dog1.mp3");
		voice.getDumpQueue().dump(new URL("file:///home/tshalif/src/ws/sspeak/mozext/tmp/dog.html"), "/tmp/dog2.mp3");
		voice.getDumpQueue().waitQueue();
	}
}
