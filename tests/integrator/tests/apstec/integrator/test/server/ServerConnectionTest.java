package apstec.integrator.test.server;

import java.io.IOException;

import apstec.integrator.test.BaseTest;
import apstec.integrator.test.Client;

public abstract class ServerConnectionTest extends BaseTest {
	protected Client client;
	
	protected boolean init() throws IOException {
		try {
			log("Try to open socket channel");
			client = new Client(this, (String) getProp(BaseTest.IP), (int) getProp(BaseTest.PORT));
		} catch (Throwable e) {
			error(e);
			return false;
		}
		return true;
	}
	
	protected boolean finish() {
		return client.stopClient();
	}
}
