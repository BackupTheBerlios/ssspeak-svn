package org.nargila.util;

import java.util.EventListener;

/**
 * basic event handler interface.
 */
public interface EventHandler extends EventListener {
    public void handleEvent(Object event);
}