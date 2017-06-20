package apstec.test.acceptance;

import java.io.IOException;
import java.nio.ByteBuffer;

import apstec.test.BaseTest;

/**
 * @test
 * @id ReadConfig
 * @executeClass apstec.test.acceptance.ReadConfig
 * @source ReadConfig.java
 * @title Test ReadConfig command
 */
public class ReadConfig extends BaseTest {
    private byte[] readConfig = new byte[] {2, 0, 0, 0};

    public static void main(String[] args) {
        main(new ReadConfig(), args);
    }

    protected boolean runTest() throws IOException {
        boolean result = true;
        for (int i = 0; i < boards.length; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length; j++) {
            	if (type[j] <= 0) {
                	log("Wrong sudAddr=", type[j], ", i=", i, ", j=", j);
                	result = false;
                } else {
                	readConfig[1] = type[j];
                    if (!cleanupCommandChannel()) {
                    	error("Can't cleanup command channel.");
                        return false;
                    }
                    send(readConfig, 0, 4);
                    ByteBuffer buf = receive(32);
                    if (buf == null) {
                        error("Can't get response sudAddr=", type[j]);
                        result = false;
                    } else if (ids[i][j] != buf.get(0)) {
                        error("The 'id' is wrong ", ids[i][j], " buf ", toString(buf));
                        result = false;
                    }
                }
            }
        }
        return result;
    }
}
