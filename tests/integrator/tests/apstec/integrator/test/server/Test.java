package apstec.integrator.test.server;

import java.io.IOException;

import apstec.integrator.test.Package;

/**
 * Simple test.
 *
 * @test
 * @sources Test.java OneConnectionTest.java BaseTest.java Client.java Package.java
 * @executeClass apstec.integrator.test.server.Test
 */
public class Test extends ServerConnectionTest {
	
	public static void main(String[] args) {
		main(args, new Test());
	}

	protected boolean test() throws IOException {
		log("Test");
		//int testTime 
		int timeout = (int) getProp(TIMEOUT);
		long timestamp = System.currentTimeMillis();
		Package pkg = null;
		while ((pkg = client.get()) == null && System.currentTimeMillis() - timestamp < timeout) {
			mySleep(5);
		}
		log("Package", pkg == null ? "null" : pkg.toFullString());
		if (pkg != null) {
			client.send(new byte[]{3,3, 0,0,0,0 ,0,0,0,0}); //FM_STATE_RUNNING
		}
		mySleep(10000);
		return pkg != null;
	}
}
