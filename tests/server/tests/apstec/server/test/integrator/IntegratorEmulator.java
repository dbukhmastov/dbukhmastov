package apstec.server.test.integrator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import apstec.server.test.BaseTest;

public class IntegratorEmulator extends Thread {
	private ServerSocketChannel ssc;
    private SocketChannel sc;
    private BaseTest baseTest;
    private long timeout;
    private volatile Package config, synch, state; 
    private int systemState = -1;
    private volatile AlarmListener alarmListener;
    
    public volatile boolean bStopFlag = true, isFailed = false;
	
	public IntegratorEmulator(int port, long timeout, BaseTest baseTest) throws IOException {
		this.baseTest = baseTest;
		this.timeout = timeout;
		ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress("127.0.0.1", port));
	}
	
	public void addAlarmListener(AlarmListener alarmListener) {
		this.alarmListener = alarmListener;
	}
	
	public boolean reconnect() throws IOException {
		if (sc != null) {
			try {
				synchronized(this) {
			    	sc.close();
				}
			} catch (Throwable e) {
	            baseTest.error("Can't close existing connection.");
	            e.printStackTrace();
	            return false;
	        } finally {
	        	synchronized(this) {
	        		sc = null;
				}
	        }
		}
		config = null;
		synch = null;
		state = null; 
	    systemState = -1;
		long timeStamp = System.currentTimeMillis();
        while ((sc = ssc.accept()) == null && System.currentTimeMillis() - timeStamp <= timeout) {
        }
        if (sc == null || !sc.isConnected()) {
        	baseTest.error("Server has not been connected.");
        	return false;
        }
        baseTest.log("Client is here.");
        sc.setOption(StandardSocketOptions.SO_KEEPALIVE, true).configureBlocking(false);
        return true;
	}
	
	public int systemState() {
		return systemState;
	}
	
	public Package config() {
		return config;
	}
	
	public boolean waitForSystemState(int state, long waitTime) {
		long timestamp = System.currentTimeMillis();
		while (systemState != state && System.currentTimeMillis() - timestamp < waitTime) {
			try {
				Thread.sleep(5);
			} catch(Throwable e) {}
		}
		return systemState == state;
	}
	
	public boolean waitForNotSystemState(int state, long waitTime) {
		long timestamp = System.currentTimeMillis();
		while (systemState == state && System.currentTimeMillis() - timestamp < waitTime) {
			try {
				Thread.sleep(5);
			} catch(Throwable e) {}
		}
		return systemState != state;
	}
	
	public void cleanup() {
        try {
        	sc.close();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
        	sc = null;
        }
        try {
        	ssc.close();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
        	ssc = null;
        }
    }
	
	// UnBlocking
	public boolean send(byte[] send, int offset, int length) throws IOException {
		int iRet = -1;
		long timestamp = System.currentTimeMillis();
		while (System.currentTimeMillis() - timestamp < timeout) {
			if (sc != null) {
				ByteBuffer buf = ByteBuffer.wrap(send, offset, length);
				synchronized(this) {
					iRet = sc == null ? -1 : sc.write(buf);
				}
			}
			if (iRet > 0) {
				return true;
			} else if (iRet != -1) {
				baseTest.error("Can't send data to server");
	        	return false;
			}
			try {
				Thread.sleep(1);
			} catch(Throwable e) {}
		}
		baseTest.error("Can't send data to server, timeout");
        return false;
    }
	
	public void run() {
		try {
			while (bStopFlag && !isFailed) {
				//read header
				ByteBuffer header = readBuffer(10);
				if (header.position() > 0) {
					if (header.hasRemaining()) {
						isFailed = true;
						baseTest.error("Can't read header");
					} else {
						Package pkg = new Package(header, baseTest);
						if (pkg.m_size > 0) {
							//read content
							ByteBuffer content = readBuffer((int)pkg.m_size);
							if (content.hasRemaining()) {
								isFailed = true;
								baseTest.error("Can't read content", pkg.toFullString());
								continue;
							} else if (!pkg.content(content)) {
								isFailed = true;
								baseTest.error("Can't parse the message content.", pkg.toFullString());
								continue;
							}
						}
						//baseTest.log(" ----- Package: ", pkg.toFullString(), " --------- ", new Date());
				    	switch (pkg.m_data_type) {
				    	case 3: // System state
				    		state = pkg;
				    		if (pkg.m_counter == 1) {
				    			systemState = 10;
				    		} else if (systemState != pkg.m_code_message) {
				    			systemState = pkg.m_code_message;
				    			baseTest.log(" ----- System state: ", pkg.toFullString());
				    		}
				    		break;
				    	case 4: //New alarm
				    		if (alarmListener != null) {
				    			alarmListener.alarm(pkg);
				    		}
				    		//baseTest.log(" ----- Alarm : ", pkg.toString());
				    		break;
				    	case 5: //Synch
				    		synch = pkg;
				    		baseTest.log(" ----- Synch : ", pkg.hashCode());
				    		break;
				    	case 6: //Configuration
				    		config  = pkg;
				    		baseTest.log(" ----- Configuration : ", pkg.hashCode());
				    		break;
				    	default:
				    		baseTest.log(" ----- Unknown : ", pkg.hashCode());
				    	}
					}
				} else {
					try {
						Thread.sleep(5);
					} catch (Throwable e) {}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			baseTest.error(e);
			isFailed = true;
		}
		baseTest.log("IntegratorEmulator is stop.");
	}

	private ByteBuffer readBuffer(int read) throws IOException {
    	ByteBuffer buf = ByteBuffer.allocateDirect(read);
		int length = -1;
		long timestamp = System.currentTimeMillis();
		while (buf.hasRemaining() && System.currentTimeMillis() - timestamp < timeout) {
			if (sc != null) {
				synchronized(this) {
					length = sc == null ? 0 : sc.read(buf);
				}
				if (length < 0) {
					isFailed = true;
					baseTest.log("The channel has reached end-of-stream");
					break;
				} else if (length == 0) {
					try {
						Thread.sleep(5);
					} catch (Throwable e) {}
				}
			}
		}
		return buf;
	}
}
