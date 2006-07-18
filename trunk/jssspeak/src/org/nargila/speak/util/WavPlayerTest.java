package jssspeak.util;

import java.io.File;

import jssspeak.Conf;
import junit.framework.TestCase;

public class WavPlayerTest extends TestCase {

	WavPlayer m_player;
	
//	@Override
	protected void setUp() throws Exception {
		m_player = new WavPlayer(new File(Conf.getProperty("ssspeakDir") + "/share/sounds/ssspeak/drip.wav"));
		super.setUp();
	}

//	@Override
	protected void tearDown() throws Exception {
		m_player.stop();
		super.tearDown();
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(WavPlayerTest.class);
	}

	/*
	 * Test method for 'jssspeak.player.WavPlayer.play()'
	 */
	public void testPlay() {
		m_player.play().drain();
		m_player.stop();
		m_player.play().drain();
	}

	/*
	 * Test method for 'jssspeak.player.WavPlayer.play(boolean)'
	 */
	public void testPlayLoop() {
		m_player.play(500);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		m_player.stop();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
