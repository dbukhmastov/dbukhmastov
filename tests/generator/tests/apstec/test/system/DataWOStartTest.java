package apstec.test.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Properties;

import apstec.test.DataChannelTest;

/**
 * @test
 * @id DataWOStartTest
 * @executeClass apstec.test.system.DataWOStartTest
 * @source DataWOStartTest.java
 * @title Test data channel should not received data without start generation.
 */
public class DataWOStartTest extends DataChannelTest implements Runnable {
    private static final long TESTING_TIME = 60000;
    // frequency_list
    // 0 - fcode (N_Counter)
    // 1 - fcDAC (DAC_Value)
    // 2 - fcATT (ATT_Value)
    // 3 - fgain (m_freqGain)
    private int flist[][] = new int[40][];
    private boolean result = true;
    private boolean bTreadStop = false;

    public static void main(String[] args) {
        main(new DataWOStartTest(), args);
    }

    protected boolean init(Properties props, Properties hwProps) throws IOException {
        // parse frequency_list
        try (StringReader sr = new StringReader(hwProps.getProperty("frequency_list"));
                BufferedReader br = new BufferedReader(sr)) {
            String tmp = null;
            int iRow = 0, rowLength = -1;
            while ((tmp = br.readLine()) != null) {
                if ((tmp = tmp.trim()).length() > 4) {
                    String[] strTmp = tmp.split("\\p{Space}+");
                    if (rowLength < 0) {
                        rowLength = strTmp.length;
                    }
                    int[] nextRow = new int[4];
                    for (int i = 0; i < 4; i++) {
                        // FLIST_fcode = 1;
                        // FLIST_fcDAC = 2;
                        // FLIST_fcATT = 3;
                        // FLIST_fgain = 4;
                        nextRow[i] = Integer.parseInt(strTmp[i + 1]);
                    }
                    if (iRow >= flist.length) {
                        int fTmp[][] = flist;
                        flist = new int[2 * fTmp.length][];
                        System.arraycopy(fTmp, 0, flist, 0, fTmp.length);
                    }
                    flist[iRow++] = nextRow;
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        // END parse frequency_list
        if (stopHub()) {
        	if (super.init(props, hwProps)) {
        		return cleanupChannel(getDataChannel());
        	}
        }
        return false;
    }

    protected boolean runTest() throws IOException {
        // -- DeviceHSRv1::setInitialValuesGen()
        // DAC gain
        log("set DAC gain");
        byte[] cmd = new byte[] {5, boards[2][0], 3, 0, 0x0C, 0x02, 0x40}; // 2 - GEN
        ByteBuffer resp = sendReceiveCmd(cmd, 0, 7, 2);
        if (resp.get(0) != 5 && resp.get(1) != ids[2][0]) {
            error("1 Wrong CMD_SET_DAC(5) response: ", resp.get(0), ", ", resp.get(1), " should be ", ids[2][0]);
            return false;
        }
        // DAC min ajust
        log("set DAC min ajust");
        cmd[4] = 0x0E;
        cmd[5] = (byte) 0x81;
        cmd[6] = 0x00;
        resp = sendReceiveCmd(cmd, 0, 7, 2);
        if (resp.get(0) != 5 && resp.get(1) != ids[2][0]) {
            error("2 Wrong CMD_SET_DAC(5) response: ", resp.get(0), ", ", resp.get(1), " should be ", ids[2][0]);
            return false;
        }
        // Input LO path attenuator; 0 - min; 3ff - max
        log("Input LO path");
        cmd[4] = 0x12;
        cmd[5] = (byte) 0x82;
        cmd[6] = 0x00;
        resp = sendReceiveCmd(cmd, 0, 7, 2);
        if (resp.get(0) != 5 && resp.get(1) != ids[2][0]) {
            error("3 Wrong CMD_SET_DAC(5) response: ", resp.get(0), ", ", resp.get(1), " should be ", ids[2][0]);
            return false;
        }
        // before CMD_SET_DAC
        log("before CMD_SET_DAC");
        cmd = new byte[] {4, boards[2][0], 2, 0, 0x14, 0x05}; // 2 - GEN
        resp = sendReceiveCmd(cmd, 0, 6, 2);
        if (resp.get(0) != 4 && resp.get(1) != ids[2][0]) {
            error("Wrong CMD_SET_ADC(4) response: ", resp.get(0), ", ", resp.get(1), " should be ", ids[2][0]);
            return false;
        }
        // sin_en sqwv_en dsm_rstb
        log("set sin_en sqwv_en dsm_rstb");
        cmd = new byte[] {6, boards[2][0], 4, 0, 0x01, 0x00, (byte) 0xDE, (byte) 0x9B}; // 2 - GEN
        resp = sendReceiveCmd(cmd, 0, 8, 3);
        log("UNDEFINED CMD_SET_PLL(6) response ", resp.get(0), ", ", resp.get(1), ", ", resp.get(2));
        // sin sel, R divider
        cmd = new byte[] {6, boards[2][0], 4, 0, 0x03, 0x00, (byte) 0x80, 0x08}; // 2 - GEN
        resp = sendReceiveCmd(cmd, 0, 8, 3);
        // N divider
        cmd = new byte[] {6, boards[2][0], 4, 0, 0x0F, 0x00, 0x00, (byte) 0x64}; // 2 - GEN
        resp = sendReceiveCmd(cmd, 0, 8, 3);
        //
        cmd = new byte[] {6, boards[2][0], 4, 0, 0x12, 0x03, 0x0E, (byte) 0x8A}; // 2 - GEN
        resp = sendReceiveCmd(cmd, 0, 8, 3);
        // END -- DeviceHSRv1::setInitialValuesGen()

        // -- DeviceHSRv1::setAD9230POVS(...)
        log("set ADC 1v pk-pk"); // ADC 1v pk-pk
        cmd = new byte[] {6, 0, 2, 0, 0x11, 0x18};
        for (byte subAddr : boards[1]) { // 0 - UPSU, 1 - POVS, 2 - GEN
            cmd[1] = subAddr;
            resp = sendReceiveCmd(cmd, 0, 6, 2);
            if (resp.get(0) != 6 && resp.get(1) != ids[2][0]) {
                error("Wrong CMD_SET_PLL(6) response for ", cmd[1], ": ", resp.get(0), ", ", resp.get(1), " should be ",
                        ids[2][0]);
                return false;
            }
        }
        // END -- DeviceHSRv1::setAD9230POVS(...)

        // -- DeviceHSRv1::setFreqPOVS
        log("set FreqPOVS");
        cmd = new byte[4 + flist.length];
        int dLen = flist.length;
        cmd[0] = 5;
        cmd[3] = (byte) (dLen & 0xFF);
        cmd[2] = (byte) (dLen >> 8 & 0xFF);
        for (int i = 0; i < boards[1].length; i++) { // 0 - UPSU, 1 - POVS, 2 -
                                                         // GEN
            byte subAddr = boards[1][i];
            cmd[1] = subAddr;
            for (int j = 0; j < flist.length; j++) {
                cmd[4 + j] = (byte) ((flist[j][3] > 11 ? 11 : flist[j][3]) & 0x0f);
            }
            resp = sendReceiveCmd(cmd, 0, cmd.length, 2);
            if (resp.get(0) != cmd[1] && resp.get(1) != ids[1][i]) {
                error("Wrong setFreqPOVS(5) response for ", cmd[1], ": ", resp.get(0), ", ", resp.get(1), " should be ",
                        ids[1][i]);
                return false;
            }
        }
        // END -- DeviceHSRv1::setFreqPOVS

        // -- DeviceHSRv1::setMeasAll()
        log("set Meas");
        cmd = new byte[23];
        cmd[0] = 7; // CMD_SET_MEAS(7)
        cmd[1] = 0; //
        cmd[2] = 19; //
        cmd[3] = 0; //
        cmd[4] = (byte) 0x80; // MEAS_PERIOD = 10000000; == 0x989680
        cmd[5] = (byte) 0x96; //
        cmd[6] = (byte) 0x98; //
        cmd[7] = 0x00; //
        cmd[8] = (byte) 0x88; // MEAS_SIDE_WAIT_CNT = 5000; == 0x1388
        cmd[9] = 0x13; //
        cmd[10] = (byte) 0xF4; // MEAS_FREQ_WAIT_CNT = 500; == 0x01F4
        cmd[11] = 0x01; //
        cmd[12] = 0x22; // MEAS_LINE_WAIT_CNT = 34; == 0x0022
        cmd[13] = 0x00; //
        cmd[14] = 0x0C; // MEAS_ELEM_WAIT_CNT = 12; == 0x000C
        cmd[15] = 0x00; //
        cmd[16] = 0x40; // MEAS_SIGNL_INT_CNT = 64; == 0x0040
        cmd[17] = 0x00; //
        cmd[18] = 0x10; // MEAS_ELEM_NUM = 16; == 0x10
        cmd[19] = 0x10; // MEAS_LINE_NUM = 16; == 0x10
        cmd[20] = (byte) 0x50; // MEAS_FREQ_NUM = 80; == 0x50
        cmd[21] = 0x02; // MEAS_SIDE_NUM = 2; == 0x02
        cmd[22] = 0x02; // MEAS_MOD_NUM = 2; == 0x02
        for (int i = 0; i < boards.length; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length; j++) {
                cmd[1] = type[j];
                resp = sendReceiveCmd(cmd, 0, 23, 2);//
                if (resp.get(0) != 7 && resp.get(1) != ids[i][j]) {
                    error("Wrong CMD_SET_MEAS(7) response for ", cmd[1], ": ", resp.get(0), ", ", resp.get(1),
                            " should be ", ids[i][j]);
                    return false;
                }
            }
        }
        // END -- DeviceHSRv1::setMeasAll()

        // -- DeviceHSRv1::setStepModeGen
        log("set StepModeGen");
        cmd = new byte[1028]; // 4 + 1024
        cmd[0] = 16;
        cmd[1] = boards[2][0];
        cmd[2] = 0;
        cmd[3] = 4;
        for (int i = 0; i < flist.length; i++) {
            // 0 - fcode (N_Counter)
            // 1 - fcDAC (DAC_Value)
            // 2 - fcATT (ATT_Value)
            long p = flist[i][1] & 0xffff; // 4 bytes
            p |= (flist[i][2] & 0x1f) << 16;
            p |= (flist[i][0] & 0x7ff) << 21;
            int index = 4 + i * 4;
            cmd[index + 3] = (byte) (p & 0xFF);
            cmd[index + 2] = (byte) ((p >> 8) & 0xFF);
            cmd[index + 1] = (byte) ((p >> 8) & 0xFF);
            cmd[index] = (byte) ((p >> 8) & 0xFF);
        }
        resp = sendReceiveCmd(cmd, 0, 1028, 2);
        if (resp.get(0) != 16 && resp.get(1) != boards[2][0]) {
            error("Wrong CMD_SET_STEP_MODE(16) response: ", resp.get(0), ", ", resp.get(1), " should be ",
                    boards[2][0]);
            return false;
        }
        // END -- DeviceHSRv1::setStepModeGen

        // -- DeviceHSRv1::setDataLenHub
        log("setDataLenHub");
        if (!setDataLenHub(0x304)) {
            error("Can't setDataLenHub");
            return false;
        }
        // END -- DeviceHSRv1::setDataLenHub

        // start collectData
        new Thread(this).start();

        // -- DeviceHSRv1::startHub()
        /*log("startHub");
        
        cmd = new byte[] {14, 41, 0, 0};
        resp = sendReceiveCmd(cmd, 0, 4, 64);
        if (resp != null && resp.position() > 0) {
            for (int i = 0; i < boards.length; i++) {
                byte[] type = boards[i];
                for (int j = 0; j < type.length; j++) {
                    if (resp.get(type[j]) == 0) {
                        log("Can't start!! type=", i, ", index=", j, ", subaddr=", type[j]);
                        return false;
                    }
                }
            }
        } else {
            error("Can't start the HUB.");
            return false;
        }*/
        // END -- DeviceHSRv1::startHub()

        // Start testing process
        long timeStamp = System.currentTimeMillis();
        while (result && System.currentTimeMillis() - timeStamp <= TESTING_TIME) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        bTreadStop = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void cleanup() {
    	stopHub();
        super.cleanup();
    }

    // collectData Thread
    public void run() {
        DatagramChannel dataChannel = getDataChannel();
        cleanupChannel(dataChannel);
        try {
            while (result && !bTreadStop) {
            	ByteBuffer tmp = ByteBuffer.allocateDirect(64);
                if (dataChannel.read(tmp) > 0) {
                	log("Received data: \"" + toString(tmp)+"\"");
                	result = false;
                }
                try {
                    Thread.sleep(1);
                } catch (Throwable e) {
                }
            }
        } catch (Throwable th) {
            error("Unexpected Throwable: " + th.getClass().getName() + ":" + th.getMessage());
            th.printStackTrace();
        }
    }
}
