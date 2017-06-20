package apstec.integrator.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class Client extends Thread {
	private SocketChannel sc;
	private volatile boolean bFlag = true, isFailed = false;
	private BaseTest bTest;
	private ArrayList<Package> recieved = new ArrayList<Package>();
	
	public Client(BaseTest bTest, String host, int port) throws IOException {
		this.bTest = bTest;
		sc = SocketChannel.open(new InetSocketAddress(host, port));
		sc.setOption(StandardSocketOptions.SO_KEEPALIVE, true).configureBlocking(false);
		if (sc.isConnected()) {
			bFlag = true;
			this.start();
		} else {
			throw new IOException("Connection is not prepared");
		}
	}
	
	public void run() {
		try {
			int timeout = (int) bTest.getProp(BaseTest.SOCKET_TIMEOUT);
			ByteBuffer headerBuffer = ByteBuffer.allocate(10);
			while (bFlag && !isFailed) {
				//read header
				headerBuffer.clear();
				readBuffer(headerBuffer, timeout);
				if (headerBuffer.position() > 0) {
					if (headerBuffer.hasRemaining()) {
						isFailed = true;
						bTest.error("Can't read header");
					} else {
						Package pkg = new Package(headerBuffer);
						//bTest.log(pkg.toString());
						if (pkg.m_size > 0) {
							ByteBuffer content = ByteBuffer.allocate((int) pkg.m_size);
							readBuffer(content, timeout);
							if (content.hasRemaining()) {
								isFailed = true;
								bTest.error("Can't read content", pkg.toFullString());
								continue;
							} else {
								pkg.content = content;
							}
						}
						add(pkg);
					}
				} 
			}
		} catch (Throwable e) {
			bTest.error(e);
			isFailed = true;
		}
	}
	
	public synchronized Package get() {
		if (!recieved.isEmpty()) {
			return recieved.remove(0);
		}
		return null;
	}
	
	public boolean isFailed() {
		return isFailed;
	}
	
	public boolean stopClient() {
		bFlag = false;
		try {
			sc.close();
		} catch (Throwable e) {
			bTest.error(e);
			return false;
		}
		return !isFailed;
	}
	
	public int send(byte[] buf) throws IOException {
		return sc.write(ByteBuffer.wrap(buf));
	}
	
	private void readBuffer(ByteBuffer bb, int timeout) throws IOException {
		int length = -1;
		long timestamp = System.currentTimeMillis();
		while (bb.hasRemaining() && System.currentTimeMillis() - timestamp < timeout) {
			if ((length = sc.read(bb)) < 0) {
				isFailed = true;
				bTest.error("The channel has reached end-of-stream");
				break;
			} else if (length == 0) {
				bTest.mySleep(5);
			}
		}
	}
	
	private synchronized void add(Package pkg) {
		recieved.add(pkg);
	} 
}
