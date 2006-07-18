/**
 * base package of the framework implementing a synthesys system  
 */
package org.nargila.speak.framework;

import java.util.Properties;

import org.nargila.util.Event;
import org.nargila.util.EventHandler;
import org.nargila.util.EventHandlerManager;

/**
 * public singleton runtime configuration object
 * @author tshalif
 *
 */
public class Conf {
	
	/**
	 * listener interface for intercepting configuration events
	 * @author tshalif
	 *
	 */
	public interface ConfigurationChangeListener extends EventHandler {		
	}
	
	/**
	 * Configuration change event
	 * @author tshalif
	 *
	 */
	public static class ConfChange extends Event {
	
		private static final long serialVersionUID = 1L;

		/**
		 * tor with param name and value
		 * @param name configuration param name
		 * @param value new configuration param value
		 */
		public ConfChange(String name, String value) {
			super(name, value);
		}
		
		/**
		 * get name
		 * @return parameter name
		 */
		public String getName() {
			return getSource().toString();
		}
		
		/**
		 * get vallue
		 * @return new configuration value
		 */
		public String getValue() {
			return getData().toString();
		}
	}

	/**
	 * singleton Conf instance
	 */
	private static Conf s_conf;
	
	/**
	 * the Properties instance backing this Conf
	 */
    private final Properties m_props = new Properties();

    /**
     * event listeners manager
     */
    private EventHandlerManager m_confListeners = new EventHandlerManager();
    
    /**
     * private constructor - no external instanciation, only singleton allowed
     *
     */
    private Conf() {
        try {
            m_props.load(getClass().getResourceAsStream("browser.properties"));
        } catch (Exception ex) {
        	throw new RuntimeException(ex);
        }
    }

    /**
     * Initialize the singleton.
     * @return the Conf object
     */
    private static Conf getInstance() {
    	if (s_conf == null) {
    		synchronized (Conf.class) {
    	    	if (s_conf == null) {
    	    		s_conf = new Conf();
    	    	}
    		}
    	}
    	
    	return s_conf;
    }
    
    /**
     * Get conf property by name. System property by same name will override any 
     * parameter set in the Conf object.
     * @param key property name
     * @return configuration value for given name from either the System or the Conf object.
     */
    public static String getProperty(String key) {
    	return getInstance()._getProperty(key);
    }

    /**
     * Get a configuration property from ssspeak property file overriden by system properties, if exists
     * @param key property key
     * @return if System.getProperty(key) returns it's value, else return property from property file  
     */
    private String _getProperty(String key) {
    	String value = System.getProperty(key);
    	
    	if (null == value) {
    		value = m_props.getProperty(key);
    	}
    	
    	return value;
    }
    
    /**
     * Set configuration property.
     * @param key prop name
     * @param value prop value
     */
    public static void setProperty(String key, String value) {
    	setProperty(key, value, false);
    }
    
    /**
     * Set configuration property and broadcast a ConfChange event.
     * @param key prop name
     * @param value prop value
     * @param notifyListeners either to notify listeners or not about change
     */
    public static void setProperty(String key, String value, boolean notifyListeners) {
    	getInstance()._setProperty(key, value);
    	
    	if (notifyListeners) {
    		getInstance().m_confListeners.generateEvent(new ConfChange(key, value));
    	}
    }
    
   /**
     * Set configuration property (setting not saved!)
     */
    private void _setProperty(String key, String value) {
        m_props.setProperty(key, value);
    }

    /**
     * Remove property
     * @param key prop name
     */
	public static void removeProperty(String key) {
		getInstance()._removeProperty(key);
	}
	
	private void _removeProperty(String key) {
		m_props.remove(key);
	}

	/**
	 * Force a ConfChange to be broadcasted to listeners.
	 *
	 */
	public static void notifyConfChange() {
		getInstance().m_confListeners.generateEvent(new ConfChange(null, null));
	}

	/**
	 * add conf change listener.
	 * @param listener the listener.
	 */
	public static void addHandler(ConfigurationChangeListener listener) {
		getInstance().m_confListeners.addHandler(listener);
	}

	/**
	 * remove conf change listener
	 * @param listener the listener
	 */
	public static void removeHandler(ConfigurationChangeListener listener) {
		getInstance().m_confListeners.removeHandler(listener);
	}
}

