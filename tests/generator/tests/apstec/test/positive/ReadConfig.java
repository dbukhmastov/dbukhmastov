package apstec.test.positive;

import java.io.IOException;
import java.util.Arrays;

import apstec.test.BaseTest;

/**
 * @test
 * @id ReadConfig
 * @executeClass apstec.test.positive.ReadConfig
 * @source ReadConfig.java
 * @title Test ReadConfig command
 */
public class ReadConfig extends BaseTest {
    private byte[] readConfig = new byte[] {2, 0, 3, 0, (byte) 255, (byte) 255, (byte) 255};// lenth
                                                                                            // =
                                                                                            // 0X0006

    public static void main(String[] args) {
        main(new ReadConfig(), args);
    }

    protected boolean runTest() throws IOException {
        boolean result = true;
        for (int i = 0; i < boards.length; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length; j++) {
                byte sudAddr = type[j];
                // result &= readConfig(sudAddr, ids[i][j]);
                // result &= sendData(sudAddr);
                // result &= readLessThen(sudAddr, ids[i][j], i+1);
            }
        }
        return result;
    }
    /*
     * public boolean readConfig() throws IOException { send(readConfig); byte[]
     * buf = receive(32); byte[] orig = initalConfig(); if (!Arrays.equals(buf,
     * orig)) { log("Config is wrong:", toString(buf)); log("Should be:",
     * toString(orig)); return false; } return true; }
     * 
     * public boolean sendData() throws IOException { byte[] readConfig =
     * getCmd(CMD_READ_CONFIG, new byte[]{(byte)255, (byte)255, (byte)255},
     * false); send(readConfig); byte[] buf = receive(32); byte[] orig =
     * initalConfig(); if (!Arrays.equals(buf, orig)) { log("Config is wrong:",
     * toString(buf)); log("Should be:", toString(orig)); return false; } return
     * true; }
     * 
     * public boolean readLessThen() throws IOException { byte[] readConfig =
     * getCmd(CMD_READ_CONFIG, null, true); send(readConfig); byte[] buf =
     * receive(10); byte[] orig = initalConfig(); byte[] tmp =
     * Arrays.copyOf(orig, buf.length); if (!Arrays.equals(buf, tmp)) {
     * log("Part of config is wrong:", toString(buf)); log("Should be:",
     * toString(tmp)); return false; } //byte[] tail = receive(22); return
     * readConfig(); }
     */
}
