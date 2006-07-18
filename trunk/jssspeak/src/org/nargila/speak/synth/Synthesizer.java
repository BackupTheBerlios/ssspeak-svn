/**
 * package for user-end synthesizer system interfaces, basic classes
 */
package org.nargila.speak.synth;

import org.nargila.util.EventHandler;
import org.nargila.util.Job;
import org.w3c.dom.Node;

public interface Synthesizer extends Job {

	public void speakSsml(Node ssml) throws SynthesisException;

	public void speakSsml(String ssml) throws SynthesisException;

	public void speakPlainText(String text) throws SynthesisException;

	public void skip(String mark)  throws SynthesisException;

	public void skip(int sec)  throws SynthesisException;

	public long getLength();

	public long getPlayPos();

	public int getProgress();

	public boolean isGrowing();

	public void seek(long offset)  throws SynthesisException;

	public void cancelSkip();

	public void addSynthListener(EventHandler synthesizerListener);

	public void removeSynthListener(EventHandler synthesizerListener);

	public void disableSound(Object id);

	public void enableSound(Object id);
	
	public boolean isEnabledSound(Object id);
	
	public String[] getVoices() throws SynthesisException;

	public void setId(Object id);
}