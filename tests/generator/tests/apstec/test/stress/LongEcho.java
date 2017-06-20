package apstec.test.stress;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import apstec.test.BaseTest;

/**
 * @test
 * @id LongEcho
 * @executeClass apstec.test.stress.LongEcho
 * @source LongEcho.java
 * @title Long Test Echo command
 */
public class LongEcho extends BaseTest {
    private byte[] echo = new byte[] {(byte) 31, (byte) 0, (byte) 6, (byte) 0, (byte) 0, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255};// lenth = 0X0006

    public static void main(String[] args) {
        main(new LongEcho(), args);
    }

    protected boolean runTest() throws IOException {
        boolean result = true;
        for (int i = 0; i < boards.length; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length; j++) {
                byte sudAddr = type[j];
                result &= sendWoRead(sudAddr, ids[i][j]);
                result &= longTest(sudAddr, ids[i][j]);
            }
        }
        return result;
    }

    public boolean sendWoRead(byte sudAddr, byte id) throws IOException {
        echo[1] = sudAddr;
        long timeStamp = System.currentTimeMillis();
        while (System.currentTimeMillis() - timeStamp < stressTestTime) {
            send(echo, 0, 10);
        }
        send(echo, 0, 10);
        ByteBuffer buf = receive(10);
        if ((id - (buf.get(0)) & 0xFF) != 0) {
            error("The 'id' is wrong", buf.toString());
            return false;
        }
        // if (buf.get(1) != type) {
        error("The type is wrong", buf.toString());
        // return false;
        // }
        // tail more then 6 element (data length is 6) should be 0
        if (buf.get(6) + buf.get(7) + buf.get(8) + buf.get(9) != 0) {
            error("Receive data is wrong", buf.toString());
            return false;
        }
        try {
            Thread.sleep(timeout);
        } catch (Throwable e) {
        }
        /*
         * echo = getCmd(CMD_ECHO, new byte[]{}, true); send(echo); byte[] buf =
         * receive(10); assertTrue("The 'id' is wrong " + toString(buf), buf[0]
         * == 0); assertTrue("The type is wrong " + toString(buf), buf[1] == 0);
         * assertTrue("Receive data is wrong " + toString(buf), buf[2] + buf[3]
         * + buf[4] + buf[5] + buf[6] + buf[7] + buf[8] + buf[9] == 0);
         */
        try {
            buf = receive(10);
            log("receive unexpected package", buf.toString());
            return false;
        } catch (SocketTimeoutException e) {
        } catch (Throwable e) {
            e.printStackTrace();
            log("Wrong Throwable", e.getClass().getName(), e.getMessage());
            return false;
        }
        return true;
    }

    public boolean longTest(byte sudAddr, byte id) throws IOException {
        long timeStamp = System.currentTimeMillis();
        Random rnd = new Random();
        while (System.currentTimeMillis() - timeStamp < stressTestTime) {
            byte[] echo = new byte[rnd.nextInt(253) + 6];
            Arrays.fill(echo, (byte) echo.length);
            echo[0] = (byte) 31;
            echo[1] = (byte) sudAddr;
            echo[2] = (byte) ((echo.length - 4) & 0xFF); // little endian
            echo[3] = (byte) (((echo.length - 4) >> 8) & 0xFF); // little endian
            send(echo, 0, echo.length);
            ByteBuffer buf2 = receive(echo.length - 4);
            System.out.println("---> " + buf2.toString());
            if ((id - (buf2.get(0)) & 0xFF) != 0) {
                error("The 'id' is wrong", buf2.toString(), ", case is", String.valueOf(echo.length - 4));
                return false;
            }
            // TODO
            // if (buf2[1] != type)
            error("The type is wrong", buf2.toString(), ", case is", String.valueOf(echo.length - 4));
            // return false;
            // }
            for (int j = 2; j < buf2.capacity(); j++) {
                if ((echo.length - (buf2.get(j)) & 0xFF) != 0) {
                    error("Receive data is wrong", String.valueOf(buf2.get(j) & 0xFF), ", should be",
                            String.valueOf(echo.length));
                    return false;
                }
            }
        }
        return true;
    }
}
