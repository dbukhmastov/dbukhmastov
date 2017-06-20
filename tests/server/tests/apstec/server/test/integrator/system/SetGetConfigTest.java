package apstec.server.test.integrator.system;

import apstec.server.test.integrator.GetConfiguration;
import apstec.server.test.integrator.IntegratorBaseTest;
import apstec.server.test.integrator.Package;

/**
 * @test
 * @id SetGetConfigTest
 * @executeClass apstec.server.test.integrator.system.SetGetConfigTest
 * @source SetGetConfigTest.java
 * @title Test setConfig and GetConfig command
 */
public class SetGetConfigTest extends IntegratorBaseTest implements GetConfiguration{
	public static void main(String[] args) {
        main(new SetGetConfigTest(), args);
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
			Thread.sleep(30000);
			byte[] cfgPackage = getConfiguration();
			result &= integrator.systemState() == 1;
			if (!result) {
				log("wrong system state before configuration ", integrator.systemState());
			}
			integrator.send(cfgPackage, 0, cfgPackage.length); // set config
			Thread.sleep(15000);
			result &= integrator.systemState() == 2;
			if (!result) {
				log("wrong system state after configuration ", integrator.systemState());
			}
			Thread.sleep(30000);
			result &= integrator.systemState() == 2;
			if (!result) {
				log("wrong system state after configuration ", integrator.systemState());
			}
			integrator.send(new byte[]{6, 9, 0,0,0,0, 0,0,0,0}, 0, 10); // get config
			Thread.sleep(30000);
			Package config = integrator.config();
			if (config == null) {
				result = false;
				log("Configuration has not been recieved");
			} else if (config.content() != null && config.content().position() == cfgPackage.length - 10) {
				for (int i = 0; i < config.content().position() && result; i++) {
					if (config.content().get(i) != cfgPackage[i + 10]) {
						result = false;
						log("Wrong config, index=", i,", ", config.content().get(i), "!=",cfgPackage[i + 10]);
						log("Recieved configuration is wrong ", config.toFullString());
					}
				}
			} else {
				result = false;
				log("Recieved configuration is wrong ", config.toFullString());
			}
			return result;
		}
		return false;
	}
}
