package apstec.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Map.Entry;
import java.util.Properties;

import com.sun.javatest.Status;
import com.sun.javatest.Test;

public abstract class BaseTest implements Test {
    // Properties
    private static final String CMD_PORT = "cmd_port";
    protected static final String IP = "ip";
    protected static final String RECEIVE_BUFFER = "receive_buffer";
    protected static final String SEND_BUFFER = "send_buffer";
    private static final String SOCKET_TIMEOUT = "socket_timeout";
    private static final String STRESS_TEST_TIME = "stress_test_time";
    private static final String TIMEOUT = "timeout";

    private static final String OK = "OK";
    private static final String FAIL = "FAIL";
    private static final String RESOURCE = "/apstec/testinfo";
    private static final String HARDWARE_CONFIG = "/apstec/hw_config";
    private DatagramChannel cmd_dc;
    private InetSocketAddress cmdAddr;
    private PrintWriter out, err;
    protected byte[][] boards = new byte[3][]; // 0 - UPSU, 1 - POVS, 2 - GEN
    protected byte[][] ids = new byte[3][]; // 0 - UPSU, 1 - POVS, 2 - GEN
    protected long timeout, stressTestTime, socketTimeout;

    public static void main(Test test, String[] args) {
        PrintWriter err = new PrintWriter(System.err, true);
        PrintWriter out = new PrintWriter(System.out, true);
        Status s = test.run(args, out, err);
        s.exit();
    }

    protected void log(Object... strs) {
        print(out, strs);
    }

    protected void error(Object... strs) {
        print(err, strs);
    }

    private void print(PrintWriter ps, Object... strs) {
        for (Object next : strs) {
        	if (next instanceof ByteBuffer) {
        		next = toString((ByteBuffer) next);
        	}
            ps.print(next.toString());
        }
        ps.println();
    }

    public final Status run(String[] args, PrintWriter out, PrintWriter err) {
        try {
            if (init(RESOURCE, out, err) && runTest()) {
                return Status.passed(OK);
            }
        } catch (Throwable th) {
            error("Unexpected Throwable: " + th.getClass().getName() + ":" + th.getMessage());
            th.printStackTrace();
        } finally {
            cleanup();
        }
        return Status.failed(FAIL);
    }

    protected void cleanup() {
        log("cleanup cmd");
        closeChannel(cmd_dc);
        cmd_dc = null;
    }

    protected void closeChannel(DatagramChannel dc) {
        cleanupChannel(dc);
        try {
            dc.disconnect();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            dc.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    protected boolean cleanupCommandChannel() {
    	return cleanupChannel(cmd_dc);
    }

    protected boolean cleanupChannel(DatagramChannel dc) {
    	boolean blockingFlag = dc.isBlocking();
        try {
            if (blockingFlag) {
                dc.configureBlocking(!blockingFlag);
            }
            ByteBuffer tmp = ByteBuffer.allocateDirect(64);
            while (dc.read(tmp) > 0) {
                tmp.clear();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        } finally {
        	try {
				dc.configureBlocking(blockingFlag);
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
        }
        return true;
    }

    private boolean init(String resource, PrintWriter out, PrintWriter err) throws IOException {
        this.out = out;
        this.err = err;
        Properties props = new Properties();
        try (InputStream is = resource.getClass().getResourceAsStream(resource)) {
            props.load(is);
            for (Entry<Object, Object> next : props.entrySet()) {
                log(next.getKey(), "=", next.getValue());
            }
        } catch (Throwable e) {
        }
        
        Properties hwProps = new Properties();
        try (FileInputStream fis = new FileInputStream(new File(props.getProperty("hw_config")))) {
        	hwProps.load(fis);
    	} catch (Throwable e) {
    		try (InputStream is = resource.getClass().getResourceAsStream(HARDWARE_CONFIG)) {
    			hwProps.load(is);
    		} catch (Throwable ee) {
    			ee.printStackTrace();
    		}
		}
        String tmp = (tmp = props.getProperty(IP)) == null ? "192.168.71.128" : tmp;
        String[] tmp2 = tmp.split("\\.");
        if (tmp2.length != 4) {
            error("BaseTest: " + tmp);
            throw new IOException("BaseTest: wrong board IP: " + tmp);
        }
        final byte[] tmp3 = new byte[tmp2.length];
        for (int i = 0; i < tmp2.length; i++) {
            tmp3[i] = (byte) Integer.parseInt(tmp2[i].trim());
        }
        cmdAddr = new InetSocketAddress(InetAddress.getByAddress(tmp3),
                Integer.parseInt((tmp = props.getProperty(CMD_PORT)) == null ? "28929" : tmp.trim()));
        cmd_dc = DatagramChannel.open();
        cmd_dc.setOption(StandardSocketOptions.SO_RCVBUF,
                Integer.parseInt((tmp = props.getProperty(RECEIVE_BUFFER)) == null ? "65536" : tmp.trim()))
                .setOption(StandardSocketOptions.SO_SNDBUF,
                        Integer.parseInt((tmp = props.getProperty(SEND_BUFFER)) == null ? "2048" : tmp.trim()));
        cmd_dc.configureBlocking(false);
        cmd_dc.connect(cmdAddr);

        timeout = Integer.parseInt((tmp = props.getProperty(TIMEOUT)) == null ? "120000" : tmp.trim());
        stressTestTime = Integer.parseInt((tmp = props.getProperty(STRESS_TEST_TIME)) == null ? "120000" : tmp.trim());
        socketTimeout = Integer.parseInt((tmp = props.getProperty(SOCKET_TIMEOUT)) == null ? "60000" : tmp.trim());

        ByteBuffer buf = sendReceiveCmd(new byte[] {100, 0, 0, 0}, 0, 4, 128); // read config read 32 bytes
        if (buf != null) {
            boards[0] = new byte[4]; // 0 - UPSU
            boards[1] = new byte[8]; // 1 - POV
            boards[2] = new byte[1]; // 2 - GEN
            ids[0] = new byte[4]; // 0 - UPSU
            ids[1] = new byte[8]; // 1 - POVS
            ids[2] = new byte[1]; // 2 - GEN
            int idx1 = 0, idx2 = 0, idx3 = 0;
            byte[] cmd = new byte[] {2, 0, 0, 0};
            for (byte i = 0; i < 64; i++) {
                cmd[1] = i;
                byte b0 = buf.get(i * 2);
                byte b1 = buf.get(i * 2 + 1);
                if ((((int) b1) & 0xff) == 0x81) { // UPSU
                    log("new UPSU " + i + ", ID=" + b0 + ", index" + idx1);
                    boards[0][idx1] = i; // sub address
                    ids[0][idx1++] = b0; // id
                    // ByteBuffer cfg = sendReceiveCmd(cmd, 0, 4, 32);
                    // log("data ", toString(cfg));
                } else if ((((int) b1) & 0xff) == 0x82) { // POVS
                    log("new POVS " + i + ", ID=" + b0 + ", index" + idx2);
                    boards[1][idx2] = i; // sub address
                    ids[1][idx2++] = b0; // id
                    // ByteBuffer cfg = sendReceiveCmd(cmd, 0, 4, 32);
                    // log("data ", toString(cfg));
                } else if ((((int) b1) & 0xff) == 0x83) { // GEN
                    log("new GEN " + i + ", ID=" + b0 + ", index" + idx3);
                    boards[2][idx3] = i; // sub address
                    ids[2][idx3++] = b0; // id
                    // ByteBuffer cfg = sendReceiveCmd(cmd, 0, 4, 32);
                    // log("data ", toString(cfg));
                }
            }
            // correct length of arrays
            correctLength(boards, 0, idx1);
            correctLength(boards, 1, idx2);
            correctLength(boards, 2, idx3);
            correctLength(ids, 0, idx1);
            correctLength(ids, 1, idx2);
            correctLength(ids, 2, idx3);
            log("HUB found ", idx1 + idx2 + idx3, " devices");
            return init(props, hwProps);
        } else {
            error("Can't recieve devices config.");
            return false;
        }
    }
    
    private void correctLength(byte[][] target, int index, int length) {
    	if (target[index].length != length) {
        	byte fTmp[] = target[index];
        	target[index] = new byte[length];
            System.arraycopy(fTmp, 0, target[index], 0, length);
        }
    }

    protected boolean init(Properties props, Properties hwProps) throws IOException {
        return true;
    }

    // Blocking
    protected ByteBuffer sendReceiveCmd(byte[] send, int offset, int length, int receive) throws IOException {
        // log("sendReceiveCmd send");
        cmd_dc.send(ByteBuffer.wrap(send), cmdAddr);
        ByteBuffer buf = ByteBuffer.allocateDirect(receive);
        // log("sendReceiveCmd receive");
        SocketAddress soc = null;
        long timeStamp = System.currentTimeMillis();
        while ((soc = cmd_dc.receive(buf)) == null && System.currentTimeMillis() - timeStamp <= socketTimeout) {
        }
        if (soc == null) {
            return null;
        }
        return buf;
    }

    // UnBlocking
    protected void send(byte[] send, int offset, int length) throws IOException {
        // log("CMD send");
        cmd_dc.send(ByteBuffer.wrap(send), cmdAddr);
    }

    // Blocking
    protected ByteBuffer receive(int length) throws IOException {
        // log("CMD receive");
        ByteBuffer buf = ByteBuffer.allocateDirect(length);
        SocketAddress soc = null;
        long timeStamp = System.currentTimeMillis();
        while ((soc = cmd_dc.receive(buf)) == null && System.currentTimeMillis() - timeStamp <= socketTimeout) {
        }
        if (soc == null) {
            return null;
        }
        return buf;
    }

    // Blocking
    protected ByteBuffer receivePart(int length) throws IOException {
        // log("CMD receivePart");
        ByteBuffer buf = ByteBuffer.allocateDirect(length);
        long timeStamp = System.currentTimeMillis();;
        while (cmd_dc.read(buf) > 0 && buf.position() < length - 1
                && System.currentTimeMillis() - timeStamp <= socketTimeout) {
        }
        return buf;
    }
    
    // send stop command to HUB
    protected boolean stopHub() {
        boolean result = true;
        byte[] stopCmd = new byte[] {15, 41, 0, 0};
        try {
            ByteBuffer resp = sendReceiveCmd(stopCmd, 0, 4, 64);
            if (resp != null && resp.position() > 0) {
                for (int i = 0; i < boards.length && result; i++) {
                    byte[] type = boards[i];
                    for (int j = 0; j < type.length && result; j++) {
                        if (resp.get(type[j]) == 0) {
                            log("Can't stop!! type=", i, ", index=", j, ", subaddr=", type[j]);
                            result = false;
                        }
                    }
                }
            } else {
                result = false;
            }
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        }
        if (!result) {
            log("Can't stop the HUB!!");
        }
        return result;
    }

    protected StringBuilder toString(ByteBuffer bb) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bb.position(); i++) {
            sb.append(((int) bb.get(i)) & 0xff);
            sb.append(", ");
        }
        return sb;
    }

    protected abstract boolean runTest() throws Throwable;
}
