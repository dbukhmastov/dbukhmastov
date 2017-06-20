package apstec.server.test.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.Properties;

import apstec.server.test.BaseTest;

public abstract class GeneratorBaseTest extends BaseTest implements Runnable {
    private static final String HARDWARE_CONFIG = "/apstec/hw_config";
    protected DatagramChannel cmd_dc, data_dc;
    protected SocketAddress serverCmdAddr, serverDataAddr;
    
    protected byte[][] boards = new byte[3][]; // 0 - UPSU, 1 - POVS, 2 - GEN
    protected byte[][] ids = new byte[3][]; // 0 - UPSU, 1 - POVS, 2 - GEN

    protected void cleanup() {
    	super.cleanup();
        log("cleanup cmd");
        closeChannel(cmd_dc);
        cmd_dc = null;
        log("cleanup data");
        closeChannel(data_dc);
        data_dc = null;
    }

    protected void closeChannel(DatagramChannel dc) {
        cleanupChannel(dc);
        try {
        	if (dc.isConnected()) {
        		dc.disconnect();
        	}
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
    	if (dc.isConnected()) {
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
    	}
        return true;
    }

    protected boolean init(Properties props) throws IOException {
        Properties hwProps = new Properties();
        try (FileInputStream fis = new FileInputStream(new File(props.getProperty("hw_config")))) {
        	hwProps.load(fis);
    	} catch (Throwable e) {
    		try (InputStream is = props.getClass().getResourceAsStream(HARDWARE_CONFIG)) {
    			hwProps.load(is);
    		} catch (Throwable ee) {
    			ee.printStackTrace();
    		}
		}
        log("-----------------------");
        String tmp = "";
        bundleNumber = Integer.parseInt((tmp = props.getProperty(BUNDLE_NUMBER)) == null ? "-1" : tmp.trim());
        timeout = Integer.parseInt((tmp = props.getProperty(TIMEOUT)) == null ? "120000" : tmp.trim());
        stressTestTime = Integer.parseInt((tmp = props.getProperty(STRESS_TEST_TIME)) == null ? "120000" : tmp.trim());
        socketTimeout = Integer.parseInt((tmp = props.getProperty(SOCKET_TIMEOUT)) == null ? "60000" : tmp.trim());
        
        cmd_dc = DatagramChannel.open();
        cmd_dc.setOption(StandardSocketOptions.SO_RCVBUF,
                Integer.parseInt((tmp = props.getProperty(RECEIVE_BUFFER)) == null ? "65536" : tmp.trim()))
                .setOption(StandardSocketOptions.SO_SNDBUF,
                        Integer.parseInt((tmp = props.getProperty(SEND_BUFFER)) == null ? "2048" : tmp.trim()));
        cmd_dc.configureBlocking(false);
        cmd_dc.bind(new InetSocketAddress(Integer.parseInt((tmp = props.getProperty(CMD_PORT)) == null ? "28929" : tmp.trim())));
        log("cmd_dc :"+cmd_dc.getLocalAddress());
        
        data_dc = DatagramChannel.open();
        data_dc.setOption(StandardSocketOptions.SO_RCVBUF,
                Integer.parseInt((tmp = props.getProperty(RECEIVE_BUFFER)) == null ? "65536" : tmp.trim()))
                .setOption(StandardSocketOptions.SO_SNDBUF,
                        Integer.parseInt((tmp = props.getProperty(SEND_BUFFER)) == null ? "2048" : tmp.trim()))
                .setOption(StandardSocketOptions.SO_BROADCAST, true);
        data_dc.configureBlocking(false);
        data_dc.bind(new InetSocketAddress(Integer.parseInt((tmp = props.getProperty(DATA_PORT)) == null ? "28930" : tmp.trim())));
        log("data_dc :"+data_dc.getLocalAddress());

        boards[0] = new byte[4]; // 0 - UPSU
        boards[1] = new byte[8]; // 1 - POVS
        boards[2] = new byte[1]; // 2 - GEN
        ids[0] = new byte[4]; // 0 - UPSU
        ids[1] = new byte[8]; // 1 - POVS
        ids[2] = new byte[1]; // 2 - GEN
        int idx1 = 0, idx2 = 0, idx3 = 0;
        for (Object next : hwProps.keySet()) {
        	String nextKey  =next.toString();
        	if (nextKey.indexOf(".") < 0) {
        		if (nextKey.startsWith("GEN")) {
            		boards[2][idx3] = Byte.valueOf(hwProps.getProperty(nextKey));
            		log("NEXT device :" ,nextKey, ", m_port=", boards[2][idx3], ", id=", Byte.valueOf(nextKey.substring(3)));
            		ids[2][idx3++] = Byte.valueOf(nextKey.substring(3));
            	} else if (nextKey.startsWith("POVS")) {
            		boards[1][idx2] = Byte.valueOf(hwProps.getProperty(nextKey));
            		log("NEXT device :", nextKey, ", m_port=", boards[1][idx2], ", id=", Byte.valueOf(nextKey.substring(4)));
            		ids[1][idx2++] = Byte.valueOf(nextKey.substring(4));
            	} else if (nextKey.startsWith("UPSU")) {
            		boards[0][idx1] = Byte.valueOf(hwProps.getProperty(nextKey));
            		log("NEXT device :", nextKey, ", m_port=", boards[0][idx1], ", id=", Byte.valueOf(nextKey.substring(4)));
            		ids[0][idx1++] = Byte.valueOf(nextKey.substring(4));
            	}
        	}
        }
        // correct length of arrays
        correctLength(boards[0], idx1);
        correctLength(boards[1], idx2);
        correctLength(boards[2], idx3);
        correctLength(ids[0], idx1);
        correctLength(ids[1], idx2);
        correctLength(ids[2], idx3);
        log("HUB found ", idx1 + idx2 + idx3, " devices");
        new Thread(this).start();
        new DataThread().start();
        return init(props, hwProps);
    }
    
    private void correctLength(byte[] target, int length) {
    	if (target.length != length) {
        	byte fTmp[] = target;
        	target = new byte[length];
            System.arraycopy(fTmp, 0, target, 0, length);
        }
    }

    protected boolean init(Properties props, Properties hwProps) throws IOException {
        return true;
    }
    
    public final void run() {
    	log(" ----- start CmdThread "+new Date());
		try {
			while (bStopFlag && !isFailed) {
				//read header
				ByteBuffer header = readBuffer(cmd_dc, 4);
				if (header != null && header.position() > 0) {
					log(" ----- CMD Read: " + toString(header));
					if (header.position() != 4) {
						isFailed = true;
						error("Can't read CMD header");
					} else {
						int length = ((int) header.get(3)) & 0xff << 8; 
						length |= ((int) header.get(2)) & 0xff;
						ByteBuffer content = null;
						if (length > 0) {
							content = readBuffer(cmd_dc, length);
						}
						byte[] buf = null;
				    	switch (header.get(0)) {
				    	case 2: // reading EEPROM from POVS/UPSU/GEN
				    		buf = command2(header, content);
				    		break;
				    	case 4: // set ADC
				    		buf = command4(header, content);
				    		break;
				    	case 5: //set DAC & setFreqPOVS
				    		buf = command5(header, content);
				    		break;
				    	case 6: //set PLL, answer is 3 bytes data for the reg & setAD9230POVS
				    		buf = command6(header, content);
				    		break;
				    	case 7: // setMeasAll
				    		buf = command7(header, content);
				    		break;
				    	case 14: // start HUB
				    		buf = command14(header, content);
				    		break;
				    	case 15: // stop HUB
				    		buf = command15(header, content);
				    		break;
				    	case 16: // setStepModeGen
				    		buf = command16(header, content);
				    		break;
				    	case 66: // get HUB param CMD_GET_PARAM = 66
				    		buf = command66(header, content);
				    		break;
				    	case 100: // send configuration  CMD_FIND_DEVICES = 100
				    		buf = command100(header, content);
				    		break;
				    	default:
				    		log("Unexpected command: ", toString(header), ", Content ", length > 0 ? toString(content): "EMPTY");
				    	}
				    	if (buf != null) {
				    		send(buf, 0, buf.length);
				    	}
				        /*
				        CMD_SET_PARAM    = 65,
				        CMD_START        = 78,
				        CMD_STOP         = 79,
				        CMD_SET_DATA_LEN = 84,
				        CMD_ECHO         = 90,
				        */
					}
				} else {
					try {
						Thread.sleep(5);
					} catch (Throwable e) {}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			isFailed = true;
		}
		log(" ----- stop CmdThread "+new Date());
	}
    
    protected byte[] command2(ByteBuffer header, ByteBuffer content) {
    	byte[] buf = new byte[32];
    	boolean bFlag = true;
		for (int i = 0; i < boards.length && bFlag; i++) {
            byte[] type = boards[i]; // 0 - UPSU, 1 - POVS, 2 - GEN
            for (int j = 0; j < type.length && bFlag; j++) {
    			if (type[j] == header.get(1)) {
    				buf[0] = type[j];
    				//buf[0] = ids[i][j];
    				bFlag = false;
    			}
    		}
        }
		//log(" ----- CMD send EEPROM POVS/UPSU/GEN");
		return buf;
    }
    
    protected byte[] command4(ByteBuffer header, ByteBuffer content) {
    	byte[] buf = new byte[2];
    	buf[0] = 4;
    	buf[1] = boards[2][0];
    	//log(" ----- CMD set ADC");
		return buf;
    }
    
    protected byte[] command5(ByteBuffer header, ByteBuffer content) {
    	byte[] buf = new byte[2];
    	boolean bFlag = true;
		buf[0] = 5;
		for (int i = 0; i < boards.length && bFlag; i++) {
            byte[] type = boards[i]; // 0 - UPSU, 1 - POVS, 2 - GEN
            for (int j = 0; j < type.length && bFlag; j++) {
    			if (type[j] == header.get(1)) { 
    				buf[1] = boards[i][j];
    				bFlag = false;
    			}
    		}
        }
		//log(" ----- CMD set DAC or setFreqPOVS");
		return buf;
    }
    
    protected byte[] command6(ByteBuffer header, ByteBuffer content) {
    	byte[] buf = new byte[3];
    	boolean bFlag = true;
		buf[0] = 6;
		for (int i = 0; i < boards.length && bFlag; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length && bFlag; j++) {
    			if (type[j] == header.get(1)) { 
    				buf[1] = boards[i][j];
    				bFlag = false;
    			}
    		}
        }
		//log(" ----- CMD set PLL or setAD9230POVS");
		return buf;
    }
    
    protected byte[] command7(ByteBuffer header, ByteBuffer content) {
    	byte[] buf = new byte[2];
    	boolean bFlag = true;
		buf[0] = 7;
		for (int i = 0; i < boards.length && bFlag; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length && bFlag; j++) {
    			if (type[j] == header.get(1)) { 
    				buf[1] = boards[i][j];
    				bFlag = false;
    			}
    		}
        }
		//log(" ----- CMD setMeasAll");
		return buf;
    }
    
    protected byte[] command14(ByteBuffer header, ByteBuffer content) {
    	byte[] buf =new byte[64];
		for (int i = 0; i < boards.length; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length; j++) {
            	buf[type[j]] = 1;
            }
        }
		//log(" ----- CMD send start HUB");
		return buf;
    }
    
    protected byte[] command15(ByteBuffer header, ByteBuffer content) {
    	byte[] buf = new byte[64];
		for (int i = 0; i < boards.length; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length; j++) {
            	buf[type[j]] = 1;
            }
        }
		//log(" ----- CMD send stop HUB");
		return buf;
    }
    
    protected byte[] command16(ByteBuffer header, ByteBuffer content) {
    	byte[] buf = new byte[3];
		buf[0] = 16;
		buf[1] = boards[2][0];
		//log(" ----- CMD setStepModeGen");
		return buf;
    }
    
    protected byte[] command66(ByteBuffer header, ByteBuffer content) {
    	byte[] buf = new byte[32];
		buf[0] = 39;
		buf[1] = 55;
		//log(" ----- CMD send HUB param");
		return buf;
    }
    
    protected byte[] command100(ByteBuffer header, ByteBuffer content) {
    	byte[] buf = new byte[128];
		for (int i = 0; i < boards.length; i++) {
            byte[] type = boards[i];
            for (int j = 0; j < type.length; j++) {
                byte sudAddr = type[j];
                buf[sudAddr * 2] = ids[i][j];
                // 0 - UPSU - 0x81
                // 1 - POVS - 0x82
                // 2 - GEN  - 0x83
                buf[sudAddr * 2 + 1] = (byte) (i == 2 ? 0x83 : i == 1 ? 0x82 : 0x81);
            }
        }
		//log(" ----- CMD send configuration " + toString(ByteBuffer.wrap(buf)));
		return buf;
    }
	
    private ByteBuffer readBuffer(DatagramChannel dc, int length) throws IOException {
    	ByteBuffer buf = ByteBuffer.allocateDirect(length);
    	SocketAddress soc = null;
		long timestamp = System.currentTimeMillis();
		while (bStopFlag && (soc = dc.receive(buf)) == null && System.currentTimeMillis() - timestamp < timeout) {
		}
		if (soc == null) {
            return null;
        } else if (cmd_dc == dc) {
        	if (serverCmdAddr == null) {
            	serverCmdAddr = soc;
            	log(" ----- serverCmdAddr="+serverCmdAddr);
        	}
        } else {
        	if (serverDataAddr == null) {
            	serverDataAddr = soc;
            	log(" ----- serverDataAddr="+serverDataAddr);
        	}
        }
        return buf;
	}

    protected void send(byte[] send, int offset, int length) throws IOException {
    	if (serverCmdAddr != null) {
    		cmd_dc.send(ByteBuffer.wrap(send, offset, length), serverCmdAddr);
    	}
    }
    
    private class DataThread extends Thread {
    	
    	public void run() {
    		log(" ----- start DataThread "+new Date());
    		try {
    			while (bStopFlag && !isFailed) {
    				//read header
    				ByteBuffer header = readBuffer(data_dc, 4);
    				if (header != null && header.position() > 0) {
    					log(" ----- DATA Read: " + GeneratorBaseTest.this.toString(header)+", header.position()="+header.position());
    					if (header.position() != 4) {
    						isFailed = true;
    						error("Can't read DATA header");
    					} else {
    						int length = ((int) header.get(3)) & 0xff << 8; 
    						length |= ((int) header.get(2)) & 0xff;
    						log(" ----- DATA Content length = " + length);
    						if (length > 0) {
    							ByteBuffer content = readBuffer(data_dc, length);
        						log(" ----- DATA Content: " + GeneratorBaseTest.this.toString(content));
    						}
    					}
    				} else {
    					try {
    						Thread.sleep(5);
    					} catch (Throwable e) {}
    				}
    			}
    		} catch (Throwable e) {
    			error(e);
    			isFailed = true;
    		}
    		log(" ----- stop DataThread "+new Date());
    	}
    	
    	// Blocking
        /*protected boolean setDataLenHub(int dLen) throws IOException {
            byte[] tmp = new byte[] {84, 0, 2, 0, 0, 0};
            tmp[5] = (byte) (dLen & 0xFF);
            tmp[4] = (byte) (dLen >> 8 & 0xFF);
            data_dc.send(ByteBuffer.wrap(tmp), dataAddr); //serverDataAddr
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
        }*/
    }
}
