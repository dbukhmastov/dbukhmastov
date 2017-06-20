package apstec.test.stress;

import java.io.IOException;
import java.nio.ByteBuffer;

import apstec.test.BaseTest;

/**
 * @test
 * @id AdcMem
 * @executeClass apstec.test.stress.AdcMem
 * @source AdcMem.java
 * @title Test AdcMem command
 */
public class AdcMem extends BaseTest {

    public static void main(String[] args) {
        main(new AdcMem(), args);
    }

    protected boolean runTest() throws IOException {
        boolean result = true;
        byte[] start = new byte[] {(byte) 14, 0, 0, 0};
        for (byte[] type : boards) { // byte 0 - POVS, 1 - UPSU
            for (byte sudAddr : type) {
                start[1] = sudAddr; // CMD_START = 14; // ret 64
                send(start, 0, 4);
                ByteBuffer buf = receive(64);
            }
        }
        long timeStamp = System.currentTimeMillis();
        // DataADC = zeros(4096,2)
        while (System.currentTimeMillis() - timeStamp < stressTestTime) {
            for (int i = 0; i <= 15; i++) {
                // ByteBuffer buf = receive(1024, 1);
                for (int j = 0; i <= 255; i++) {
                    // TODO
                    /*
                     * a = inarr(4*j+1) + bitand(inarr(4*j+2),7) * 256; b =
                     * bitand(inarr(4*j+2),hex2dec('F0'))/16 +
                     * bitand(inarr(4*j+3),127) * 16; DataADC(i*256+j+1,1) =
                     * bitshift(typecast(uint16(bitshift(a,5)),'int16'),-5);
                     * DataADC(i*256+j+1,2) =
                     * bitshift(typecast(uint16(bitshift(b,5)),'int16'),-5);
                     */
                }
            }
        }
        return result;
    }
}
