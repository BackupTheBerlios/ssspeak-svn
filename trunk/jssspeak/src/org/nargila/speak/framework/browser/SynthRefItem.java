
package org.nargila.speak.framework.browser;

import java.util.HashSet;
import java.util.Set;

import org.nargila.speak.synth.HtmlSynthesizer;

public final class SynthRefItem {
	final HtmlSynthesizer synth;
	//Set<URI> refList = new HashSet<URI>();
	Set refList = new HashSet();
	
	SynthRefItem(HtmlSynthesizer synth) {
		this.synth = synth;
	}	
}