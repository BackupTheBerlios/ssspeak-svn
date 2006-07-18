package jssspeak.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;


public class IOSessionServer extends ServerSocket implements Runnable {
    IOSessionWorker.Factory m_factory;

    public IOSessionServer(int port, IOSessionWorker.Factory factory) throws IOException {
        super(port);

        m_factory = factory;

    }

    public void run() {
        while (true) {
            try {
                final Socket conn = accept();
                
                new Thread() {
                    public void run() {
                        try {
                            IOSessionWorker worker = m_factory.getSessionWorker();
                            worker.startIOSession(conn.getInputStream(), conn.getOutputStream());
                        } catch (Exception e){
                            e.printStackTrace();
                        } // end of try-catch
                    }
                }.start();
            } catch (IOException e){
                e.printStackTrace();
            } // end of try-catch
        }
    }

    public static void main(String[] argv) throws IOException {
        int port = Integer.parseInt(argv[0]);

        IOSessionWorker.Factory factory = new IOSessionWorker.Factory() {
                public IOSessionWorker getSessionWorker() {
                    return new IOSessionWorker() {
                            public void startIOSession(InputStream input, OutputStream output) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                                Writer writer = new OutputStreamWriter(output);

                                String line = null;

                                try {
                                    while (null != (line = reader.readLine())) {
                                        writer.write(line + "\n");
                                        writer.flush();
                                    }
                                } catch (IOException e){
                                } 
                            }
                        };
                }
            };

        IOSessionServer svr = new IOSessionServer(port, factory);

        svr.run();
    }
}
