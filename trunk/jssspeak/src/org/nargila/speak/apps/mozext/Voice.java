package org.nargila.speak.apps.mozext;


import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import org.nargila.speak.framework.Conf;
import org.nargila.speak.framework.browser.SynthBrowser;
import org.nargila.speak.framework.synth.SynthesisEngineFactory;
import org.nargila.speak.framework.synth.SynthesizerImpl;
import org.nargila.speak.impl.synth.festival.FestivalSynthesisEngine;
import org.nargila.speak.synth.SynthesisException;
import org.nargila.speak.synth.Synthesizer;
import org.nargila.util.EventHandler;
import org.nargila.util.WavPlayer;

public class Voice  extends SynthBrowser {
	//final LinkedList<EventObject> m_eventQueue = new LinkedList<EventObject>();
	final LinkedList m_eventQueue = new LinkedList();
	final WavPlayer m_readyWav;
	//final Map<String, WavPlayer> m_wavCache = new HashMap<String,WavPlayer>();
	final Map m_wavCache = new HashMap();
	
	private MP3DumpQueue dumpQueue = new MP3DumpQueue();
	
	
	public Voice() {
		try {
			m_readyWav = new WavPlayer(new File(Conf.getProperty("ssspeakDir") + "/share/sounds/ssspeak/drip.wav"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		addSynthListener(new EventHandler() {
			
			public void handleEvent(Object event) {
				synchronized (m_eventQueue) {
					m_eventQueue.add(event);
				}
			}
		});		
	}
	
	public WavPlayer say(String txt) throws Exception {
		return say(txt, false);
	}
	
	public WavPlayer say(String txt, boolean cache) {
		
		synchronized (m_wavCache) {
			WavPlayer player = (WavPlayer) m_wavCache.get(txt);
			
			if (null == player) {
				try {
					File file = File.createTempFile("WavPlayer", ".wav");
					if (!Boolean.valueOf(Conf.getProperty("debug.keepFiles")).booleanValue()) {
						file.deleteOnExit();    		
					}
					
					Synthesizer synth = new SynthesizerImpl(new FileOutputStream(file));
					
					String ssml;
					
					{ // make xsl, set speed, volume params
						String speed = Conf.getProperty("synthesizer.voice.announce.speed");
						String volume = Conf.getProperty("synthesizer.voice.announce.volume");

						String voice = Conf.getProperty("synthesizer.voice.announce");

						if (null == speed) {
							speed = "medium";
						}
						

						if (null == volume) {
							volume = "medium";
						}
						
						
						ssml = "<speak> <prosody rate='" + speed + "' volume='" + volume + "'>";
						
						if (null != voice && !voice.equals("default")) {
							ssml += "<voice name='" + voice + "'>" + txt + "</voice>";
						} else {
							ssml += txt;
						}
						
						ssml += "</prosody></speak>";
					}
											
					synth.speakSsml(ssml);
					synth.finish();
					player = new WavPlayer(file, SynthesisEngineFactory.getInstance().getAudioFormat());
					
					if (cache) {
						m_wavCache.put(txt, player);
					} else {
						player.deleteAfterPlay();
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
				
			}
			return player;
		}
	}
	
	public WavPlayer sound(String path, boolean cache) throws Exception {
		
		synchronized (m_wavCache) {
			WavPlayer player = (WavPlayer) m_wavCache.get(path);
			
			if (null == player) {
				player = new WavPlayer(new File(path));
				
				if (cache) {
					m_wavCache.put(path, player);
				} 
			}
			
			return player;
		}
	}
	
	public EventObject nextEvent() {
		synchronized (m_eventQueue) {
			try {
				return (EventObject) m_eventQueue.removeFirst();
			} catch (NoSuchElementException e) {
				return null;
			}
		}
	}
	/**
	 * 
	 * @param uri
	 * @throws SynthesisException 
	 */
	public void setSession(String uri) throws SynthesisException {
		try {
			setSession(new URI(uri));
		} catch (URISyntaxException e) {
			throw new SynthesisException(e);
		}
	}

	public boolean hasSession(String url) {
		try {
			return hasSession(new URI(url));
		} catch (URISyntaxException e) {
			return false;
		}
	}

	public void speakHtml(String url, String uri) throws SynthesisException, MalformedURLException, URISyntaxException {
		super.speakHtml(new URL(url), new URI(uri));
	}

	public void remove(String uri) throws SynthesisException {
		try {
			super.remove(new URI(uri));
		} catch (URISyntaxException e) {
			throw new SynthesisException(e);
		}
	}
	
	public void setProperty(String name, String value) {
		Conf.setProperty(name, value);
	}
	
	public String getProperty(String name) {
		return Conf.getProperty(name);
	}
	public void removeProperty(String name) {
		Conf.removeProperty(name);
	}
	public void configurationChanged() {
		FestivalSynthesisEngine.refreshPool();
		
		
		synchronized (m_wavCache) {
			m_wavCache.clear();
		}
	}

	public MP3DumpQueue getDumpQueue() {
		return dumpQueue;
	}
}
