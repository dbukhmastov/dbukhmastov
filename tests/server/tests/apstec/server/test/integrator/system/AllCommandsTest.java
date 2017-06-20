package apstec.server.test.integrator.system;

import apstec.server.test.integrator.GetConfiguration;
import apstec.server.test.integrator.IntegratorBaseTest;

/**
 * @test
 * @id AllCommandsTest
 * @executeClass apstec.server.test.integrator.system.AllCommandsTest
 * @source AllCommandsTest.java
 * @title Test chain of all command
 */
public class AllCommandsTest extends IntegratorBaseTest implements GetConfiguration{
	public static void main(String[] args) {
        main(new AllCommandsTest(), args);
    }

	protected boolean runTest() throws Throwable {
		if (startServer(null)) {
			boolean result = true;
			byte[] cfgPackage = getConfiguration();
			byte[] tmp = "2016-01-01 01:01:01.100".getBytes();
			byte[] synchPackage = new byte[10 + tmp.length];
			int length = tmp.length;
			synchPackage[0] = 5;
			synchPackage[6] = (byte) (length & 0xff);
			synchPackage[7] = (byte) ((length >> 8) & 0xff);
			synchPackage[8] = (byte) ((length >> 16) & 0xff);
			synchPackage[9] = (byte) ((length >> 24) & 0xff);
			System.arraycopy(tmp, 0, synchPackage, 10, tmp.length);
			
			long timestamp = System.currentTimeMillis();
			while (result && System.currentTimeMillis() - timestamp < testTime) {
				log("clear config");
				integrator.send(new byte[]{0, 6, 0,0,0,0, 0,0,0,0}, 0, 10); // clear config
				Thread.sleep(15000);
				result = integrator.systemState() == 1;
				if (!result) {
					log("wrong system state after clear config ", integrator.systemState());
				}
				log("set config");
				integrator.send(cfgPackage, 0, cfgPackage.length); // set config
				Thread.sleep(15000);
				result = integrator.systemState() == 2;
				if (!result) {
					log("wrong system state after set config ", integrator.systemState());
				}
				log("measure zero");
				integrator.send(new byte[]{0, 4, 0,0,0,0, 0,0,0,0}, 0, 10); // measure zero
				Thread.sleep(15000);
				result = integrator.systemState() == 2;
				if (!result) {
					log("wrong system state after clear config ", integrator.systemState());
				}
				log("synchronization");
				integrator.send(synchPackage, 0, synchPackage.length); // synchronization
				Thread.sleep(15000);
				result = integrator.systemState() == 2;
				if (!result) {
					log("wrong system state after clear config ", integrator.systemState());
				}
				log("start server");
				integrator.send(new byte[]{0, 8, 0,0,0,0, 0,0,0,0}, 0, 10); // start server
				Thread.sleep(15000);
				result = integrator.systemState() == 3;
				if (!result) {
					log("wrong system state after run ", integrator.systemState());
				}
				log("stop server");
				integrator.send(new byte[]{0, 5, 0,0,0,0, 0,0,0,0}, 0, 10); // stop server
				Thread.sleep(15000);
				result = integrator.systemState() == 2;
				if (!result) {
					log("wrong system state after stop ", integrator.systemState());
				}
			}
			return result;
		} 
		return false;
	}
}
