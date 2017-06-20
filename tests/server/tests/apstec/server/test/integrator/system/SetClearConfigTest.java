package apstec.server.test.integrator.system;

import apstec.server.test.integrator.GetConfiguration;
import apstec.server.test.integrator.IntegratorBaseTest;

/**
 * @test
 * @id SetClearConfigTest
 * @executeClass apstec.server.test.integrator.system.SetClearConfigTest
 * @source SetClearConfigTest.java
 * @title Test set - clear config
 */
public class SetClearConfigTest extends IntegratorBaseTest implements GetConfiguration{
	public static void main(String[] args) {
        main(new SetClearConfigTest(), args);
    }

	protected boolean runTest() throws Throwable {
		if (startServer(null)) {
			boolean result = true;
			byte[] cfgPackage = getConfiguration();
			long timestamp = System.currentTimeMillis();
			while (result && System.currentTimeMillis() - timestamp < testTime) {
				log("clear config");
				integrator.send(new byte[]{0, 6, 0,0,0,0, 0,0,0,0}, 0, 10); // clear config
				Thread.sleep(15000);
				result &= integrator.systemState() == 1;
				if (!result) {
					log("wrong system state after clear config ", integrator.systemState());
				}
				log("set config");
				integrator.send(cfgPackage, 0, cfgPackage.length); // set config
				Thread.sleep(15000);
				result &= integrator.systemState() == 2;
				if (!result) {
					log("wrong system state after set config ", integrator.systemState());
				}
			}
			return result;
		} 
		return false;
	}
}
