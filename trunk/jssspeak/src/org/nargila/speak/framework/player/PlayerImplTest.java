package org.nargila.speak.framework.player;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.nargila.speak.framework.Conf;
import org.nargila.speak.framework.synth.SynthesisEngineFactory;
import org.nargila.util.EventHandler;

public class PlayerImplTest extends TestCase implements EventHandler {

	final String wavPath = Conf.getProperty("testdir") + "/data/raw-wav/preparing-for-speeking.raw";
	//final String wavPath = "/tmp/bula.wav";
	
	PlayerImpl player;
	
	public static void main(String[] args) {
		junit.swingui.TestRunner.run(PlayerImplTest.class);
	}

	void resetPlayer() throws Exception {
		
		player.open(SynthesisEngineFactory.getInstance().getAudioFormat());

		new Thread("RawPlayer Data Feeder") {
			public void run() {
				
				OutputStream output = player.outputStream();
				
				try {
					FileInputStream in = new FileInputStream(wavPath);
					
					byte[] buff = new byte[1024];

					for (int len = in.read(buff); len != -1; len = in.read(buff)) {
						output.write(buff, 0, len);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		}.start();
	}
	public void testPause() throws Exception {
		resetPlayer();

		Thread.sleep(1000);
		
		player.pause();
		
		Thread.sleep(3000);
		
		player.resume();
		
		player.finish();
	}
	
	public void testSeek() throws Exception {
		resetPlayer();
		
		
		player.seek(100000);
		


		Thread.sleep(800);

		player.seek(0);
		
		Thread.sleep(1000);

		player.seek(0);

		player.finish();
		
	}
	public void testReset() throws Exception {
		
		for (int i = 0; i < 2; ++i) {
			resetPlayer();
			player.seek(100000);
			
			
			
			Thread.sleep(800);
			
			player.seek(0);
			
			Thread.sleep(1000);
			
			player.seek(0);
			
			player.finish();
		}		
	}

	/* 
	 * Test method for 'jssspeak.player.MyRawPlayer.write(int)'
	 */
	public void testRawPlayer() throws Exception {

		resetPlayer();
		player.addPosMark(0);
		player.addPosMark(100);
		
		player.finish();
	}
	public void handleEvent(Object event) {
		System.out.println("type:" + event);
	}

//	@Override
	protected void setUp() throws Exception {

		this.player = new PlayerImpl("test player");
		this.player.open(SynthesisEngineFactory.getInstance().getAudioFormat());
		this.player.addPlayListener(this);
		super.setUp();
	}
}
