/**
 * Implementation framework of a serial synthesizer
 */
package org.nargila.speak.framework.synth;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import org.nargila.speak.event.player.PlayerEndEvent;
import org.nargila.speak.event.synth.SynthesizerEndEvent;
import org.nargila.speak.event.synth.SynthesizerEvent;
import org.nargila.speak.framework.player.Player;
import org.nargila.speak.framework.player.PlayerImpl;
import org.nargila.speak.synth.SynthesisException;
import org.nargila.speak.synth.Synthesizer;
import org.nargila.util.DefaultJob;
import org.nargila.util.EventHandler;
import org.nargila.util.EventHandlerManager;
import org.nargila.util.Xslt;
import org.w3c.dom.Node;



/**
 * Basic implementation of the Synthesizer interface. This impleentaion 
 * Assumes speech synthesis is produced by a SynthesisEngine, which churns 
 * out an unskippable continuous audio output from start to end of the synthesis. 
 */
public class SynthesizerImpl extends DefaultJob implements Synthesizer {

	/**
	 * The player object attached to this synthesizer (may be null)
	 */
	private Player m_player;
	
	/**
	 * Output to send speech audio data to. This may be 
	 * an output file for batch synthesis or a wrapper OutputStream
	 * arround the player object
	 * 
	 * @see Player#outputStream()
	 */
	private OutputStream m_output;
	
	/**
	 * Helper object to manage marks
	 */
	private MarkManager m_markManager;
	
	/**
	 * Helper object to handle skip requests 
	 */
	private SkipManager m_skipManager;
	
	/**
	 * An implementation of the synthesis engine.
	 */
	private SynthesisEngine m_synthEngine;
	
	/**
	 * synthesis listener manager
	 */
	protected EventHandlerManager m_listenerManager = new EventHandlerManager();
	
	
	private boolean m_growing;
	
	public SynthesizerImpl() {
		this("SynthesizerImpl");
	}
	
	public SynthesizerImpl(String id) {
		this(id, new PlayerImpl(id), null);
	}
	
	public SynthesizerImpl(OutputStream output) {
		this("SynthesizerImpl", null, output);
	}
	
	private SynthesizerImpl(String id, Player player, OutputStream output) {
		setId(id);

		m_synthEngine = SynthesisEngineFactory.getInstance();
		m_output = output;		
		m_player = player;
		
		if (null != player) {
			m_markManager = new MarkManager(this);		
			m_skipManager = new SkipManager(this);
			
			m_player.addPlayListener(new EventHandler() {

				public void handleEvent(Object event) {
					m_listenerManager.generateEvent(event); // broadcast player event to synthesis listeners
					
					if (event instanceof PlayerEndEvent) {
						m_listenerManager.generateEvent(new SynthesizerEndEvent(getId()));
					}
				}
				
			});
			m_listenerManager.addHandler(m_markManager); // mark manager will intercept engine events and player pos events
			
			m_output = player.outputStream();
		}
	}
	
	
	public void speakSsml(Node ssml) {
		try {
			if (null != m_player) {
				m_player.abort();
				m_markManager.setup(ssml);				
				m_player.open(m_synthEngine.getAudioFormat());
			}
			
			m_synthEngine.setId(getId());			
			m_synthEngine.synth(ssml, outputStream(), m_listenerManager);
		} catch (Exception e) {
			new RuntimeException(e);
		}
	}

	public void speakSsml(String ssml) {
		try {
			speakSsml(Xslt.loadXML(new StringReader(ssml)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void speakPlainText(String text) {
		speakSsml("<speak>" + text + "</speak>");
	}

	public void finish() {
		if (null != m_synthEngine) m_synthEngine.finish();
		if (null != m_player) m_player.finish();
	}

	public void abort() {
		if (null != m_synthEngine) {
			m_synthEngine.abort();
		}
		if (null != m_player) m_player.abort();
	}

	public void pause() {
		if (null != m_player) m_player.pause();
		if (null != m_synthEngine) m_synthEngine.pause();
	}

	public void resume() {
		if (null != m_synthEngine) m_synthEngine.resume();
		if (null != m_player) m_player.resume();
	}

	public MarkManager getMarkManager() {
		return m_markManager;
	}

	public Player getPlayer() {
		return m_player;
	}


	public void skip(String mark)  throws SynthesisException {
		m_skipManager.skip(mark);
	}

	public void skip(int sec)  throws SynthesisException {
		m_skipManager.skip(sec);
	}

	public void addPosMark(long off) {
		m_player.addPosMark(off);
	}

	public long getLength() {
		return m_player.getLength();
	}

	public long getPlayPos() {
		return m_player.getPlayPos();
	}

	public int getProgress() {
		return m_player.getProgress();
	}

	/**
	 * Mark this synthesis job as not growing
	 * @param growing false to indicate end of synthesis speech production by synthesis engine
	 */
	void setGrowing(boolean growing) {
		m_growing = growing;
	}
	
	public boolean isGrowing() {
		return m_growing;
	}

	public void seek(long offset)  throws SynthesisException {
		m_player.seek(offset);
	}

	public void cancelSkip() {
		m_skipManager.cancelSkip();
	}

	public void addSynthListener(EventHandler synthesizerListener) {
		m_listenerManager.addHandler(synthesizerListener);
	}

	public void removeSynthListener(EventHandler synthesizerListener) {
		m_listenerManager.removeHandler(synthesizerListener);
	}

	/**
	 * Make this synthesizer broadcast an event to all its listeners
	 * @param event event to fire
	 */
	void generateEvent(SynthesizerEvent event) {
		m_listenerManager.generateEvent(event);
	}

	public void setId(Object id) {
		super.setId(id);		
		
		if (null != m_player) {
			m_player.setId(id);
		}
	}

	// TODO check either SyntheisEngine does not already
	// fire end of synthesis event and mark manager intercepts
	private OutputStream outputStream() {
		return new OutputStream() {

//			@Override
			public void write(int b) throws IOException {
				m_output.write(b);
			}

//			@Override
			public void close() throws IOException {
				m_output.close();

				if (null != m_markManager) {
				    m_markManager.notifyMarks();
				}
			}

//			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				m_output.write(b, off, len);
			}};	
	}

	public void disableSound(Object id) {
		m_player.disableSound(id);
	}

	public void enableSound(Object id) {
		m_player.enableSound(id);
	}

	public String[] getVoices() throws SynthesisException {
		return m_synthEngine.getVoices();
	}

	public void start() {		
	}

	public boolean isEnabledSound(Object id) {
		return m_player.isEnabledSound(id);
	}
}
