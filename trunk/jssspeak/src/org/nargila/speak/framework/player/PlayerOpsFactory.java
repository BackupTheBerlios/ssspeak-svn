package org.nargila.speak.framework.player;

import org.nargila.speak.framework.Conf;
import org.nargila.speak.impl.player.sampled.JavaPlayerOps;

/**
 * Factory responsible for getting a PlayerOps object.
 * @author tshalif
 *
 */
public class PlayerOpsFactory {
	/**
	 * Get a PlayerOps instance. The default implementation
	 * provided can be overriden by the Conf or System property
	 * "org.nargila.speak.framework.player.PlayerOps"
	 * @see PlayerOps
	 * @return a PlayerOps instance
	 */
	public static PlayerOps getInstance() {
		String implClass = Conf.getProperty("org.nargila.speak.framework.player.PlayerOps");
		
		if (null != implClass) {
			try {
				return (PlayerOps) Class.forName(implClass).newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return new JavaPlayerOps();
	}
}
