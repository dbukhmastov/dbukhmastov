package apstec.server.test.generator.system;

import java.nio.ByteBuffer;
import java.util.Date;

import apstec.server.test.generator.GeneratorBaseTest;

/**
 * @test
 * @id DataAvalancheTest
 * @executeClass apstec.server.test.generator.system.DataAvalancheTest
 * @source DataAvalancheTest.java
 * @title Test DataAvalancheTest command
 */
public class DataAvalancheTest extends GeneratorBaseTest {

	public static void main(String[] args) {
		main(new DataAvalancheTest(), args);
	}

	protected boolean runTest() throws Throwable {
		long timestamp = System.currentTimeMillis();
		while (serverDataAddr == null && System.currentTimeMillis() - timestamp < timeout) {
			try {
				Thread.sleep(5);
			} catch(Throwable e) {}
		}
		if (serverDataAddr == null) {
			log("Can't obtain server Data port");
			return false;
		} else {
			new SendDataThread().start();
			Thread.sleep(120000);
		}
		return true;
	}

	private class SendDataThread extends Thread {
		public boolean bStopFlag = false;
		public void run() {
			byte[] send = new byte[1024];
			log(" ----- start SendDataThread " + new Date());
			while (!bStopFlag) {
				try {
					data_dc.send(ByteBuffer.wrap(send, 0, send.length), serverDataAddr);
				} catch (Throwable e) {
					e.printStackTrace();
					bStopFlag = true;
				}
			}
			log(" ----- stop SendDataThread " + new Date());
		}
	}

}
