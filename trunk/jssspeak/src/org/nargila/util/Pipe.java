/**
 * 
 */
package org.nargila.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Pipe extends Thread {
	private InputStream input;
	private OutputStream output;
	
	final Object synchObj = "";

	public Pipe(InputStream input, OutputStream output) {
		this.input = input;
		this.output = output;
								
		synchronized (synchObj) {
			start();
			
			try {
				synchObj.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void run() {
		synchronized (synchObj) {
			synchObj.notify();
		}
		
		byte[] buff = new byte[1024];
		
		int len = 0;
		
		try {
			while ((len = input.read(buff)) != -1) {
				output.write(buff, 0, len);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
}