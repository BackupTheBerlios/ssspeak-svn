package org.nargila.speak.apps.htmldump;

import java.io.OutputStream;
import java.net.URL;

import org.nargila.speak.framework.synth.html.HtmlSynthesizerImpl;

/**
 * Dumps HTML as raw wav to stdout
 * @author tshalif
 */
public class HtmlDump {
	/**
	 * @param args first argument must be an HTML URL to retrieve
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		dump(new URL(args[0]), System.out);
	}

	public static void dump(URL url, OutputStream stream) throws Exception {
		HtmlSynthesizerImpl synth = new HtmlSynthesizerImpl(stream);
		synth.setId(url);
		synth.speakHtml(url);
		synth.finish();
	}

}
