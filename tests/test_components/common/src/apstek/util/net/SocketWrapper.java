package apstek.util.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketWrapper extends Socket {
    private final InputStream in;

    public SocketWrapper(String host, int port) throws UnknownHostException, IOException {
        super(host, port);
        setKeepAlive(true);
        in = getInputStream();
    }

    public SocketWrapper(String host, int port, int timeout) throws UnknownHostException,
            IOException {
        super(host, port);
        setKeepAlive(true);
        setSoTimeout(timeout);
        in = getInputStream();
    }

    @Override
    public synchronized void close() throws IOException {
        // exhaust input stream before closing
        try {
            if (in != null) {
                while (in.available() > 0) {
                    in.read();
                }
            }
        } catch (IOException ex) {
            // just ignore
        }
        super.close();
    }
}
