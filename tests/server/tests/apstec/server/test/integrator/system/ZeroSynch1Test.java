package apstec.server.test.integrator.system;

import apstec.server.test.integrator.GetConfiguration;
import apstec.server.test.integrator.IntegratorBaseTest;

/**
 * @test
 * @id ZeroSynch1Test
 * @executeClass apstec.server.test.integrator.system.ZeroSynch1Test
 * @source ZeroSynch1Test.java
 * @title Test measure zero and synchronization command before configuration
 */
public class ZeroSynch1Test extends IntegratorBaseTest implements GetConfiguration{
	public static void main(String[] args) {
        main(new ZeroSynch1Test(), args);
    }

	protected boolean runTest() throws Throwable {
		if (startServer(null)) {
			boolean result = true;
			log("clear config");
			integrator.send(new byte[]{0, 6, 0,0,0,0, 0,0,0,0}, 0, 10); // clear config
			Thread.sleep(15000);
			result &= integrator.systemState() == 1;
			if (!result) {
				log("wrong system state after clear config ", integrator.systemState());
			}
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
				log("measure zero");
				integrator.send(new byte[]{0, 4, 0,0,0,0, 0,0,0,0}, 0, 10); // measure zero
				Thread.sleep(15000);
				result &= integrator.systemState() == 1;
				if (!result) {
					log("wrong system state after clear config ", integrator.systemState());
				}
				log("synchronization");
				integrator.send(synchPackage, 0, synchPackage.length); // synchronization
				Thread.sleep(15000);
				result &= integrator.systemState() == 1;
				if (!result) {
					log("wrong system state after clear config ", integrator.systemState());
				}
			}
			return result;
		}
		return false;
	}
}
