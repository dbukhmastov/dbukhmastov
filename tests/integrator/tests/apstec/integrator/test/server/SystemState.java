package apstec.integrator.test.server;

import java.io.IOException;

import apstec.integrator.test.Package;

/**
 * Simple test.
 *
 * @test
 * @sources SystemState.java OneConnectionTest.java BaseTest.java Client.java Package.java
 * @executeClass apstec.integrator.test.server.SystemState
 */
public class SystemState extends ServerConnectionTest {
	
	public static void main(String[] args) {
		main(args, new SystemState());
	}

	protected boolean test() throws IOException {
		log("Test");
		byte[][] states = {new byte[]{3,0, 0,0,0,0 ,0,0,0,0}, //FM_STATE_UNKNOWN
				new byte[]{3,1, 0,0,0,0 ,0,0,0,0}, //FM_STATE_UNCONFIGURED
				new byte[]{3,2, 0,0,0,0 ,0,0,0,0}, //FM_STATE_STOPPED
				new byte[]{3,3, 0,0,0,0 ,0,0,0,0}}; //FM_STATE_RUNNING
		long testTime = (long) getProp(STRESS_TEST_TIME);
		long timestamp = System.currentTimeMillis();
		while (System.currentTimeMillis() - timestamp < testTime) {
			for (byte[] next : states) {
				if (client.send(next) <= 0) {
					error("Can't send state");
					return false;
				} else {
					mySleep(10);
					Package pkg = client.get();
					if (pkg != null) {
						log(pkg.toFullString());
					}
				}
			}
		}
		return true;
	}
}
