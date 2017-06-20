package apstec.test.negative;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import apstec.test.BaseTest;

/**
 * @test
 * @id WrongEcho
 * @executeClass apstec.test.negative.WrongEcho
 * @source WrongEcho.java
 * @title Test Echo command
 */
public class WrongEcho extends BaseTest {
    private byte[] echo = new byte[] {(byte) 31, (byte) 0, (byte) 6, (byte) 0, (byte) 0, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255};// lenth = 0X0006

    public static void main(String[] args) {
        main(new WrongEcho(), args);
    }

    protected boolean runTest() throws IOException {
        boolean result = true;
        for (int i = 0; i < boards.length; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length; j++) {
                byte sudAddr = type[j];
                if (!wrongLength(sudAddr, ids[i][j])) {
                    result = false;
                    log("case wrongLength failed.");
                }
                if (!wrongEcho1(sudAddr, ids[i][j], i + 1)) {
                    result = false;
                    log("case wrongEcho1 failed.");
                }
                if (!wrongEcho2(sudAddr, ids[i][j], i + 1)) {
                    result = false;
                    log("case wrongEcho2 failed.");
                }
                if (!wrongEcho3(sudAddr, ids[i][j], i + 1)) {
                    result = false;
                    log("case wrongEcho3 failed.");
                }
            }
        }
        return result;
    }

    public boolean wrongLength(byte sudAddr, byte id) throws IOException {
        byte[] echo = new byte[] {(byte) 31, sudAddr, (byte) 0, (byte) 0, (byte) 255};
        send(echo, 0, 5);
        ByteBuffer buf = receive(5);
        if (buf != null) {
            error("Unexpected response ", toString(buf));
            return false;
        }
        return correct(sudAddr, id);
    }

    public boolean wrongEcho1(byte sudAddr, byte id, int type) throws IOException {
        try {
            if (wrongEcho(1, sudAddr)) {
                echoCommand(sudAddr, id, type);// echo should work after wrong request
            }
        } catch (SocketTimeoutException e) {
            return true;
        }
        error("SocketTimeoutException should be thrown.");
        return false;
    }

    public boolean wrongEcho2(byte sudAddr, byte id, int type) throws IOException {
        if (wrongEcho(2, sudAddr)) {
            return echoCommand(sudAddr, id, type);// echo should work after wrong request
        }
        return false;
    }

    public boolean wrongEcho3(byte sudAddr, byte id, int type) throws IOException {
        if (wrongEcho(3, sudAddr)) {
            return echoCommand(sudAddr, id, type);// echo should work after wrong request
        }
        return false;
    }

    private boolean wrongEcho(int i, byte sudAddr) throws IOException {
        byte[] echo = new byte[] {(byte) 31, (byte) sudAddr, (byte) 0, (byte) 0};
        byte[] cmd = new byte[echo.length - i];
        System.arraycopy(echo, 0, cmd, 0, cmd.length);
        send(cmd, 0, cmd.length);
        ByteBuffer buf = receive(cmd.length);
        if (buf == null) {
            return true;
        } else {
            error("Unexpected response ", " not null ", toString(buf));
            return false;
        }
    }

    private boolean echoCommand(byte sudAddr, byte id, int type) throws IOException {
        echo[1] = sudAddr;
        send(echo, 0, 10);
        ByteBuffer buf = receive(10);
        if ((id - (buf.get(0)) & 0xFF) != 0) {
            error("The 'id' is wrong", buf.toString());
            return false;
        }
        if (buf.get(1) != type) {
            error("The type is wrong ", sudAddr, " buf ", toString(buf));
            return false;
        }
        // tail more then 6 element (data length is 6) should be 0
        if (buf.get(6) + buf.get(7) + buf.get(8) + buf.get(9) != 0) {
            error("Receive data is wrong", buf.toString());
            return false;
        }
        return true;
    }

    private boolean correct(byte sudAddr, byte id) throws IOException {
        byte[] echo = new byte[] {(byte) 31, sudAddr, (byte) 1, (byte) 0, (byte) 255};
        send(echo, 0, 5);
        ByteBuffer buf = receive(5);
        if (buf == null) {
            error("Unexpected response ", "null");
            return false;
        } else if (id != buf.get(0)) {
            error("The 'id' is wrong", toString(buf));
            return false;
        }
        return true;
    }
}
