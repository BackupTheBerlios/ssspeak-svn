package org.nargila.speak.apps.mozext;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import org.nargila.speak.apps.htmldump.HtmlDump;
import org.nargila.util.Pipe;

public class MP3DumpQueue {
	private static class DumpJob {
		URL url;
		String file;
		
		DumpJob(URL url, String file) {
			this.url = url;
			this.file = file;
		}
	}
	
	private LinkedList m_queue = new LinkedList();

	public void dump(String url, String output) throws MalformedURLException {
		dump(new URL(url), output);
	}
	
	public void dump(URL url, String output) {
		synchronized (m_queue) {
			m_queue.add(new DumpJob(url, output));
			
			if (m_queue.size() == 1) {
				Thread dumpThread = new Thread("dump thread") {
					public void run() {
						synchronized (m_queue) {
							m_queue.notifyAll();
						}
						
						do {
							DumpJob current = (DumpJob) m_queue.peek();
							setName("mp3 dump: " + current.url + " => " + current.file);
							
							try {
								Process sox = Runtime.getRuntime().exec("sox -t raw -r 16000 -s -w - -t wav -");
								Process lame = Runtime.getRuntime().exec("lame --quiet - -");
								
								new Pipe(sox.getInputStream(), lame.getOutputStream());
								Pipe lastPipe = new Pipe(lame.getInputStream(), new FileOutputStream(current.file));

								HtmlDump.dump(current.url, new BufferedOutputStream(sox.getOutputStream()));
								
								lastPipe.join();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							synchronized (m_queue) {
								m_queue.poll();
								m_queue.notifyAll();
							}
						} while (!m_queue.isEmpty());
					}
				};
				
				dumpThread.start();
				
				try {
					m_queue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	public void waitQueue() {
		synchronized (m_queue) {
			while (!m_queue.isEmpty()) {
				try {
					m_queue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

}
