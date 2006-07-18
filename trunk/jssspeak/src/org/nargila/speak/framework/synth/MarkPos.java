
package org.nargila.speak.framework.synth;

/**
 * An object holding an SSML mark and it's associated speech audio offset.
 * @author tshalif
 *
 */
public class MarkPos {
	/**
	 * SSML mark name
	 */
	public String name;
	
	/**
	 * speech audio offset
	 */
	public long pos;
	
	/**
	 * ctor with name and offset
	 * @param name SSML mark name or null
	 * @param pos audio offset or -1
	 */
	public MarkPos(String name, long pos) {
		this.name = name;
		this.pos = pos;
	}
	
	/**
	 * ctor with SSML mark name
	 * @param name mark name
	 */
	public MarkPos(String name) {
		this(name, -1);
	}
}