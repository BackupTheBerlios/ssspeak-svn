/**
 * festival based implementation of the SynthesisEngine
 */
package org.nargila.speak.impl.synth.festival;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.xml.transform.TransformerException;

import org.nargila.speak.event.synth.SynthesisEngineOffsetEvent;
import org.nargila.speak.framework.Conf;
import org.nargila.speak.framework.Conf.ConfChange;
import org.nargila.speak.framework.player.Player;
import org.nargila.speak.framework.synth.SynthesisEngine;
import org.nargila.speak.framework.xslt.XsltHelper;
import org.nargila.speak.synth.SynthesisException;
import org.nargila.util.AsyncJob;
import org.nargila.util.AsyncJobRunnable;
import org.nargila.util.EventHandler;
import org.nargila.util.Xslt;
import org.w3c.dom.Node;

/**
 * Implementation of SynthesisEngine using wrappers arround festival.
 * Festival is executed as an external program and it is communicated with
 * via its stdin, stdout and stderr.
 * The tricky part is to force festival to produce SSML marks. This is acheived
 * by embeding significantly short audio beeps into the SABLE file (the only markup format
 * understandable by festival) then intercepting them during the
 * synthesis phase and calculating their offset. Crude, but it works.  
 * @author tshalif
 *
 */
public class FestivalSynthesisEngine extends AsyncJob implements SynthesisEngine {
	
	/**
	 * Festival process pool. To shorten synthesis latency, a few 
	 * ready to use festival processes should always be ready to us.
	 * @author tshalif
	 *
	 */
	private static class ProcessPool extends Thread implements Conf.ConfigurationChangeListener {
		private static final int DEFAULT_POOL_SIZE = 3;
		
		// <Process>
		/**
		 * List of ready to use Process objects
		 */
		private LinkedList m_pool = new LinkedList();

		/**
		 * Path to the festival data lib (or null)
		 */
		private String m_festivalLib;

		/**
		 * path to festival executable (or null)
		 */
		private String m_festivalExecutable;
		
		ProcessPool() {
			super("Festival Process Pool");
			start();
		}
		
		/**
		 * Get pool size
		 * @return number of processes in pool
		 */
		private int getPoolSize() {
			int retval;
			
			try {
				retval = Integer.valueOf(Conf.getProperty("festival.procPoolSize")).intValue();
				retval = Math.max(1, retval);
			} catch (NumberFormatException e) {
				retval = DEFAULT_POOL_SIZE;
			}
			
			return retval;
		}

		/** 
		 * Getn next available process
		 * @return the festival Process object
		 */
		Process getProcess() {
			Process retval = null;
			
			while (null == retval) {
				synchronized (m_pool) {
					while (m_pool.isEmpty()) {
						try {
							m_pool.wait();
						} catch (InterruptedException e) {
						}
					}
					
					retval = (Process) m_pool.removeFirst();
					
					m_pool.notifyAll(); // notify pool to create new process
				}
				
				{ // check process is not a dead one
					try {
						retval.exitValue(); // should throw exception
						continue; // dead pool process.. get another one
					} catch (IllegalThreadStateException e) {}
				}
			}
			
			return retval;			
		}
				
		public void run() {
			while (true) {				
				boolean pathChanged = resolveFestivalPaths();
				
				/*
				 * uppon runtime festival configuration chane,
				 * refresh pool with new instances 
				 */
				if (pathChanged) {
					clearPool();
				}
				synchronized (m_pool) {
					while (m_pool.size() < getPoolSize()) {
						try {
							createProcess();
						} catch (IOException e) {
							e.printStackTrace();
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e1) {
							}
						}
						Thread.yield();
					}
					
					try {
						m_pool.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}

		/** 
		 * remove all festival process.
		 *
		 */
		private void clearPool() {
			synchronized (m_pool) {
				Iterator i = m_pool.iterator();
				
				while (i.hasNext()) {
					Process fest = (Process) i.next();
					fest.destroy();
				}
				
				m_pool.clear();
				
				m_pool.notifyAll();
			}
		}

		/**
		 * Create a single ready-to-use festival process
		 * @throws IOException
		 */
		private void createProcess() throws IOException {
			Process fest = executeFestival();
			
			synchronized (m_pool) {
				m_pool.add(fest);
				m_pool.notifyAll();
			}			
		}

		/**
		 * Figure out which festival executable, srcdir, etc to use
		 * @return
		 */
		private boolean resolveFestivalPaths() {
			boolean changed = false;						
			
			String[] oldvals = new String[] {
					m_festivalExecutable,
					m_festivalLib
			};

			m_festivalLib = Conf.getProperty("festival.lib");
			m_festivalExecutable = Conf.getProperty("festival.prog");
			
			if (null == m_festivalExecutable) {
				String festival_dir = Conf.getProperty("festival.dir");
				
				if (null != festival_dir) {
					m_festivalExecutable = festival_dir + File.separator + "bin" + File.separator + "festival";
					
					if (null == m_festivalLib) {
						m_festivalLib = festival_dir + File.separator + "lib";
					}
				} else {		
					m_festivalExecutable = "festival"; // use whatever installed festival executable in path
				}
			}
			
			{ // cygwin path compatibility
			    if (null != m_festivalLib) {
				if (Boolean.valueOf(Conf.getProperty("festival.cygwin")).booleanValue()) {
				    m_festivalLib = m_festivalLib.replaceAll("\\\\", "/").replaceAll("^([a-zA-Z]):", "/cygdrive/$1"); // this will make cygwin compatible path on windows
				}
			    }
			}

			String[] newvals = new String[] {
					m_festivalExecutable,
					m_festivalLib
			};

			logger.info("festivalExecutable: " + m_festivalExecutable);
			logger.info("festivalLib: " + m_festivalLib);
			
			changed = !Arrays.equals(oldvals, newvals);
			
			return changed;
		}

		/**
		 * Execute (create) a festival Process
		 * @return a festival Process
		 * @throws IOException
		 */
		private Process executeFestival() throws IOException {
			//List<String> args = new LinkedList<String>();
			List args = new LinkedList();
			
			String cmd = "";

			args.add(m_festivalExecutable);
			
			cmd += m_festivalExecutable;

			args.add("--pipe");

			cmd += " --pipe";

			if (null != m_festivalLib) {
				args.add("--libdir");
				cmd += " --libdir ";
				args.add(m_festivalLib);
				cmd += m_festivalLib;
			}
			
			logger.info("executing: " + cmd);

			String[] cmdarr = (String[]) args.toArray(new String[args.size()]);
			
			return Runtime.getRuntime().exec(cmdarr);
		}

		public void handleEvent(Object event) {
			assert event instanceof Conf.ConfChange: "unexpected object type";
		
			Conf.ConfChange confChange = (ConfChange) event;
			
			if (null != confChange.getName()) {
				final String name = confChange.getName();
				
				if (name.equals("festival.lib") || name.equals("festival.dir")) {
					clearPool();
				}
			}
		}
	}
	
	/**
	 * The shared festival Process pool
	 */
	private static ProcessPool s_processPool = new ProcessPool();
	
	static Logger logger = Logger.getLogger(FestivalSynthesisEngine.class.getName()); 

	/**
	 * The festival Process backing this synthesis engine
	 */
	private Process m_sable2wave;
	
	/**
	 * the listener on this festival synth job
	 */
	private EventHandler m_engineListener;
	
	/**
	 * The output for writing the resulting speech audio data into
	 */
	private OutputStream m_output;
	
	/**
	 * The sable data
	 */
	private Node m_sable;

	public FestivalSynthesisEngine() {
		super("Sable2Wave");
		
		setJobs(new AsyncJobRunnable[]{new SynthesisJob(), new SynthesisOutputFilter()});
	}
	
	
	public void synth(Node ssml, OutputStream output, EventHandler engineListener) throws SynthesisException {		
		m_sable2wave = s_processPool.getProcess();			

		try {
			m_sable = XsltHelper.xslt("ssml2sable", ssml, new String[]{"sounddir=" + Conf.getProperty("ssspeakDir") + "/share/sounds/ssspeak"});
		} catch (TransformerException e) {
			throw new SynthesisException(e);
		}
		
		m_output = output;
		m_engineListener = engineListener;
		
		this.start();
	}
	
	protected void onAbort() {
		if (null != m_sable2wave) m_sable2wave.destroy();
	}

	protected void onDone() {
		try {
			m_output.close();
		} catch (IOException e) {
		}

		m_sable2wave.destroy();
	}

	public String[] getVoices() throws SynthesisException {
		Process festival = s_processPool.getProcess();
		
		Writer out = new OutputStreamWriter(festival.getOutputStream());
		
		BufferedReader ins = new BufferedReader(new InputStreamReader(festival.getInputStream()));
		
		String lisp = "(let  ((voices (voice.list))) (while voices (print (car voices)) (set! voices (cdr voices))))";
		
		try {
			out.write(lisp);
			out.close();
			
			String line;
			
			//List<String> voices = new LinkedList<String>();
			List voices = new LinkedList();
			
			while ((line = ins.readLine()) != null) {
				voices.add(line);
			}		
			
			try {
				festival.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (false && 0 != festival.exitValue()) {
				StringBuffer buff = new StringBuffer();
				
				BufferedReader err = new BufferedReader(new InputStreamReader(festival.getErrorStream()));
				
				while ((line = err.readLine()) != null) {
					buff.append(line + "\n");
				}
				
				throw new RuntimeException("festival: " + buff.toString());
			}
			
			ins.close();
			
			return (String[]) voices.toArray(new String[voices.size()]);
		} catch (IOException e1) {
			throw new SynthesisException(e1);
		}
	}
	
	/**
	 * Read resource into string
	 * @param name name of resource
	 * @return resource as string
	 * @throws IOException
	 */
	private String readResource(String name) throws IOException {
		InputStream resource = this.getClass().getResourceAsStream(name);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		copyStream(resource, bout);
		
		resource.close();
		bout.close();
		
		return bout.toString("utf-8");
	}
	

	/**
	 * Specialized Thread job to feed festival Process output into the output consumer of this synthesis job
	 * @author tshalif
	 *
	 */
	class SynthesisJob implements AsyncJobRunnable {


		public String getName() {
			return "SynthesisJob";
		}
		
		public void run(JobControl controler)  throws InterruptedException {
			
			
			InputStream ins = m_sable2wave.getInputStream();
			
			try {					
				writeScheme();
				
				byte[] buff = new byte[Player.WRITE_GRANULARITY];
				
				for (int len = ins.read(buff);  controler.check() && len != -1; len = ins.read(buff)) {
					m_output.write(buff, 0, len);

					Thread.yield();
				}				
			} catch (SynthesisException e) {
				FestivalSynthesisEngine.this.abort();
				throw new RuntimeException(e);
			} catch (IOException e) {
				if (!controler.isAborted()) {
					controler.abort();
					throw new RuntimeException(e);
				}
			} 
		}};

	/**
	 * Specialized Thread job for scanning festival's stderr output for mark offsets.
	 * @author tshalif
	 *
	 */
	class SynthesisOutputFilter implements AsyncJobRunnable {


		public String getName() {
			return "SynthesisOutputFilter";
		}

		public void run(JobControl controler) throws InterruptedException  {
			
			try {
				BufferedReader markInput = 
					new BufferedReader(new InputStreamReader(m_sable2wave.getErrorStream()));
				
				for (String line = markInput.readLine(); controler.check() && null != line; line = markInput.readLine()) {
					if (line.startsWith("mark:")) {
						String[] markToken = line.split(":");
						
						assert markToken.length == 2: "mark tokens returned from sable2wave should be of form 'mark:<sample_offset>'";
						
						long pos = Long.parseLong(markToken[1]);
						
						if (m_engineListener != null) {
							m_engineListener.handleEvent(new SynthesisEngineOffsetEvent(this, pos));
						}
					} else {
						System.err.println(line);
					}
				}
			} catch (IOException ex) {
				if (!controler.isAborted()) {
					controler.abort();
					
					throw new RuntimeException(ex);
				}
			}
		}};
	
		
	/**
	 * Generate a sable file from the DOM Node
	 * @return unix/cygwin compatilbe normalized path of generated file
	 * @throws IOException
	 * @throws TransformerException
	 * @throws FileNotFoundException
	 * @throws MalformedURLException
	 */
	private String generateSable() throws IOException, TransformerException, FileNotFoundException, MalformedURLException {		
		File sablef = File.createTempFile("sable2wave", ".sable");
		
		if (!Boolean.valueOf(Conf.getProperty("debug.keepFiles")).booleanValue()) {
			sablef.deleteOnExit();
		}
		
		Xslt.serialize(m_sable, sablef.getPath());
					
		String path = sablef.toURL().getPath();
		
		if (isWindows()) {
			path = path.substring(1); // remove the leading '/' e.g. from '/c:/tmp/bula' to c:/tmp/bula
		}
		
		return path;
	}

	/**
	 * check windows is current operating system
	 * @return true if running on windows
	 */
	private boolean isWindows() {
		return File.separatorChar == '\\';
	}

	private void copyStream(InputStream ins, OutputStream out) throws IOException {
		byte[] buff = new byte[Player.WRITE_GRANULARITY];
	
		for (int len = ins.read(buff); len != -1; len = ins.read(buff)) {
			out.write(buff, 0, len);
			Thread.yield();
		}				
	}

	private void writeScheme() throws SynthesisException {
		try {
			String scheme = readResource("sable2wave.scm");
			
			String sablePath = generateSable();
			
			scheme = scheme.replaceAll("@SABLE_FILE@", sablePath);
			
			OutputStream festival = m_sable2wave.getOutputStream();

			ByteArrayInputStream sable2wav = new ByteArrayInputStream(scheme.getBytes("utf-8"));
			
			copyStream(sable2wav, festival);
			
			sable2wav.close();
			festival.close();
		} catch (MalformedURLException e) {
			throw new SynthesisException(e);
		} catch (UnsupportedEncodingException e) {
			throw new SynthesisException(e);
		} catch (IOException e) {
			throw new SynthesisException(e);
		} catch (TransformerException e) {
			throw new SynthesisException(e);
		}
	}

	/**
	 * Refresh pool (i.e. delete all Process and create new ones)
	 *
	 */
	public static void refreshPool() {
		s_processPool.clearPool();
	}


	public AudioFormat getAudioFormat() {
		return new RawAudioFormat();
	}
}
