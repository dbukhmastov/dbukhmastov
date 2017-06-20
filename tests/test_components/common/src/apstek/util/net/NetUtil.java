package apstek.util.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;

public abstract class NetUtil {
    public static int freeUDPPort() {
        try (DatagramSocket ds = new DatagramSocket(0)) {
            return ds.getLocalPort();
        } catch (SocketException ex) {
            return -1;
        }
    }

    public static int freeTCPPort() {
        try (ServerSocket ss = new ServerSocket(0)) {
            return ss.getLocalPort();
        } catch (IOException ex) {
            return -1;
        }
    }
}
