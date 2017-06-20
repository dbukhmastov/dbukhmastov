package apstec.test.positive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import apstec.test.BaseTest;

/**
 * @test
 * @id Echo
 * @executeClass apstec.test.positive.Echo
 * @source Echo.java
 * @title Test Echo command
 */
public class Echo extends BaseTest {
    private byte[] echo1 = new byte[] {(byte) 31, (byte) 0, (byte) 6, (byte) 0, (byte) 0, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255};// lenth = 0X0006
    private byte[] echo2 = new byte[] {(byte) 31, (byte) 0, (byte) 6, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0};// lenth = 0X0006

    public static void main(String[] args) {
        main(new Echo(), args);
    }

    protected boolean runTest() throws IOException {
        boolean result = true;
        for (int i = 0; i < boards.length && result; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length && result; j++) {
                byte sudAddr = type[j];
                if (!echoCommand(sudAddr, ids[i][j], i + 1)) {
                    result = false;
                    log("case echoCommand failed.");
                } else {
                    log("case echoCommand passed.");
                }
                // TODO
                /*
                 * if (!zeroEcho(sudAddr)) { result = false;
                 * log("case zeroEcho failed."); } else {
                 * log("case zeroEcho passed."); }
                 */
                if (!oneEcho(sudAddr, ids[i][j])) {
                    result = false;
                    log("case oneEcho failed.");
                } else {
                    log("case oneEcho passed.");
                }
                if (!sendThenRead(sudAddr, ids[i][j], i + 1)) {
                    result = false;
                    log("case sendThenRead failed.");
                } else {
                    log("case sendThenRead passed.");
                }
                if (!receiveWOsend(sudAddr)) {
                    result = false;
                    log("case receiveWOsend failed.");
                } else {
                    log("case receiveWOsend passed.");
                }
                if (!readLessThen(sudAddr, ids[i][j], i + 1)) {
                    result = false;
                    log("case readLessThen failed.");
                }
                // TODO
                /*
                 * if (!sendMore(sudAddr)) { result = false;
                 * log("case sendMore failed."); }
                 */
            }
        }
        return result;
    }

    public boolean echoCommand(byte sudAddr, byte id, int type) throws IOException {
        echo1[1] = sudAddr;
        send(echo1, 0, 10);
        ByteBuffer buf = receive(10);
        if (buf == null) {
            error("Can't receive response ", sudAddr, " echoCommand");
            return false;
        } else {
            if (id != buf.get(0)) {
                error("The 'id' is wrong ", sudAddr, " buf ", toString(buf));
                return false;
            }
            if (buf.get(1) != type) {
                error("The type is wrong ", sudAddr, " buf ", toString(buf));
                return false;
            }
            // tail more then 6 element (data length is 6) should be 0
            if (buf.get(6) + buf.get(7) + buf.get(8) + buf.get(9) != 0) {
                error("Receive data is wrong ", sudAddr, " buf ", toString(buf));
                return false;
            }
            return true;
        }
    }

    public boolean zeroEcho(byte sudAddr) throws IOException {
        byte[] echo = new byte[] {(byte) 31, sudAddr, (byte) 0, (byte) 0};
        send(echo, 0, 4);
        ByteBuffer buf = receive(4);
        if (buf == null) {
            error("Can't receive response ", sudAddr, " zeroEcho");
            return false;
        } else {
            if (buf.get(0) != 0) {
                error("The 'id' is wrong ", sudAddr, " buf ", toString(buf));
                return false;
            }
            if (buf.get(1) != 0) {
                error("The type is wrong ", sudAddr, " buf ", toString(buf));
                return false;
            }
            if (buf.get(2) + buf.get(3) + buf.get(4) + buf.get(5) + buf.get(6) + buf.get(7) + buf.get(8)
                    + buf.get(9) != 0) {
                error("Receive data is wrong ", sudAddr, " buf ", toString(buf));
                return false;
            }
            return true;
        }
    }

    public boolean oneEcho(byte sudAddr, byte id) throws IOException {
        byte[] echo = new byte[] {(byte) 31, sudAddr, (byte) 1, (byte) 0, (byte) 255};
        send(echo, 0, 5);
        ByteBuffer buf = receive(5);
        if (id != buf.get(0)) {
            error("The 'id' is wrong", toString(buf));
            return false;
        }
        if (buf.get(1) != 0) {
            error("The type is wrong", toString(buf));
            return false;
        }
        return true;
    }

    public boolean sendThenRead(byte sudAddr, byte id, int type) throws IOException {
        for (int i = 3; i < 255; i++) {
            byte[] buf1 = new byte[i + 4];
            Arrays.fill(buf1, (byte) i);
            buf1[0] = (byte) 31;
            buf1[1] = (byte) sudAddr;
            buf1[2] = (byte) (i & 0xFF); // little endian
            buf1[3] = (byte) ((i >> 8) & 0xFF); // little endian
            send(buf1, 0, buf1.length);
            ByteBuffer buf2 = receive(i + 4);
            if (buf2 == null) {
                error("Can't receive response ", sudAddr, " sendThenRead");
                return false;
            } else {
                if (id != buf2.get(0)) {
                    log("send ", toString(buf1));
                    log("recieved ", toString(buf2));
                    error("The 'id' is wrong", toString(buf2), ", case is", String.valueOf(i));
                    return false;
                }
                if (buf2.get(1) != type) {
                    log("send ", toString(buf1));
                    log("recieved ", toString(buf2));
                    error("The type is wrong", toString(buf2), ", case is", String.valueOf(i));
                    return false;
                }
                // TODO return is 1,2, 3
                for (int j = 2; j < buf2.position(); j++) {
                    if (j != (((int) buf2.get(j)) & 0xFF)) {
                        log("send ", toString(buf1));
                        log("recieved ", toString(buf2));
                        error("Receive data is wrong ", String.valueOf(buf2.get(j)), ", should be", String.valueOf(i));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // TODO generic case
    public boolean receiveWOsend(byte sudAddr) throws IOException {
        try {
            ByteBuffer buf = receive(6);
            if (buf == null) {
                return true;
            }
            error("Data:", toString(buf));
        } catch (Throwable e) {
            e.printStackTrace();
            error("Unexpected ", e.getMessage());
        }
        return false;
    }

    public boolean readLessThen(byte sudAddr, byte id, int type) throws IOException {
        echo1[1] = sudAddr;
        send(echo1, 0, 10);
        ByteBuffer buf = receivePart(5);
        if ((id - (buf.get(0)) & 0xFF) != 0) {
            error("The 'id' is wrong", toString(buf));
            return false;
        }
        if (buf.get(1) != type) {
            error("The type is wrong", toString(buf));
            return false;
        }
        // Cleanup
        receivePart(4);
        /*
         * buf = receivePart(4); if (buf.get(0) != (byte)255) {
         * error("Receive tail is wrong", toString(buf)); return false; }
         */
        return echoCommand(sudAddr, id, type);
    }

    // TODO the echo response is return is sudAddr,ID,3,4,5,....
    /*
     * public boolean sendMore(byte sudAddr) throws IOException { echo2[1] =
     * sudAddr; send(echo2, 0, 10); ByteBuffer buf = receive(10); if (buf ==
     * null) { error("Can't receive response ", sudAddr, " sendMore"); return
     * false; } else { if (buf.get(0) + buf.get(1) + buf.get(2) + buf.get(3) +
     * buf.get(4) + buf.get(5) + buf.get(6) + buf.get(7) + buf.get(8) +
     * buf.get(9) != 0) { error("Receive data is wrong", toString(buf)); return
     * false; } return true; } }
     */

    protected StringBuilder toString(byte[] bb) {
        StringBuilder sb = new StringBuilder();
        for (byte next : bb) {
            sb.append(((int) next) & 0xff);
            sb.append(", ");
        }
        return sb;
    }
}
