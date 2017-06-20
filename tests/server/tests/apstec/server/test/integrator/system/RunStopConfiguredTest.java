package apstec.server.test.integrator.system;

import apstec.server.test.integrator.GetConfiguration;
import apstec.server.test.integrator.IntegratorBaseTest;

/**
 * @test
 * @id RunStopConfiguredTest
 * @executeClass apstec.server.test.integrator.system.RunStopConfiguredTest
 * @source RunStopConfiguredTest.java
 * @title Test setConfig and run-stop command
 */
public class RunStopConfiguredTest extends IntegratorBaseTest implements GetConfiguration{
	public static void main(String[] args) {
        main(new RunStopConfiguredTest(), args);
    }

	protected boolean runTest() throws Throwable {
		if (startServer(null)) {
			boolean result = true;
			integrator.send(new byte[]{0, 6, 0,0,0,0, 0,0,0,0}, 0, 10); // clear config
			Thread.sleep(15000);
			result &= integrator.systemState() == 1;
			if (!result) {
				log("wrong system state after start ", integrator.systemState());
			}
			byte[] cfgPackage = getConfiguration();
			integrator.send(cfgPackage, 0, cfgPackage.length);
			Thread.sleep(15000);
			result &= integrator.systemState() == 2;
			if (result) {
				long timestamp = System.currentTimeMillis();
				while (result && System.currentTimeMillis() - timestamp < testTime) {
					integrator.send(new byte[]{0, 8, 0,0,0,0, 0,0,0,0}, 0, 10); // start server
					Thread.sleep(15000);
					result &= integrator.systemState() == 3;
					if (result) {
						Thread.sleep(30000);
					} else {
						log("wrong system state after run ", integrator.systemState());
					}
					integrator.send(new byte[]{0, 5, 0,0,0,0, 0,0,0,0}, 0, 10); // stop server
					Thread.sleep(15000);
					result &= integrator.systemState() == 2;
					if (!result) {
						log("wrong system state after stop ", integrator.systemState());
					}
				}
			} else {
				log("wrong system state after configuration ", integrator.systemState());
			}
			return result;
		} 
		return false;
	}
}
