package org.nargila.speak.framework.synth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nargila.speak.event.player.PlayerOffsetEvent;
import org.nargila.speak.event.synth.SynthesisEngineEvent;
import org.nargila.speak.event.synth.SynthesisEngineFinishedEvent;
import org.nargila.speak.event.synth.SynthesisEngineOffsetEvent;
import org.nargila.speak.event.synth.SynthesizerMarkEvent;
import org.nargila.util.EventHandler;
import org.nargila.util.Xslt;
import org.w3c.dom.Node;

/**
 * Helper class for managing SSML synthesizer marks. The mark manager
 * is responsible for the construction of the mark to audio offset mapping.
 * It does so by initialy constructing a list of marks or the given SSML input node. 
 * It then listens for SynthesizerEngine's mark offset events to resolve
 * the offset mapping of the next yet-to-resolve mark in the list. 
 * @author tshalif
 *
 */
class MarkManager implements EventHandler {

	//private Map<String, MarkPos> m_mark2offsetMap = new HashMap<String, MarkPos>();
	/**
	 * SSML mark=>MarkPos mapping (Map&lt;String, MarkPos&gt;)
	 */
	private Map m_mark2offsetMap = new HashMap();
	/**
	 * List of marks in same order as in the SSML input
	 */
	private MarkPos[] m_markList;
	//private Map<Long, MarkPos> m_offset2markMap = new HashMap<Long, MarkPos>();
	/**
	 * Speech audio offset=>mark reverse mapping
	 */
	private Map m_offset2markMap = new HashMap();
	
	/**
	 * Index of next unresolved mark in m_markList 
	 */
	private int m_counter = 0;
	
	/**
	 * The synthesizer holding this MarkManager object
	 */
	private SynthesizerImpl m_synthesizer;
	
	
	/**
	 * ctor with owner synthesizer object
	 * @param synthesizer our owner
	 */
	MarkManager(SynthesizerImpl synthesizer) {
		m_synthesizer = synthesizer;
		//synthesizer.getPlayer().addPlayListener(this);
	}
	
	/**
	 * notify all objects waiting on any MarkPos to resolve 
	 *
	 */
	void notifyMarks() {
		Iterator i = Arrays.asList(m_markList).iterator();
		
		while (i.hasNext()) {
			MarkPos markPos = (MarkPos) i.next();
			
			synchronized (markPos) {
				markPos.notifyAll();
			}
		}		
	}
	
	/**
	 * setup mark manager
	 * @param ssml input SSML node
	 */
	void setup(Node ssml) {
		clear();
		
		try {
			m_markList = makeMarkList(ssml);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		m_counter = 0;
	}
	
	/**
	 * release (notify) any object waiting on a mark pos and clear mappings 
	 *
	 */
	private void clear() {
		if (null != m_markList) {
			notifyMarks();
		}
		
		m_mark2offsetMap.clear();
		m_offset2markMap.clear();
	}

	/**
	 * generate mark list from given SSML input
	 * @param ssml input SSML node
	 * @return array of MarkPos in same order as in the SSML input
	 * @throws Exception
	 */
	private MarkPos[] makeMarkList(Node ssml) throws Exception {
		Node[] marks = Xslt.xpath(ssml, "//mark/@name");
		
		//List<MarkPos> list = new LinkedList<MarkPos>();
		List list = new LinkedList();
		
		Iterator i = Arrays.asList(marks).iterator();
		
		while (i.hasNext()) {
			Node n = (Node) i.next();
			String mark = n.getNodeValue();
			
			MarkPos markPos = new MarkPos(mark); 
			list.add(markPos);
			m_mark2offsetMap.put(mark, markPos);
		}
		
		return (MarkPos[]) list.toArray(new MarkPos[list.size()]);
	}

	/**
	 * Resolve mark<=>offset mapping of next mark in list.
	 * This is done in response to an offset event sent by the
	 * SynthesizerEngine's synthesis process. Any object waiting on
	 * the resolved MarkPos is notified.
	 * 
	 * @param off resolved offset of the next MarkPos in list
	 */
	private void resolveOffset(long off) {
		MarkPos markPos = m_markList[m_counter++];
		
		/*
		 * some skip operation may be waiting on a markPos
		 * to have it's offset value set
		 */
		synchronized (markPos) {
			markPos.pos = off;
			markPos.notifyAll();
		}
		
		m_offset2markMap.put(new Long(off), markPos);
		
		m_synthesizer.addPosMark(off);
	}

	/**
	 * Get the MarkPos object for the given SSML mark.
	 * @param s the SSML mark
	 * @return MarkPos object for the given SSML mark
	 */
	MarkPos getMarkPos(String s) {
		return (MarkPos) m_mark2offsetMap.get(s);
	}
	
	/**
	 * Handle events from both the player side and synthesis engine side of the synthesis process.
	 * Events from the SynthesisEngine are generated when the speech audio data is produced allowing
	 * the resolution of the mark<=>offset mappings. Later, the Player sends to this mark manager
	 * PlayerOffset events, which can than be translated to SynthesizerMark events.
	 * 
	 *  @param event the player/engin event trigering this action 
	 */
	public void handleEvent(Object event) {
		if (event instanceof PlayerOffsetEvent) {
			long off = ((PlayerOffsetEvent)event).getOffset();
			MarkPos markPos = (MarkPos) m_offset2markMap.get(new Long(off));
			String name = (null == markPos ? "" : markPos.name);
			m_synthesizer.generateEvent(new SynthesizerMarkEvent(m_synthesizer.getId(), name));
		} else if (event instanceof SynthesisEngineEvent) {
			if (event instanceof SynthesisEngineFinishedEvent) {
				m_synthesizer.setGrowing(false);
			} else if (event instanceof SynthesisEngineOffsetEvent) {
				resolveOffset(((SynthesisEngineOffsetEvent)event).getOffset());
			}
		}				
	}
}
