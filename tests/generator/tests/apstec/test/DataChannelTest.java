package apstec.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Properties;

public abstract class DataChannelTest extends BaseTest {
    // Properties
    private static final String DATA_PORT = "data_port";

    private DatagramChannel data_dc;
    private InetSocketAddress dataAddr;

    protected void cleanup() {
        log("cleanup data");
        closeChannel(data_dc);
        data_dc = null;
        super.cleanup();
    }

    protected boolean init(Properties props, Properties hwProps) throws IOException {
        String tmp = (tmp = props.getProperty(IP)) == null ? "192.168.71.128" : tmp;
        dataAddr = new InetSocketAddress(InetAddress.getByName(tmp.trim()),
                Integer.parseInt((tmp = props.getProperty(DATA_PORT)) == null ? "28930" : tmp.trim()));
        
        data_dc = DatagramChannel.open();
        data_dc.setOption(StandardSocketOptions.SO_RCVBUF,
                Integer.parseInt((tmp = props.getProperty(RECEIVE_BUFFER)) == null ? "65536" : tmp.trim()))
                .setOption(StandardSocketOptions.SO_SNDBUF,
                        Integer.parseInt((tmp = props.getProperty(SEND_BUFFER)) == null ? "2048" : tmp.trim()));
        data_dc.configureBlocking(false);
        data_dc.connect(dataAddr);
        return super.init(props, hwProps);
    }

    // Blocking
    protected boolean setDataLenHub(int dLen) throws IOException {
        byte[] tmp = new byte[] {84, 0, 2, 0, 0, 0};
        tmp[5] = (byte) (dLen & 0xFF);
        tmp[4] = (byte) (dLen >> 8 & 0xFF);
        data_dc.send(ByteBuffer.wrap(tmp), dataAddr);
        ByteBuffer buf = ByteBuffer.allocateDirect(dLen);
        SocketAddress soc = null;
        long timeStamp = System.currentTimeMillis();
        while ((soc = data_dc.receive(buf)) == null && System.currentTimeMillis() - timeStamp <= 60000) {
            try {
                Thread.sleep(10);
            } catch (Throwable e) {
            }
        }
        if (soc == null) {
            return false;
        }
        return buf.get(0) == 84;
    }

    protected DatagramChannel getDataChannel() {
        return data_dc;
    }
}
