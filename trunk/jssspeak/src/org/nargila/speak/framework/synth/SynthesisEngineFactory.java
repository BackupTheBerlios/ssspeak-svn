package org.nargila.speak.framework.synth;

import org.nargila.speak.framework.Conf;
import org.nargila.speak.impl.synth.festival.FestivalSynthesisEngine;

/**
 * Helper factory for getting a SynthesisEngine instance
 * @author tshalif
 *
 */
public class SynthesisEngineFactory {
	
	/**
	 * Get a SynthesisEngine instance. The default implementation
	 * provided can be overriden by setting the Conf or System property
	 *  "org.nargila.speak.framework.synth.SynthesisEngine"
	 *  
	 * @return an object implementing SynthesisEngine
	 */
	public static SynthesisEngine getInstance() {
		
		String implClass = Conf.getProperty("org.nargila.speak.framework.synth.SynthesisEngine");
		
		if (null != implClass) {
			try {
				return (SynthesisEngine) Class.forName(implClass).newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		
		return new FestivalSynthesisEngine();
	}
}
