package jssspeak.util;

import java.io.InputStream;
import java.io.OutputStream;



public interface IOSessionWorker {
    interface Factory {
        public IOSessionWorker getSessionWorker();
    }

    public void startIOSession(InputStream input, OutputStream output);
}
