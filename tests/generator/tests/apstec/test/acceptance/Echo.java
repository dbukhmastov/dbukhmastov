package apstec.test.acceptance;

import java.io.IOException;
import java.nio.ByteBuffer;

import apstec.test.BaseTest;

/**
 * @test
 * @id Echo
 * @executeClass apstec.test.acceptance.Echo
 * @source Echo.java
 * @title Echo acceptance Test
 */
public class Echo extends BaseTest {
    private byte[] echo1 = new byte[] {31, 0, 6, 0, 0, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255};// lenth = 0X0006
    private byte[] echo2 = new byte[] {31, 0, 6, 0, 0, 0, 0, 0, 0, 0};// lenth = 0X0006

    public static void main(String[] args) {
        main(new Echo(), args);
    }

    protected boolean runTest() throws IOException {
        boolean result = true;
        for (int i = 0; i < boards.length; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length; j++) {
                byte sudAddr = type[j];
                if (sudAddr <= 0) {
                	log("Wrong sudAddr=", sudAddr, ", i=", i, ", j=", j);
                	result = false;
                } else {
                	result &= echoCommand(echo1, sudAddr, ids[i][j], i + 1);
                    result &= echoCommand(echo2, sudAddr, ids[i][j], i + 1);
                }
                
            }
        }
        return result;
    }

    public boolean echoCommand(byte[] echo, byte sudAddr, byte id, int type) throws IOException {
        echo[1] = sudAddr;
        if (!cleanupCommandChannel()) {
        	error("Can't cleanup command channel.");
            return false;
        }
        send(echo, 0, echo.length);
        ByteBuffer buf = receive(10);
        if (buf == null) {
            error("Can't receive response sudAddr=", sudAddr, " echoCommand");
            return false;
        } else {
            if (id != buf.get(0)) {
                error("The 'id' is wrong sudAddr=", sudAddr, " buf ", toString(buf));
                return false;
            }
            if (buf.get(1) != type) {
                error("The type is wrong sudAddr=", sudAddr, " buf ", toString(buf));
                return false;
            }
            // tail more then 6 element (data length is 6) should be 0
            if (buf.get(6) + buf.get(7) + buf.get(8) + buf.get(9) != 0) {
                error("Receive data is wrong sudAddr=", sudAddr, " buf ", toString(buf));
                return false;
            }
            return true;
        }
    }
}
